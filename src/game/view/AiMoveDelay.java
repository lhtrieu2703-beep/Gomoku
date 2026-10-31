package game.view;

/** Presentation timing only; engine depth, evaluation and benchmark timing are untouched. */
public final class AiMoveDelay {
    public static final int DEFAULT_MINIMUM_MS=800;
    private AiMoveDelay() {}

    public static int remainingMillis(int minimumMillis,long elapsedMillis) {
        if(minimumMillis<0||elapsedMillis<0) throw new IllegalArgumentException("Delay and elapsed time must be nonnegative");
        return elapsedMillis>=minimumMillis?0:minimumMillis-(int)elapsedMillis;
    }
}
