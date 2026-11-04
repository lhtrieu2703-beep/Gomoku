package game.view;

/** Empty stored names remain implicit so the default follows the current language. */
public record PlayerProfile(String name, int avatarIndex) {
    public PlayerProfile {
        name = AppSettings.normalizePlayerName(name);
        avatarIndex = avatarIndex >= 0 && avatarIndex < 8 ? avatarIndex : 0;
    }
    public String displayName() { return name.isEmpty() ? Lang.defaultPlayerName() : name; }
    public String displayName(boolean secondPlayer) {
        return name.isEmpty() && secondPlayer ? Lang.defaultPlayerTwo() : displayName();
    }
}
