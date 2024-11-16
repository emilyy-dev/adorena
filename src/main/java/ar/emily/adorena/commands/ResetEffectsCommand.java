package ar.emily.adorena.commands;

import ar.emily.adorena.kitchen.EffectProcessor;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.text.PaperComponents;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ar.emily.adorena.commands.RootCommand.permission;
import static ar.emily.adorena.commands.RootCommand.prefixed;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.kyori.adventure.text.Component.text;

@SuppressWarnings("UnstableApiUsage")
final class ResetEffectsCommand {

  static LiteralArgumentBuilder<CommandSourceStack> create(final EffectProcessor effectProcessor) {
    return literal("reset")
        .requires(permission("adorena.reset"))
        .executes(ctx -> resetSelf(ctx.getSource().getSender(), effectProcessor))
        .then(argument("targets", ArgumentTypes.entities()).executes(ctx -> resetTargets(ctx, effectProcessor)));
  }

  private static int resetSelf(final CommandSender source, final EffectProcessor effectProcessor) {
    if (source instanceof final Player player) {
      effectProcessor.resetEffects(player);
      source.sendPlainMessage(prefixed("Effects have been reset"));
      return 1;
    } else {
      source.sendPlainMessage(prefixed("A target entity must be provided"));
      return 0;
    }
  }

  private static int resetTargets(
      final CommandContext<CommandSourceStack> ctx,
      final EffectProcessor effectProcessor
  ) throws CommandSyntaxException {
    final CommandSender source = ctx.getSource().getSender();
    final EntitySelectorArgumentResolver selector = ctx.getArgument("targets", EntitySelectorArgumentResolver.class);
    final List<Entity> targets = new ArrayList<>(selector.resolve(ctx.getSource()));
    targets.removeIf(entity -> {
      if (entity instanceof final LivingEntity target) {
        effectProcessor.resetEffects(target);
        return false;
      } else {
        return true;
      }
    });

    if (targets.isEmpty()) {
      source.sendPlainMessage(prefixed("No target entity is able of being affected by effects"));
    } else {
      try {
        source.sendMessage(
            text(prefixed("Effects for "))
                .append(PaperComponents.resolveWithContext(getSelectorFromCommand(ctx), source, null))
                .append(text(" have been reset"))
        );
      } catch (final IOException ex) {
        throw (CommandSyntaxException) ex.getCause();
      }
    }

    return targets.size();
  }

  private static Component getSelectorFromCommand(final CommandContext<CommandSourceStack> ctx) {
    return ctx.getNodes().stream()
        .filter(node -> node.getNode().getName().equals("targets"))
        .map(ParsedCommandNode::getRange)
        .map(range -> range.get(ctx.getInput()))
        .<Component>map(Component::selector)
        .findFirst()
        .orElse(text("target entities"));
  }
}
