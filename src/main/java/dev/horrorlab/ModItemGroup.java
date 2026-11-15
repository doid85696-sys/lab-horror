package dev.horrorlab;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroup {
    public static final ItemGroup GROUP = Registry.register(Registries.ITEM_GROUP, HorrorLabMod.id("main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.KEYCARD_3))
                    .displayName(Text.translatable("itemGroup.horrorlab.main"))
                    .entries((ctx, e) -> {
                        e.add(ModItems.KEYCARD_1);
                        e.add(ModItems.KEYCARD_2);
                        e.add(ModItems.KEYCARD_3);
                        e.add(ModItems.GAS_MASK);
                        e.add(ModItems.PISTOL);
                        e.add(ModItems.AMMO);
                        e.add(ModItems.MEDKIT);
                        e.add(ModItems.MUTANT_SPAWN_EGG);
                        e.add(ModBlocks.LAB_WALL);
                        e.add(ModBlocks.LAB_FLOOR);
                        e.add(ModBlocks.LAB_LAMP);
                        e.add(ModBlocks.TOXIC_GAS);
                        for (var d : ModBlocks.LAB_DOORS) e.add(d);
                    }).build());

    public static void init() {}
}
