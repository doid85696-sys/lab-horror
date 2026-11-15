package dev.horrorlab;

import dev.horrorlab.item.GasMaskItem;
import dev.horrorlab.item.KeycardItem;
import dev.horrorlab.item.MedkitItem;
import dev.horrorlab.item.PistolItem;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModItems {
    public static final Item KEYCARD_1 = reg("keycard_1", new KeycardItem(1, new Item.Settings().maxCount(1)));
    public static final Item KEYCARD_2 = reg("keycard_2", new KeycardItem(2, new Item.Settings().maxCount(1)));
    public static final Item KEYCARD_3 = reg("keycard_3", new KeycardItem(3, new Item.Settings().maxCount(1)));
    public static final Item GAS_MASK = reg("gas_mask", new GasMaskItem(new Item.Settings().maxCount(1)));
    public static final Item PISTOL = reg("pistol", new PistolItem(new Item.Settings().maxCount(1)));
    public static final Item AMMO = reg("ammo", new Item(new Item.Settings()));
    public static final Item MEDKIT = reg("medkit", new MedkitItem(new Item.Settings().maxCount(8)));
    public static final Item MUTANT_SPAWN_EGG = reg("mutant_spawn_egg",
            new SpawnEggItem(ModEntities.MUTANT, 0x2f3f2f, 0x8b0000, new Item.Settings()));

    private static Item reg(String name, Item item) {
        return Registry.register(Registries.ITEM, HorrorLabMod.id(name), item);
    }

    public static void init() {}
}
