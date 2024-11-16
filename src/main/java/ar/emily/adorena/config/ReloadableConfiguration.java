package ar.emily.adorena.config;

import ar.emily.adorena.PEBKACException;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Layer on top of ConfigurationV{version} to handle hot-reloading */
public final class ReloadableConfiguration {

  public static final YAMLMapper YAML_MAPPER =
      YAMLMapper.builder()
          .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
          .disable(YAMLGenerator.Feature.USE_NATIVE_OBJECT_ID, YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)
          .propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
          .build();

  private static final String CONFIG_NOT_LOADED = """
      Configuration failed to load and hasn't been reloaded. \
      Once the configuration file is fixed, run `/adorena reload`""";

  private final File configFile;
  private final List<Runnable> reloadListeners;
  private ConfigurationV1 config;

  public ReloadableConfiguration(final File configFile) {
    this.configFile = configFile;
    this.reloadListeners = new ArrayList<>();
  }

  public void load() throws IOException, PEBKACException {
    final ConfigurationV1 config = YAML_MAPPER.readValue(this.configFile, AbstractConfiguration.class).asLatest();
    final EffectKind killEffect = config.effectOnKill().effect();
    final EffectKind deathEffect = config.effectOnDeath().effect();
    if (killEffect == deathEffect && deathEffect != EffectKind.NONE) {
      throw new PEBKACException("effect-on-death.effect must not be the same as effect-on-kill.effect. Configuration will not be loaded.");
    }

    PEBKACException ex = null;
    for (final NamespacedKey attributeKey : config.attributeScaleMultipliers().keySet()) {
      if (Registry.ATTRIBUTE.get(attributeKey) == null) {
        final var invalidAttributeEx = new PEBKACException(attributeKey.asString() + " is not a valid attribute");
        if (ex == null) {
          ex = invalidAttributeEx;
        } else {
          ex.addSuppressed(invalidAttributeEx);
        }
      }
    }

    if (ex != null) {
      throw ex;
    }

    this.config = config;
    this.reloadListeners.forEach(Runnable::run);
  }

  public void attachReloadListener(final Runnable reloadListener) {
    this.reloadListeners.add(reloadListener);
  }

  public double growthRate() {
    return assertLoaded().growthRate();
  }

  public KillEffectSettings effectOnKill() {
    return assertLoaded().effectOnKill();
  }

  public DeathEffectSettings effectOnDeath() {
    return assertLoaded().effectOnDeath();
  }

  public boolean clearEffectWithMilk() {
    return assertLoaded().clearEffectWithMilk();
  }

  public AppliesToMonsters appliesToMonsters() {
    return assertLoaded().appliesToMonsters();
  }

  public Map<NamespacedKey, Double> attributeScaleMultipliers() {
    return assertLoaded().attributeScaleMultipliers();
  }

  private ConfigurationV1 assertLoaded() {
    return Objects.requireNonNull(this.config, CONFIG_NOT_LOADED);
  }
}
