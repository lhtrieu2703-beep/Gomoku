package game.view;

/** Pure setup rules: defaults are explicit inputs, with no Swing or current-locale state. */
public final class TwoPlayerSetupValidation {
    private TwoPlayerSetupValidation() {}

    public record Result(String firstName, String secondName, boolean duplicateNames, boolean duplicateAvatars) {
        public boolean canStart() { return !duplicateNames && !duplicateAvatars; }
    }

    public static String effectiveName(String input, String defaultName) {
        String trimmed = input == null ? "" : input.trim();
        return trimmed.isEmpty() ? defaultName : trimmed;
    }

    public static boolean isAvatarLocked(int candidate, int otherPlayerAvatar) {
        return candidate == otherPlayerAvatar;
    }

    /** Retain the second choice unless it conflicts; then choose the first free index. */
    public static int initialSecondAvatar(int firstAvatar, int secondAvatar) {
        if (!isAvatarLocked(secondAvatar, firstAvatar)) return secondAvatar;
        for (int candidate = 0; candidate < 8; candidate++)
            if (!isAvatarLocked(candidate, firstAvatar)) return candidate;
        throw new IllegalStateException("No available avatar");
    }

    public static Result validate(String first, String second, String firstDefault, String secondDefault,
                                  int firstAvatar, int secondAvatar) {
        String firstName = effectiveName(first, firstDefault);
        String secondName = effectiveName(second, secondDefault);
        return new Result(firstName, secondName, firstName.equalsIgnoreCase(secondName),
                isAvatarLocked(firstAvatar, secondAvatar));
    }
}
