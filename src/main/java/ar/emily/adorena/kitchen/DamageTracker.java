package ar.emily.adorena.kitchen;

import ar.emily.adorena.config.AppliesToMonsters;
import ar.emily.adorena.config.ReloadableConfiguration;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public final class DamageTracker {

  private final ReloadableConfiguration config;
  private final EffectProcessor effectProcessor;

  // Cache<LivingEntity, LivingEntity> (target -> attacker)
  private final Cache<UUID, UUID> entityLastAttackerCache =
      Caffeine.newBuilder()
          .expireAfterWrite(Duration.ofSeconds(10L))
          .scheduler(Scheduler.systemScheduler())
          .build();

  public DamageTracker(final ReloadableConfiguration config, final EffectProcessor effectProcessor) {
    this.config = config;
    this.effectProcessor = effectProcessor;
  }

  public void recordDamage(final LivingEntity target, final LivingEntity attacker) {
    final boolean targetIsPlayer = target instanceof Player;
    final boolean attackerIsPlayer = attacker instanceof Player;
    if (
        attackerIsPlayer && (targetIsPlayer || this.config.effectOnKill().applyUponKillingMonsters())
        || targetIsPlayer && this.config.effectOnDeath().applyUponDyingToMonsters()
        || targetIsPlayer && this.config.appliesToMonsters() == AppliesToMonsters.PLAYER_KILLS_ONLY
        || this.config.appliesToMonsters() == AppliesToMonsters.ALWAYS
    ) {
      this.entityLastAttackerCache.put(target.getUniqueId(), attacker.getUniqueId());
    }
  }

  public void recordDeath(final LivingEntity target, final DamageSource deathDamageSource) {
    determineKiller(target, deathDamageSource).ifPresent(
        killer -> {
          final boolean targetIsPlayer = target instanceof Player;
          final boolean killerIsPlayer = killer instanceof Player;
          final AppliesToMonsters appliesToMonsters = this.config.appliesToMonsters();
          // don't apply to target if it isn't a player because they aren't coming back lmao
          if (targetIsPlayer && (killerIsPlayer || this.config.effectOnDeath().applyUponDyingToMonsters())) {
            this.effectProcessor.applyEffects(target, EffectProcessor.ApplicationCause.deathForKiller(killer));
          }

          if (
              killerIsPlayer && (targetIsPlayer || this.config.effectOnKill().applyUponKillingMonsters())
              || targetIsPlayer && appliesToMonsters == AppliesToMonsters.PLAYER_KILLS_ONLY
              || appliesToMonsters == AppliesToMonsters.ALWAYS
          ) {
            this.effectProcessor.applyEffects(killer, EffectProcessor.ApplicationCause.KILL);
          }
        }
    );
  }

  private Optional<LivingEntity> determineKiller(final LivingEntity target, final DamageSource deathDamageSource) {
    // simple priority system:
    //  * death causing entity
    //  * else, target killer
    //  * else, last attacker within 10 seconds
    if (deathDamageSource.getCausingEntity() instanceof final LivingEntity killer) {
      return Optional.of(killer);
    } else if (target.getKiller() instanceof final LivingEntity killer) { // goofy ahh null check
      return Optional.of(killer);
    } else {
      return Optional.ofNullable(this.entityLastAttackerCache.getIfPresent(target.getUniqueId()))
          .map(target.getServer()::getEntity)
          .filter(LivingEntity.class::isInstance)
          .map(LivingEntity.class::cast);
    }
  }
}
