package dev.horrorlab;

import dev.horrorlab.block.LabDoorBlock;
import dev.horrorlab.block.ToxicGasBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.block.BlockSoundGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.TallBlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlocks {
    public static final Block LAB_WALL = register("lab_wall", new Block(AbstractBlock.Settings.create()
            .mapColor(MapColor.LIGHT_GRAY).strength(10f, 1200f).requiresTool().sounds(BlockSoundGroup.METAL)));

    public static final Block LAB_FLOOR = register("lab_floor", new Block(AbstractBlock.Settings.create()
            .mapColor(MapColor.GRAY).strength(10f, 1200f).requiresTool().sounds(BlockSoundGroup.METAL)));

    public static final Block LAB_LAMP = register("lab_lamp", new Block(AbstractBlock.Settings.create()
            .mapColor(MapColor.WHITE).strength(2f).luminance(s -> 15).sounds(BlockSoundGroup.GLASS)));

    public static final Block TOXIC_GAS = register("toxic_gas", new ToxicGasBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.LIME).noCollision().nonOpaque().replaceable().dropsNothing()
            .strength(0f).pistonBehavior(PistonBehavior.DESTROY)));

    /** Двери уровней 1..3 (зелёная, синяя, красная). Неразрушимы в выживании. */
    public static final Block[] LAB_DOORS = new Block[3];

    static {
        for (int i = 1; i <= 3; i++) {
            Block door = new LabDoorBlock(i, AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY)
                    .strength(-1f, 3600000f).nonOpaque().sounds(BlockSoundGroup.METAL)
                    .pistonBehavior(PistonBehavior.BLOCK));
            Registry.register(Registries.BLOCK, HorrorLabMod.id("lab_door_" + i), door);
            Registry.register(Registries.ITEM, HorrorLabMod.id("lab_door_" + i),
                    new TallBlockItem(door, new Item.Settings()));
            LAB_DOORS[i - 1] = door;
        }
    }

    private static Block register(String name, Block block) {
        Registry.register(Registries.BLOCK, HorrorLabMod.id(name), block);
        Registry.register(Registries.ITEM, HorrorLabMod.id(name), new BlockItem(block, new Item.Settings()));
        return block;
    }

    public static void init() {}
}
