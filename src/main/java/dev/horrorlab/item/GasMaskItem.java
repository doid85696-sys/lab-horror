package dev.horrorlab.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Противогаз: надевается в слот шлема (ПКМ или перетаскиванием). */
public class GasMaskItem extends Item implements Equipment {
    public GasMaskItem(Settings settings) {
        super(settings);
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return this.equipAndSwap(this, world, user, hand);
    }

    public static boolean isWearing(LivingEntity entity) {
        return entity.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof GasMaskItem;
    }
}
