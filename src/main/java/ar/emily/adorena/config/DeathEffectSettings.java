package ar.emily.adorena.config;

public record DeathEffectSettings(
    EffectKind effect,
    int maximumTimes,
    long cooldownTicks,
    boolean applyUponDyingToMonsters
) implements BasicEffectSettings {
}
