package ar.emily.adorena.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.bukkit.NamespacedKey;

import java.util.Map;
import java.util.Objects;

@JsonTypeName("1")
public record ConfigurationV1(
    double growthRate,
    KillEffectSettings effectOnKill,
    DeathEffectSettings effectOnDeath,
    boolean clearEffectWithMilk,
    boolean suspiciousStewAppliesEffectsRandomly,
    AppliesToMonsters appliesToMonsters,
    Map<NamespacedKey, Double> attributeScaleMultipliers
) implements AbstractConfiguration {

  public ConfigurationV1 {
    attributeScaleMultipliers = Map.copyOf(Objects.requireNonNullElse(attributeScaleMultipliers, Map.of()));
  }

  @Override
  public ConfigurationV1 asLatest() {
    return this;
  }
}
