package ar.emily.adorena.config;

public record KillEffectSettings(
    EffectKind effect,
    int maximumTimes,
    long cooldownTicks,
    boolean applyUponKillingMonsters,
    boolean resetOnPvpDeath,
    boolean resetOnPveDeath
) implements BasicEffectSettings {
}
