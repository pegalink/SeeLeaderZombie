package net.robotic.seeleaderzombie.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.robotic.seeleaderzombie.SeeLeaderZombieMod;
import net.robotic.seeleaderzombie.config.ModConfig;
import net.robotic.seeleaderzombie.core.LeaderZombies;

/**
 * In-game control surface for the Fabric build.
 *
 * <p>NeoForge gets this for free through its config screen and config-reload event; Fabric has
 * no equivalent, so without these commands a config change would only apply to leaders that
 * spawn afterwards. Every mutation re-syncs the already-loaded entities.
 */
public final class SeeLeaderZombieCommand {

    /** Same level vanilla requires for /gamerule - operators only. */
    private static final int PERMISSION_LEVEL = 2;

    private SeeLeaderZombieCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(LeaderZombies.MOD_ID)
                .requires(source -> source.hasPermission(PERMISSION_LEVEL));

        root.then(Commands.literal("reload").executes(SeeLeaderZombieCommand::reload));
        root.then(Commands.literal("status").executes(SeeLeaderZombieCommand::status));

        LiteralArgumentBuilder<CommandSourceStack> set = Commands.literal("set");
        for (ModConfig.Option option : ModConfig.Option.values()) {
            set.then(Commands.literal(option.key())
                    .then(Commands.argument("value", BoolArgumentType.bool())
                            .executes(context -> set(context, option))));
        }
        root.then(set);

        dispatcher.register(root);
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!ModConfig.load()) {
            source.sendFailure(Component.literal(
                    "Could not read " + ModConfig.path() + " - see the log for details."));
            return 0;
        }

        int affected = resync(source);
        source.sendSuccess(() -> Component.literal(
                "SeeLeaderZombie config reloaded (" + affected + " leader zombie(s) updated)."), true);
        return affected;
    }

    private static int set(CommandContext<CommandSourceStack> context, ModConfig.Option option) {
        boolean value = BoolArgumentType.getBool(context, "value");
        ModConfig.set(option, value);

        CommandSourceStack source = context.getSource();
        int affected = resync(source);
        source.sendSuccess(() -> Component.literal(
                option.key() + " set to " + value + " (" + affected + " leader zombie(s) updated)."), true);
        return affected;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        StringBuilder message = new StringBuilder("SeeLeaderZombie config:");
        for (ModConfig.Option option : ModConfig.Option.values()) {
            message.append("\n  ").append(option.key()).append(" = ").append(ModConfig.get(option));
        }
        message.append("\n  file: ").append(ModConfig.path());

        context.getSource().sendSuccess(() -> Component.literal(message.toString()), false);
        return 1;
    }

    /** Applies the current settings to everything already loaded on this server. */
    private static int resync(CommandSourceStack source) {
        return LeaderZombies.resyncLoadedEntities(
                source.getServer(), ModConfig.INSTANCE, SeeLeaderZombieMod.LOGGER);
    }
}
