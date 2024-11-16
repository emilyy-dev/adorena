package ar.emily.adorena.commands;

import ar.emily.adorena.PEBKACException;
import ar.emily.adorena.config.ReloadableConfiguration;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static ar.emily.adorena.commands.RootCommand.permission;
import static ar.emily.adorena.commands.RootCommand.prefixed;
import static io.papermc.paper.command.brigadier.Commands.literal;

@SuppressWarnings("UnstableApiUsage")
final class ReloadConfigCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger("Adorena");

  static LiteralArgumentBuilder<CommandSourceStack> create(final ReloadableConfiguration config) {
    return literal("reload")
        .requires(permission("adorena.reload"))
        .executes(ctx -> reloadConfig(config, ctx.getSource().getSender()));
  }

  private static int reloadConfig(final ReloadableConfiguration config, final CommandSender source) {
    try {
      config.load();
      source.sendPlainMessage(prefixed("Config reloaded"));
      return 1;
    } catch (final IOException ex) {
      LOGGER.error("Error reloading plugin configuration file", ex);
      source.sendPlainMessage(prefixed("Error reloading the configuration file. Check the console for details."));
      return 0;
    } catch (final PEBKACException ex) {
      LOGGER.error(null, ex);
      source.sendPlainMessage(prefixed("Error reloading the configuration file. Check the console for details."));
      return 0;
    }
  }
}
