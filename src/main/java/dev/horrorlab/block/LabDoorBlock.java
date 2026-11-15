package dev.horrorlab.block;

import dev.horrorlab.item.KeycardItem;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** Дверь, которая открывается только карточкой нужного (или более высокого) уровня в руке. */
public class LabDoorBlock extends DoorBlock {
    private final int level;

    public LabDoorBlock(int level, Settings settings) {
        super(BlockSetType.IRON, settings);
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    private boolean hasAccess(PlayerEntity player) {
        for (Hand hand : Hand.values()) {
            ItemStack s = player.getStackInHand(hand);
            if (s.getItem() instanceof KeycardItem card && card.getLevel() >= level) return true;
        }
        return false;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!hasAccess(player)) {
            if (!world.isClient) {
                player.sendMessage(Text.translatable("message.horrorlab.need_key", level), true);
                world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1f, 0.7f);
            }
            return ActionResult.SUCCESS;
        }
        if (!world.isClient) {
            boolean open = state.get(OPEN);
            this.setOpen(player, world, state, pos, !open);
            if (!open) {
                world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.6f, 1.6f);
                world.scheduleBlockTick(pos, this, 100); // дверь сама закроется через 5 секунд
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(OPEN)) {
            this.setOpen(null, world, state, pos, false);
        }
    }
}
