package dev.minesla.djinn;

/** Shared balance values; no client classes are referenced on a dedicated server. */
public final class RoyalBalance {
    public static final int SWORD_DAMAGE = 10;
    public static final int SWING_TICKS = 18;
    private RoyalBalance() {}
    public static double attackBase(double existing, boolean king) { return king ? 1.0 : existing; }
    public static boolean isGuard(int role) { return role == 2; }
}
