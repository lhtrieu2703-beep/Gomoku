package game.view;

import org.junit.jupiter.api.Test;
import javax.sound.sampled.*;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class SoundResourceTest {
    @Test void closesClipAfterPlaybackStops() throws Exception { verify("success"); }
    @Test void closesClipWhenOpenFails() throws Exception { verify("open"); }
    @Test void closesClipWhenStartFails() throws Exception { verify("start"); }
    @Test void staleResultClosesClipWithoutStarting() throws Exception { verify("stale"); }

    @Test void queuedPieceSoundChecksGameValidityBeforeOpeningAnyAudioDevice() throws Exception {
        boolean sound = SoundManager.isSoundEnabled();
        var field = SoundManager.class.getDeclaredField("PLAYBACK"); field.setAccessible(true);
        ThreadPoolExecutor playback = (ThreadPoolExecutor) field.get(null);
        CountDownLatch occupied = new CountDownLatch(2), release = new CountDownLatch(1), checked = new CountDownLatch(1);
        AtomicBoolean current = new AtomicBoolean(true), observedCurrent = new AtomicBoolean(true);
        try {
            for (int i = 0; i < 2; i++) playback.execute(() -> {
                occupied.countDown();
                try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
            assertTrue(occupied.await(3, TimeUnit.SECONDS));
            SoundManager.setSoundEnabled(true);
            SoundManager.playPlacePieceSound(() -> {
                observedCurrent.set(current.get()); checked.countDown(); return current.get();
            });
            current.set(false); release.countDown();
            assertTrue(checked.await(3, TimeUnit.SECONDS)); assertFalse(observedCurrent.get());
        } finally { release.countDown(); SoundManager.setSoundEnabled(sound); }
    }

    private void verify(String scenario) throws Exception {
        AtomicBoolean closed = new AtomicBoolean();
        AtomicBoolean started = new AtomicBoolean();
        AtomicReference<LineListener> listener = new AtomicReference<>();
        Clip clip = (Clip) Proxy.newProxyInstance(Clip.class.getClassLoader(), new Class<?>[]{Clip.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "addLineListener" -> listener.set((LineListener) args[0]);
                        case "open" -> { if (scenario.equals("open")) throw new LineUnavailableException(); }
                        case "start" -> {
                            started.set(true);
                            if (scenario.equals("start")) throw new IllegalStateException();
                            listener.get().update(new LineEvent((Line) proxy, LineEvent.Type.STOP, 1));
                        }
                        case "close" -> closed.set(true);
                        default -> throw new AssertionError("Unexpected Clip call: " + method.getName());
                    }
                    return null;
                });
        if (scenario.equals("stale")) SoundManager.playClip(clip, new byte[100], 44100, () -> false);
        else if (scenario.equals("success")) SoundManager.playClip(clip, new byte[100], 44100);
        else assertThrows(Exception.class, () -> SoundManager.playClip(clip, new byte[100], 44100));
        assertTrue(closed.get(), "Clip must close on success and every failure path");
        if (scenario.equals("stale")) assertFalse(started.get());
    }
}
