package ar.emily.adorena.kitchen;

import ar.emily.adorena.config.ReloadableConfiguration;

public record Adorena(
    ReloadableConfiguration config,
    EffectProcessor effectProcessor,
    DamageTracker damageTracker
) {
}
