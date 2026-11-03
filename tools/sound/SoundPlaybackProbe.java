package game.view;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.Mixer;
import javax.swing.SwingUtilities;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;
import java.util.ArrayList;
import java.util.IdentityHashMap;

/** Real-device probe outside production. No JFrame, Preferences, or audio I/O on the EDT. */
public final class SoundPlaybackProbe {
    public static void main(String[] args) throws Exception {
        int intervalMs = args.length == 0 ? 200 : Integer.parseInt(args[0]);
        System.setProperty("gomoku.sound.diagnostics", args.length < 2 ? "true" : args[1]);
        System.out.printf("PROBE java=%s os=%s intervalMs=%d time=%s%n",
                System.getProperty("java.version"), System.getProperty("os.name"), intervalMs, Instant.now());
        SoundManager.setSoundEnabled(true);
        CacheObserver observer = CacheObserver.createIfPresent();
        for (int i = 1; i <= 10; i++) {
            String[] event = {null};
            long begin = System.nanoTime();
            SwingUtilities.invokeAndWait(() -> {
                long start = System.nanoTime();
                Instant time = Instant.now();
                SoundManager.playPlacePieceSound();
                event[0] = "time=" + time + " thread=" + Thread.currentThread().getName()
                        + " requestNs=" + start + " apiReturnMs=" + (System.nanoTime() - start) / 1_000_000.0;
            });
            System.out.printf("PROBE_REQUEST index=%d %s dispatchReturnMs=%.3f%n",
                    i, event[0], (System.nanoTime() - begin) / 1_000_000.0);
            Thread.sleep(intervalMs);
        }
        waitForPlayback();
        flushDiagnostics();
        if (observer != null) observer.finishAndVerify(10);
        System.out.println("PROBE_TEN_REQUESTS_DRAINED " + Instant.now());
        if (args.length >= 3 && args[2].equals("ten-only")) {
            System.out.println("PROBE_DONE " + Instant.now());
            System.exit(0);
        }
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(info);
            System.out.printf("MIXER name=%s description=%s sourceLines=%d targetLines=%d%n",
                    info.getName(), info.getDescription(), mixer.getSourceLineInfo().length, mixer.getTargetLineInfo().length);
        }
        checkFormat(new AudioFormat(44100, 8, 1, true, false));
        checkFormat(new AudioFormat(44100, 16, 1, true, false));
        checkFormat(new AudioFormat(48000, 16, 1, true, false));
        checkFormat(new AudioFormat(44100, 16, 2, true, false));
        compareReusedClip();
        System.out.println("PROBE_DONE " + Instant.now());
        System.exit(0);
    }

    private static void checkFormat(AudioFormat format) {
        System.out.printf("FORMAT_CHECK format=%s advertised=%s%n", format,
                AudioSystem.isLineSupported(new DataLine.Info(Clip.class, format)));
        long begin = System.nanoTime();
        try (Clip clip = AudioSystem.getClip()) {
            int frames = (int) (format.getSampleRate() * 0.06);
            byte[] silence = new byte[frames * format.getFrameSize()];
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.OPEN || event.getType() == LineEvent.Type.CLOSE) {
                    System.out.printf("FORMAT_LINE format=%s type=%s frame=%d thread=%s%n",
                            format, event.getType(), event.getFramePosition(), Thread.currentThread().getName());
                }
            });
            clip.open(format, silence, 0, silence.length);
            System.out.printf("FORMAT_OPEN_OK format=%s openMs=%.3f frames=%d isOpen=%s%n",
                    format, (System.nanoTime() - begin) / 1_000_000.0, clip.getFrameLength(), clip.isOpen());
            observePlayback(clip, "FRESH " + format, 3000);
        } catch (Exception error) {
            System.out.println("FORMAT_OPEN_FAILED format=" + format);
            error.printStackTrace(System.out);
        }
    }

    private static void compareReusedClip() throws Exception {
        AudioFormat format = new AudioFormat(44100, 8, 1, true, false);
        try (Clip clip = AudioSystem.getClip()) {
            clip.open(format, new byte[2646], 0, 2646);
            for (int i = 1; i <= 10; i++) {
                clip.stop();
                clip.setFramePosition(0);
                observePlayback(clip, "REUSED trial=" + i, 3000);
                Thread.sleep(140);
            }
        }
    }

    private static void observePlayback(Clip clip, String label, int timeoutMs) throws Exception {
        CountDownLatch stopped = new CountDownLatch(1);
        ConcurrentLinkedQueue<String> events = new ConcurrentLinkedQueue<>();
        long requested = System.nanoTime();
        javax.sound.sampled.LineListener listener = event -> {
            events.add("type=" + event.getType() + " sinceStartMs=" + (System.nanoTime() - requested) / 1_000_000.0
                    + " lineFrame=" + event.getFramePosition());
            if (event.getType() == LineEvent.Type.STOP) stopped.countDown();
        };
        clip.addLineListener(listener);
        clip.start();
        boolean completed = stopped.await(timeoutMs, TimeUnit.MILLISECONDS);
        System.out.printf("CONTROL %s completed=%s framePosition=%d frameLength=%d elapsedMs=%.3f events=%s%n",
                label, completed, clip.getFramePosition(), clip.getFrameLength(),
                (System.nanoTime() - requested) / 1_000_000.0, events);
        clip.removeLineListener(listener);
        if (!completed) clip.stop();
    }

    private static void waitForPlayback() throws Exception {
        Field slot = SoundManager.class.getDeclaredField("PLAYBACK");
        slot.setAccessible(true);
        ThreadPoolExecutor playback = (ThreadPoolExecutor) slot.get(null);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (playback.getActiveCount() != 0 || !playback.getQueue().isEmpty()) {
            if (System.nanoTime() > deadline) throw new IllegalStateException("Audio queue did not drain");
            Thread.sleep(20);
        }
        System.out.printf("POOL completed=%d active=%d queued=%d%n",
                playback.getCompletedTaskCount(), playback.getActiveCount(), playback.getQueue().size());
    }

    private static void flushDiagnostics() throws Exception {
        try {
            Field field = SoundManager.class.getDeclaredField("SOUND_DIAG_LOG");
            field.setAccessible(true);
            ExecutorService logger = (ExecutorService) field.get(null);
            if (logger != null) logger.submit(() -> {}).get(3, TimeUnit.SECONDS);
        } catch (NoSuchFieldException diagnosticsRemoved) {
            System.out.println("PROBE_DIAGNOSTIC_LOGGER absent");
        }
    }

    /** Observes the real cache without changing a factory, PCM data, or production code. */
    private static final class CacheObserver {
        private final Object cache;
        private final Field clipSlot;
        private final IdentityHashMap<Clip, javax.sound.sampled.LineListener> observed = new IdentityHashMap<>();
        private final List<String> events = new CopyOnWriteArrayList<>();
        private final List<Long> completedFrames = new CopyOnWriteArrayList<>();
        private final AtomicReference<Throwable> failure = new AtomicReference<>();
        private final Thread watcher;
        private volatile boolean done;

        static CacheObserver createIfPresent() throws Exception {
            try {
                Field field = SoundManager.class.getDeclaredField("PLACE_CLIP");
                field.setAccessible(true);
                return new CacheObserver(field.get(null));
            } catch (NoSuchFieldException beforeFix) {
                System.out.println("PROBE_CACHE_OBSERVER absent (pre-fix implementation)");
                return null;
            }
        }

        private CacheObserver(Object cache) throws Exception {
            this.cache = cache;
            clipSlot = cache.getClass().getDeclaredField("clip");
            clipSlot.setAccessible(true);
            watcher = new Thread(() -> {
                try {
                    while (!done) {
                        Clip clip = (Clip) clipSlot.get(cache);
                        if (clip != null && !observed.containsKey(clip)) {
                            javax.sound.sampled.LineListener listener = event -> {
                                // LineEvent position is immutable; do not query the native frame counter here.
                                events.add("PROBE_CLIP time=" + Instant.now() + " ns=" + System.nanoTime()
                                        + " type=" + event.getType() + " clip=" + System.identityHashCode(clip)
                                        + " thread=" + Thread.currentThread().getName() + " isOpen=" + clip.isOpen()
                                        + " isRunning=" + clip.isRunning() + " framePositionFromEvent=" + event.getFramePosition()
                                        + " frameLength=" + clip.getFrameLength());
                                if (event.getType() == LineEvent.Type.STOP) completedFrames.add(event.getFramePosition());
                            };
                            clip.addLineListener(listener);
                            observed.put(clip, listener);
                        }
                        Thread.sleep(1);
                    }
                } catch (InterruptedException end) {
                    Thread.currentThread().interrupt();
                } catch (Throwable error) { failure.set(error); }
            }, "sound-probe-observer");
            watcher.setDaemon(true);
            watcher.start();
        }

        void finishAndVerify(int expected) throws Exception {
            // STOP listeners run on Java Sound's event thread; wait for its final callback too.
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while (completedFrames.size() < expected && System.nanoTime() < deadline) Thread.sleep(10);
            done = true;
            watcher.interrupt();
            watcher.join(2000);
            for (String event : events) System.out.println(event);
            long completed = completedFrames.stream().filter(position -> position == 2646).count();
            System.out.printf("PROBE_COMPLETED expected=%d fullFrameStops=%d clipInstances=%d%n", expected, completed, observed.size());
            for (Clip clip : new ArrayList<>(observed.keySet())) {
                System.out.printf("PROBE_FINAL_STATE clip=%d isOpen=%s isRunning=%s framePosition=%d frameLength=%d%n",
                        System.identityHashCode(clip), clip.isOpen(), clip.isRunning(), clip.getFramePosition(), clip.getFrameLength());
                clip.removeLineListener(observed.get(clip));
                clip.close(); // Probe cleanup only; production keeps its bounded cache open.
            }
            if (failure.get() != null) throw new IllegalStateException("Observer failed", failure.get());
            if (completed != expected) throw new IllegalStateException("Not every requested sound reached the full frame count");
        }
    }
}
