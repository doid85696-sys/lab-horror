package dev.horrorlab;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.horrorlab.world.LabGenerator;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class LabCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("horrorlab")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.literal("build").executes(ctx -> build(ctx.getSource()))));
    }

    private static int build(ServerCommandSource src) throws CommandSyntaxException {
        ServerPlayerEntity player = src.getPlayerOrThrow();
        BlockPos p = player.getBlockPos();
        // Вход в лабораторию окажется в 3 блоках впереди (на юг) от игрока.
        BlockPos origin = new BlockPos(p.getX() - LabGenerator.ENTRANCE_OFFSET_X, p.getY() - 1, p.getZ() + 3);
        LabGenerator.build(src.getWorld(), origin, src.getWorld().getRandom());
        src.sendFeedback(() -> Text.translatable("command.horrorlab.built",
                origin.getX(), origin.getY(), origin.getZ()), true);
        return 1;
    }
}
