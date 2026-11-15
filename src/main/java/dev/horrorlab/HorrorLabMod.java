package dev.horrorlab;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class HorrorLabMod implements ModInitializer {
    public static final String MOD_ID = "horrorlab";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.init();
        ModItems.init();
        ModEntities.init();
        ModItemGroup.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> LabCommand.register(dispatcher));
        ServerTickEvents.END_WORLD_TICK.register(HorrorLabMod::ambient);
    }

    /** Жуткий ambient: стук сердца, когда игрок идёт по полу лаборатории. */
    private static void ambient(ServerWorld world) {
        if (world.getTime() % 200 != 0) return;
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (world.getBlockState(p.getBlockPos().down()).isOf(ModBlocks.LAB_FLOOR)
                    && world.getRandom().nextFloat() < 0.3f) {
                world.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_WARDEN_HEARTBEAT,
                        SoundCategory.AMBIENT, 0.8f, 1.0f);
            }
        }
    }
}
