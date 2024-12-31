package ar.emily.adorena;

import ar.emily.adorena.commands.RootCommand;
import ar.emily.adorena.config.ReloadableConfiguration;
import ar.emily.adorena.kitchen.Adorena;
import ar.emily.adorena.kitchen.DamageTracker;
import ar.emily.adorena.kitchen.EffectProcessor;
import com.google.common.base.Suppliers;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

// TODO:
//  * kiss Emilia more

/**
 * Entry point for the working bits of the plugin, listens to damage and other events
 * and calls respective actions in damage tracker/effect processor
 */
@SuppressWarnings("UnstableApiUsage")
public final class AdorenaPlugin extends JavaPlugin implements Listener {

  private static final Logger LOGGER = LoggerFactory.getLogger("Adorena");

  private static final String UNABLE_TO_LOAD_CONFIG = """
      Unable to read plugin configuration file. \
      The plugin will still load, but the configuration file must be fixed first, then run `/adorena reload`""";

  private final ReloadableConfiguration config;
  private final EffectProcessor effectProcessor;
  private final DamageTracker damageTracker;
  private final Adorena adorena;
  private final Supplier<ConsumeEffect.ClearAllStatusEffects> clearEffectsEffect;
  private final RandomGenerator randomSource;

  public AdorenaPlugin() {
    this.config = new ReloadableConfiguration(new File(getDataFolder(), "config.yml"));
    this.effectProcessor = new EffectProcessor(this.config);
    this.damageTracker = new DamageTracker(this.config, this.effectProcessor);
    this.adorena = new Adorena(this.config, this.effectProcessor, this.damageTracker);
    this.clearEffectsEffect = Suppliers.memoize(ConsumeEffect::clearAllStatusEffects);

    final ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
    try {
      this.randomSource = RandomGenerator.getDefault();
    } finally {
      Thread.currentThread().setContextClassLoader(ctxClassLoader);
    }

    this.config.attachReloadListener(this::reloadAllEffects);
  }

  @Override
  public void onLoad() {
    try {
      saveDefaultConfig();
      this.config.load();
    } catch (final IOException | PEBKACException ex) {
      LOGGER.error(UNABLE_TO_LOAD_CONFIG, ex);
      // don't rethrow exception, let user use the plugin reload command instead of fully disabling the plugin
    }
  }

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
    getLifecycleManager().registerEventHandler(
        LifecycleEvents.COMMANDS, event -> event.registrar().register(RootCommand.create(this.adorena).build())
    );
  }

  private void reloadAllEffects() {
    getServer().getWorlds().stream()
        .map(World::getLivingEntities)
        .flatMap(Collection::stream)
        .forEach(this.effectProcessor::reloadEffects);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void on(final PlayerJoinEvent event) {
    this.effectProcessor.loadEffects(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void on(final EntitiesLoadEvent event) {
    for (final Entity entity : event.getEntities()) {
      if (entity instanceof final LivingEntity target) {
        this.effectProcessor.loadEffects(target);
      }
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void on(final EntityDamageEvent event) {
    if (
        event.getEntity() instanceof final LivingEntity target
        && event.getDamageSource().getCausingEntity() instanceof final LivingEntity attacker
    ) {
      this.damageTracker.recordDamage(target, attacker);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void on(final EntityDeathEvent event) {
    this.damageTracker.recordDeath(event.getEntity(), event.getDamageSource());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void on(final PlayerItemConsumeEvent event) {
    if (!this.config.clearEffectWithMilk() && !this.config.suspiciousStewAppliesEffectsRandomly()) {
      return;
    }

    final ItemStack item = event.getItem();
    final Player player = event.getPlayer();

    // order here mimics vanilla's behavior if you have an item with SUSPICIOUS_STEW_EFFECTS & CLEAR_ALL_STATUS_EFFECTS

    if (this.config.suspiciousStewAppliesEffectsRandomly() && item.hasData(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS)) {
      final int randomAmplitude =
          this.randomSource.nextInt(
              -this.config.effectOnDeath().maximumTimes(),
              this.config.effectOnKill().maximumTimes()
          );
      this.effectProcessor.setEffectsAmplitude(player, randomAmplitude);
    }

    // TODO: rename setting?
    if (this.config.clearEffectWithMilk()) {
      // TODO: version-check, just check if item.type == MILK pre-data component API.. or maybe don't do that
      final Consumable consumable = item.getData(DataComponentTypes.CONSUMABLE);
      if (consumable != null && consumable.consumeEffects().contains(this.clearEffectsEffect.get())) {
        this.effectProcessor.resetEffects(player);
      }
    }
  }
}
