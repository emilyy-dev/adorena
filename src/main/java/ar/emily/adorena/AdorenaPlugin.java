package ar.emily.adorena;

import ar.emily.adorena.commands.RootCommand;
import ar.emily.adorena.config.ReloadableConfiguration;
import ar.emily.adorena.kitchen.Adorena;
import ar.emily.adorena.kitchen.EffectProcessor;
import ar.emily.adorena.kitchen.DamageTracker;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

// 24:00 hs
// TODO:
//  * kiss Emilia more

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

  public AdorenaPlugin() {
    this.config = new ReloadableConfiguration(new File(getDataFolder(), "config.yml"));
    this.effectProcessor = new EffectProcessor(this.config);
    this.damageTracker = new DamageTracker(this.config, this.effectProcessor);
    this.adorena = new Adorena(this.config, this.effectProcessor, this.damageTracker);
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
    if (this.config.clearEffectWithMilk() && event.getItem().getType() == Material.MILK_BUCKET) {
      this.effectProcessor.resetEffects(event.getPlayer());
    }
  }
}
