package ar.emily.adorena.caffeine;

import com.github.benmanes.caffeine.cache.Scheduler;
import io.papermc.paper.util.Tick;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine Scheduler that posts given tasks to the given executor <b>from</b> the server thread,
 * approximating the delay to full tick count.
 */
public final class BukkitScheduler implements Scheduler {

  public static final Scheduler INSTANCE = new BukkitScheduler();

  private final Plugin plugin = JavaPlugin.getProvidingPlugin(BukkitScheduler.class);
  private final org.bukkit.scheduler.BukkitScheduler scheduler = this.plugin.getServer().getScheduler();

  private BukkitScheduler() {
  }

  @Override
  public Future<?> schedule(final Executor executor, final Runnable command, final long delay, final TimeUnit unit) {
    final var future = new FutureTask<Void>(() -> executor.execute(command), null);
    final int ticks = Tick.tick().fromDuration(Duration.ofNanos(unit.toNanos(delay)));
    this.scheduler.runTaskLater(this.plugin, future, ticks);
    return future;
  }
}
