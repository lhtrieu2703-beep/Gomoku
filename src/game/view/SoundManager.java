package game.view;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.util.concurrent.*;

/**
 * Trình quản lý âm thanh cho game Caro:
 * Tự động tổng hợp sóng âm thanh (Audio Synthesis) cho tiếng gõ cờ và tiếng chiến thắng
 * mà không cần file ngoài, đảm bảo 100% chạy mượt trên mọi máy tính.
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
        if (!soundEnabled) return;

        PLAYBACK.execute(() -> {
            if (!soundEnabled) return;
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

                playBuffer(buffer, sampleRate);
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * Phát âm thanh chuông reo vui vẻ khi người chơi chiến thắng.
     */
    public static void playVictorySound() {
        if (!soundEnabled) return;

        PLAYBACK.execute(() -> {
            if (!soundEnabled) return;
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

                playBuffer(buffer, sampleRate);
            } catch (Exception ignored) {
            }
        });
    }

    private static void playBuffer(byte[] buffer, float sampleRate) {
        try {
            playClip(AudioSystem.getClip(), buffer, sampleRate);
        } catch (Exception ignored) {
        }
    }

    /** Package-visible to verify cleanup without requiring an audio device. */
    static void playClip(Clip clip, byte[] buffer, float sampleRate) throws Exception {
        CountDownLatch stopped = new CountDownLatch(1);
        AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
        try (AudioInputStream stream = new AudioInputStream(
                new ByteArrayInputStream(buffer), format, buffer.length)) {
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) stopped.countDown();
            });
            clip.open(stream);
            clip.start();
            long durationMs = (long) (1000.0 * buffer.length / sampleRate);
            stopped.await(durationMs + 1000, TimeUnit.MILLISECONDS);
        } finally {
            clip.close();
        }
    }
}
