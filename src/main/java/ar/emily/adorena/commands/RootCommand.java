package ar.emily.adorena.commands;

import ar.emily.adorena.kitchen.Adorena;
import ar.emily.adorena.PluginConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.function.Predicate;

import static io.papermc.paper.command.brigadier.Commands.literal;

@SuppressWarnings("UnstableApiUsage")
public final class RootCommand {

  public static LiteralArgumentBuilder<CommandSourceStack> create(final Adorena adorena) {
    return literal("adorena")
        .requires(permission("adorena"))
        .executes(ctx -> {
          final CommandSender src = ctx.getSource().getSender();
          src.sendPlainMessage('[' + PluginConstants.STYLIZED_NAME + " - Adorena] - v" + PluginConstants.VERSION);
          return 0;
        }).then(ResetEffectsCommand.create(adorena.effectProcessor()))
        .then(ReloadConfigCommand.create(adorena.config()));
  }

  static Predicate<CommandSourceStack> permission(final String permission) {
    return stack -> stack.getSender().hasPermission(permission);
  }

  static String prefixed(final String message) {
    return '[' + PluginConstants.STYLIZED_NAME + "] " + message;
  }
}
