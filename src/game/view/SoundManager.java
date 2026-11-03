package game.view;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/**
 * Trình quản lý âm thanh cho game Caro:
 * Tự động tổng hợp sóng âm thanh (Audio Synthesis) cho tiếng gõ cờ và tiếng chiến thắng
 * mà không cần file ngoài. Phát trên luồng nền, giữ Clip mở để tránh khởi động lại
 * thiết bị ở mỗi nước đi; lỗi thiết bị được ghi ra stderr.
 */
public class SoundManager {
    private static volatile boolean soundEnabled = true;
    private static final ThreadPoolExecutor PLAYBACK = new ThreadPoolExecutor(
            2, 2, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(8), task -> {
                Thread thread = new Thread(task, "gomoku-audio");
                thread.setDaemon(true);
                return thread;
            }, new ThreadPoolExecutor.DiscardOldestPolicy());
    static { PLAYBACK.allowCoreThreadTimeOut(true); }
    private static final CachedClip PLACE_CLIP = new CachedClip();
    private static final CachedClip VICTORY_CLIP = new CachedClip();

    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    public static boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Phát tiếng "cách" đanh của quân cờ gỗ khi đặt xuống bàn cờ.
     */
    public static void playPlacePieceSound() {
        playPlacePieceSound(() -> true);
    }

    public static void playPlacePieceSound(BooleanSupplier currentGame) {
        if (!soundEnabled) return;

        PLAYBACK.execute(() -> {
            if (!soundEnabled || !currentGame.getAsBoolean()) return;
            try {
                float sampleRate = 44100;
                int durationMs = 60;
                int numSamples = (int) (sampleRate * durationMs / 1000);
                byte[] buffer = new byte[numSamples];

                // Tổng hợp âm thanh gõ gỗ: Tần số khoảng 800Hz giảm dần về 300Hz với hàm tắt dần nhanh
                for (int i = 0; i < numSamples; i++) {
                    double time = i / (double) sampleRate;
                    double freq = 800.0 - 500.0 * (i / (double) numSamples);
                    double decay = Math.exp(-time * 50.0); // Tắt dần rất nhanh để tạo tiếng gõ gọn
                    double sinVal = Math.sin(2.0 * Math.PI * freq * time);
                    buffer[i] = (byte) (sinVal * decay * 110);
                }

                if (currentGame.getAsBoolean())
                    PLACE_CLIP.play(buffer, sampleRate, () -> soundEnabled && currentGame.getAsBoolean());
            } catch (Exception error) {
                reportFailure("place", error);
            }
        });
    }

    /**
     * Phát âm thanh chuông reo vui vẻ khi người chơi chiến thắng.
     */
    public static void playVictorySound() {
        playVictorySound(() -> true);
    }

    public static void playVictorySound(BooleanSupplier currentResult) {
        if (!soundEnabled) return;

        PLAYBACK.execute(() -> {
            if (!soundEnabled || !currentResult.getAsBoolean()) return;
            try {
                float sampleRate = 44100;
                int durationMs = 400;
                int numSamples = (int) (sampleRate * durationMs / 1000);
                byte[] buffer = new byte[numSamples];

                // Hợp âm 3 nốt vui tươi (Do - Mi - Sol): 523Hz, 659Hz, 784Hz
                for (int i = 0; i < numSamples; i++) {
                    double time = i / (double) sampleRate;
                    double decay = Math.exp(-time * 4.0);
                    double sample = 0.4 * Math.sin(2.0 * Math.PI * 523.25 * time)
                                  + 0.3 * Math.sin(2.0 * Math.PI * 659.25 * time)
                                  + 0.3 * Math.sin(2.0 * Math.PI * 783.99 * time);
                    buffer[i] = (byte) (sample * decay * 110);
                }

                if (currentResult.getAsBoolean()) {
                    VICTORY_CLIP.play(buffer, sampleRate, () -> soundEnabled && currentResult.getAsBoolean());
                }
            } catch (Exception error) {
                reportFailure("victory", error);
            }
        });
    }

    /** Package-visible to verify cleanup without requiring an audio device. */
    static void playClip(Clip clip, byte[] buffer, float sampleRate) throws Exception {
        playClip(clip, buffer, sampleRate, () -> true);
    }

    static void playClip(Clip clip, byte[] buffer, float sampleRate, BooleanSupplier currentResult) throws Exception {
        playClip(clip, buffer, sampleRate, currentResult, true);
    }

    private static void playClip(Clip clip, byte[] buffer, float sampleRate,
                                 BooleanSupplier currentResult, boolean openAndClose) throws Exception {
        CountDownLatch stopped = new CountDownLatch(1);
        AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
        LineListener listener = event -> {
            if (event.getType() == LineEvent.Type.STOP) stopped.countDown();
        };
        try (AudioInputStream stream = new AudioInputStream(
                new ByteArrayInputStream(buffer), format, buffer.length)) {
            if (!openAndClose) {
                clip.stop();
                clip.setFramePosition(0);
            }
            clip.addLineListener(listener);
            if (openAndClose) clip.open(stream);
            if (!currentResult.getAsBoolean()) return;
            clip.start();
            long durationMs = (long) (1000.0 * buffer.length / sampleRate);
            // A cold Windows device exceeded the former 1-second startup allowance in the probe.
            long deadline = System.nanoTime() + (durationMs + 3000) * 1_000_000L;
            while (currentResult.getAsBoolean() && System.nanoTime() < deadline)
                if (stopped.await(10, TimeUnit.MILLISECONDS)) break;
            if (stopped.getCount() != 0 && currentResult.getAsBoolean())
                throw new LineUnavailableException("Audio playback timed out before STOP");
        } finally {
            if (openAndClose) clip.close();
            else {
                try { clip.stop(); }
                finally { clip.removeLineListener(listener); }
            }
        }
    }

    /** At most one open line per effect. All device calls and waits stay on PLAYBACK. */
    private static final class CachedClip {
        private Clip clip;

        synchronized void play(byte[] buffer, float sampleRate, BooleanSupplier currentResult) throws Exception {
            if (!currentResult.getAsBoolean()) return;
            if (clip == null || !clip.isOpen()) {
                Clip opened = AudioSystem.getClip();
                try {
                    opened.open(new AudioFormat(sampleRate, 8, 1, true, false), buffer, 0, buffer.length);
                    clip = opened;
                } catch (Exception error) {
                    opened.close();
                    throw error;
                }
            }
            try {
                playClip(clip, buffer, sampleRate, currentResult, false);
            } catch (Exception error) {
                clip.close();
                clip = null;
                throw error;
            }
        }
    }

    private static void reportFailure(String event, Exception error) {
        // Called only by the audio worker: report the complete error without blocking the EDT.
        System.err.println("[SOUND] " + event + " playback failed on " + Thread.currentThread().getName());
        error.printStackTrace(System.err);
    }
}
