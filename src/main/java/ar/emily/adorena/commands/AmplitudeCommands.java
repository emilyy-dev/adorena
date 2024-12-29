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
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.kyori.adventure.text.Component.text;

@SuppressWarnings("UnstableApiUsage")
final class AmplitudeCommands {

  static LiteralArgumentBuilder<CommandSourceStack> create(final EffectProcessor effectProcessor) {
    return literal("amplitude")
        .requires(permission("adorena.amplitude"))
        .then(
            literal("get")
                .requires(permission("adorena.amplitude.get"))
                .executes(ctx -> getSelfAmplitude(ctx.getSource(), effectProcessor))
                .then(
                    argument("target", ArgumentTypes.entity())
                        .requires(permission("adorena.amplitude.get.others"))
                        .executes(ctx -> getEntityAmplitude(ctx, effectProcessor))
                )
        ).then(
            literal("set")
                .requires(permission("adorena.amplitude.set"))
                .then(
                    argument("amplitude", integer())
                        .executes(ctx ->
                            setSelfAmplitude(ctx.getSource(), effectProcessor, getInteger(ctx, "amplitude"))
                        ).then(
                            argument("targets", ArgumentTypes.entities())
                                .requires(permission("adorena.amplitude.set.others"))
                                .executes(ctx -> setTargetsAmplitude(ctx, effectProcessor))
                        )
                )
        )
        ;
  }

  private static int getSelfAmplitude(final CommandSourceStack css, final EffectProcessor effectProcessor) {
    final CommandSender source = css.getSender();
    if (css.getExecutor() instanceof final LivingEntity target) {
      final int amplitude = effectProcessor.getEffectsAmplitude(target);
      if (amplitude == 0) {
        source.sendPlainMessage(prefixed("You are not currently affected by effects"));
      } else {
        source.sendPlainMessage(prefixed("Your current effects amplitude is of " + amplitude));
      }

      return 1;
    } else {
      source.sendPlainMessage(prefixed("A target entity must be provided"));
      return 0;
    }
  }

  private static int getEntityAmplitude(
      final CommandContext<CommandSourceStack> ctx,
      final EffectProcessor effectProcessor
  ) throws CommandSyntaxException {
    final CommandSender source = ctx.getSource().getSender();
    final Entity entity =
        ctx.getArgument("target", EntitySelectorArgumentResolver.class)
            .resolve(ctx.getSource())
            .getFirst();

    if (entity instanceof final LivingEntity target) {
      final int amplitude = effectProcessor.getEffectsAmplitude(target);
      if (amplitude == 0) {
        source.sendMessage(
            text(prefixed(""))
                .append(target.teamDisplayName())
                .append(text(" is not currently affected by effects"))
        );
      } else {
        source.sendMessage(
            text(prefixed(""))
                .append(target.teamDisplayName())
                .append(text("'s current effects amplitude is of " + amplitude))
        );
      }

      return 1;
    } else {
      source.sendPlainMessage(prefixed("The target entity is not able of being affected by effects"));
      return 0;
    }
  }

  private static int setSelfAmplitude(
      final CommandSourceStack css,
      final EffectProcessor effectProcessor,
      final int amplitude
  ) {
    final CommandSender source = css.getSender();
    if (css.getExecutor() instanceof final Player player) {
      effectProcessor.setEffectsAmplitude(player, amplitude);
      source.sendPlainMessage(prefixed("Effects have been reset"));
      return 1;
    } else {
      source.sendPlainMessage(prefixed("A target entity must be provided"));
      return 0;
    }
  }

  private static int setTargetsAmplitude(
      final CommandContext<CommandSourceStack> ctx,
      final EffectProcessor effectProcessor
  ) throws CommandSyntaxException {
    final CommandSender source = ctx.getSource().getSender();
    final EntitySelectorArgumentResolver selector = ctx.getArgument("targets", EntitySelectorArgumentResolver.class);
    final List<Entity> targets = new ArrayList<>(selector.resolve(ctx.getSource()));
    final int amplitude = getInteger(ctx, "amplitude");
    targets.removeIf(entity -> {
      if (entity instanceof final LivingEntity target) {
        effectProcessor.setEffectsAmplitude(target, amplitude);
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
                .append(text(" have been applied"))
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
