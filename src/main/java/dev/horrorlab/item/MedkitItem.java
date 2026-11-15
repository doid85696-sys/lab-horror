package dev.horrorlab.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class MedkitItem extends Item {
    public MedkitItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (player.getHealth() >= player.getMaxHealth()) return TypedActionResult.fail(stack);
        if (!world.isClient) {
            player.heal(10f);
            world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_HONEY_BOTTLE_DRINK,
                    SoundCategory.PLAYERS, 1f, 1f);
            stack.decrementUnlessCreative(1, player);
        }
        player.getItemCooldownManager().set(this, 40);
        return TypedActionResult.success(stack, world.isClient());
    }
}
