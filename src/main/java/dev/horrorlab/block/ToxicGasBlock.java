package dev.horrorlab.block;

import dev.horrorlab.entity.MutantEntity;
import dev.horrorlab.item.GasMaskItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/** Ядовитый газ: наносит урон и тошноту всем, кто без противогаза. */
public class ToxicGasBlock extends Block {
    public ToxicGasBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient || !(entity instanceof LivingEntity living)) return;
        if (living instanceof MutantEntity || GasMaskItem.isWearing(living)) return;
        living.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
        living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 0));
        if (world.getTime() % 20 == 0) {
            living.damage(world.getDamageSources().magic(), 1.5f);
        }
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    protected boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return stateFrom.isOf(this);
    }
}
