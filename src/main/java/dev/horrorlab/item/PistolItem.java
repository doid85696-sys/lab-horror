package dev.horrorlab.item;

import dev.horrorlab.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

/** Пистолет: мгновенный выстрел (hitscan), тратит патроны (ammo). */
public class PistolItem extends Item {
    private static final double RANGE = 40.0;
    private static final float DAMAGE = 9.0f;

    public PistolItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (player.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);

        boolean creative = player.isCreative();
        if (!creative && !hasAmmo(player)) {
            if (!world.isClient) {
                world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_DISPENSER_FAIL,
                        SoundCategory.PLAYERS, 1f, 1.5f);
            }
            return TypedActionResult.fail(stack);
        }
        if (!world.isClient) {
            if (!creative) consumeAmmo(player);
            shoot((ServerWorld) world, player);
        }
        player.getItemCooldownManager().set(this, 10);
        return TypedActionResult.success(stack, world.isClient());
    }

    private static boolean hasAmmo(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(ModItems.AMMO)) return true;
        }
        return false;
    }

    private static void consumeAmmo(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack s = player.getInventory().getStack(i);
            if (s.isOf(ModItems.AMMO)) {
                s.decrement(1);
                return;
            }
        }
    }

    private static void shoot(ServerWorld world, PlayerEntity player) {
        Vec3d start = player.getEyePos();
        Vec3d dir = player.getRotationVec(1.0f);
        Vec3d end = start.add(dir.multiply(RANGE));

        BlockHitResult blockHit = world.raycast(new RaycastContext(start, end,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
        Vec3d stop = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getPos();

        EntityHitResult hit = ProjectileUtil.raycast(player, start, stop,
                player.getBoundingBox().stretch(dir.multiply(RANGE)).expand(1.0),
                e -> !e.isSpectator() && e.canHit(), RANGE * RANGE);
        if (hit != null) {
            Entity target = hit.getEntity();
            target.damage(world.getDamageSources().playerAttack(player), DAMAGE);
            stop = hit.getPos();
        }

        double dist = start.distanceTo(stop);
        for (double d = 1.0; d < dist; d += 1.5) {
            Vec3d p = start.add(dir.multiply(d));
            world.spawnParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        world.spawnParticles(ParticleTypes.SMOKE, stop.x, stop.y, stop.z, 4, 0.05, 0.05, 0.05, 0.01);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST,
                SoundCategory.PLAYERS, 1.2f, 1.6f);
    }
}
