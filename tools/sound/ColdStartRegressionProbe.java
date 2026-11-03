package game.view;

import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import java.lang.reflect.Proxy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** Deterministic regression: a slow device must reach STOP before the Clip is closed. */
public final class ColdStartRegressionProbe {
    public static void main(String[] args) throws Exception {
        AtomicReference<LineListener> listener = new AtomicReference<>();
        AtomicBoolean stopped = new AtomicBoolean();
        AtomicBoolean closedEarly = new AtomicBoolean();
        ScheduledExecutorService device = Executors.newSingleThreadScheduledExecutor();
        long begin = System.nanoTime();
        Clip clip = (Clip) Proxy.newProxyInstance(Clip.class.getClassLoader(), new Class<?>[]{Clip.class},
                (proxy, method, arguments) -> {
                    switch (method.getName()) {
                        case "addLineListener" -> listener.set((LineListener) arguments[0]);
                        case "open" -> { }
                        case "start" -> device.schedule(() -> {
                            stopped.set(true);
                            listener.get().update(new LineEvent((Line) proxy, LineEvent.Type.STOP, 2646));
                        }, 1250, TimeUnit.MILLISECONDS);
                        case "close" -> closedEarly.set(!stopped.get());
                        default -> throw new AssertionError("Unexpected Clip call: " + method.getName());
                    }
                    return null;
                });
        try {
            SoundManager.playClip(clip, new byte[2646], 44100);
            System.out.printf("COLD_START delayMs=1250 elapsedMs=%.3f stopDelivered=%s closedBeforeStop=%s%n",
                    (System.nanoTime() - begin) / 1_000_000.0, stopped.get(), closedEarly.get());
            if (!stopped.get() || closedEarly.get()) throw new AssertionError("Clip closed before delayed STOP");
            System.out.println("COLD_START_REGRESSION PASS");
        } finally {
            device.shutdownNow();
        }
    }
}
