package ar.emily.adorena.kitchen;

import ar.emily.adorena.config.ReloadableConfiguration;

/** Holder for stuff to be DI'd */
public record Adorena(
    ReloadableConfiguration config,
    EffectProcessor effectProcessor,
    DamageTracker damageTracker
) {
}
