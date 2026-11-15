package dev.horrorlab.world;

import dev.horrorlab.ModBlocks;
import dev.horrorlab.ModEntities;
import dev.horrorlab.ModItems;
import dev.horrorlab.entity.MutantEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import java.util.*;

/**
 * Процедурная генерация лаборатории: сетка 5x5 комнат, лабиринт из коридоров,
 * двери уровней 1-3 по мере удаления от входа, карточки в сундуках, мутанты и газовые комнаты.
 */
public final class LabGenerator {
    public static final int GRID = 5;
    public static final int S = 8;   // шаг клетки (7 внутри + 1 общая стена)
    public static final int H = 6;   // пол y0, проход y1..y4, потолок y5
    public static final int ENTRANCE_OFFSET_X = (GRID / 2) * S + 4;

    private static final int FLAGS = Block.NOTIFY_LISTENERS | Block.FORCE_STATE;

    private record Cell(int x, int z) {}

    private LabGenerator() {}

    public static void build(ServerWorld w, BlockPos o, Random rnd) {
        final int size = GRID * S;
        BlockState wall = ModBlocks.LAB_WALL.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        BlockState floor = ModBlocks.LAB_FLOOR.getDefaultState();

        // 1. Сплошной блок стен
        for (int x = 0; x <= size; x++)
            for (int y = 0; y < H; y++)
                for (int z = 0; z <= size; z++)
                    set(w, o.add(x, y, z), wall);

        // 2. Выдалбливаем комнаты
        for (int cx = 0; cx < GRID; cx++) {
            for (int cz = 0; cz < GRID; cz++) {
                for (int dx = 1; dx < S; dx++) {
                    for (int dz = 1; dz < S; dz++) {
                        set(w, o.add(cx * S + dx, 0, cz * S + dz), floor);
                        for (int y = 1; y <= 4; y++) set(w, o.add(cx * S + dx, y, cz * S + dz), air);
                    }
                }
                set(w, o.add(cx * S + 4, 4, cz * S + 4), ModBlocks.LAB_LAMP.getDefaultState());
            }
        }

        // 3. Лабиринт (DFS) + глубина клеток
        int[][] depth = new int[GRID][GRID];
        for (int[] row : depth) Arrays.fill(row, -1);
        Cell start = new Cell(GRID / 2, 0);
        depth[start.x()][start.z()] = 0;
        Deque<Cell> stack = new ArrayDeque<>();
        stack.push(start);
        List<Cell[]> edges = new ArrayList<>();
        Set<String> linked = new HashSet<>();
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!stack.isEmpty()) {
            Cell c = stack.peek();
            List<Cell> opts = new ArrayList<>();
            for (int[] d : dirs) {
                int nx = c.x() + d[0], nz = c.z() + d[1];
                if (nx >= 0 && nz >= 0 && nx < GRID && nz < GRID && depth[nx][nz] < 0) opts.add(new Cell(nx, nz));
            }
            if (opts.isEmpty()) {
                stack.pop();
                continue;
            }
            Cell n = opts.get(rnd.nextInt(opts.size()));
            depth[n.x()][n.z()] = depth[c.x()][c.z()] + 1;
            edges.add(new Cell[]{c, n});
            linked.add(key(c, n));
            linked.add(key(n, c));
            stack.push(n);
        }

        // Несколько дополнительных проходов (петли), чтобы было не так линейно
        for (int i = 0; i < 8; i++) {
            Cell a = new Cell(rnd.nextInt(GRID), rnd.nextInt(GRID));
            int[] d = dirs[rnd.nextInt(4)];
            int nx = a.x() + d[0], nz = a.z() + d[1];
            if (nx < 0 || nz < 0 || nx >= GRID || nz >= GRID) continue;
            Cell b = new Cell(nx, nz);
            if (linked.contains(key(a, b))) continue;
            linked.add(key(a, b));
            linked.add(key(b, a));
            edges.add(new Cell[]{a, b});
        }

        int maxDepth = 0;
        Cell finalCell = start;
        for (int x = 0; x < GRID; x++)
            for (int z = 0; z < GRID; z++)
                if (depth[x][z] > maxDepth) {
                    maxDepth = depth[x][z];
                    finalCell = new Cell(x, z);
                }

        // 4. Двери: уровень = зона более дальней из двух комнат
        for (Cell[] e : edges) {
            int lvl = Math.max(zone(depth[e[0].x()][e[0].z()], maxDepth), zone(depth[e[1].x()][e[1].z()], maxDepth));
            connect(w, o, e[0], e[1], lvl);
        }

        // Вход с улицы (без двери)
        BlockPos entrance = o.add(start.x() * S + 4, 1, 0);
        set(w, entrance, air);
        set(w, entrance.up(), air);

        // 5. Наполнение
        List<Cell> all = new ArrayList<>();
        for (int x = 0; x < GRID; x++) for (int z = 0; z < GRID; z++) all.add(new Cell(x, z));
        Set<Cell> special = new HashSet<>();
        special.add(start);
        special.add(finalCell);

        // Стартовый сундук: пистолет, патроны, противогаз, аптечка
        chest(w, o.add(start.x() * S + 1, 1, start.z() * S + 1), List.of(
                new ItemStack(ModItems.PISTOL),
                new ItemStack(ModItems.AMMO, 24),
                new ItemStack(ModItems.GAS_MASK),
                new ItemStack(ModItems.MEDKIT, 2)));

        // Карточки доступа: k-я лежит в зоне, доступной без карточки k
        net.minecraft.item.Item[] keycards = {ModItems.KEYCARD_1, ModItems.KEYCARD_2, ModItems.KEYCARD_3};
        for (int k = 1; k <= 3; k++) {
            final int kk = k;
            List<Cell> cand = new ArrayList<>();
            for (Cell c : all)
                if (!special.contains(c) && zone(depth[c.x()][c.z()], maxDepth) == kk - 1) cand.add(c);
            if (cand.isEmpty())
                for (Cell c : all)
                    if (!special.contains(c) && zone(depth[c.x()][c.z()], maxDepth) < kk) cand.add(c);
            if (cand.isEmpty()) cand.add(start);
            Cell pick = cand.get(rnd.nextInt(cand.size()));
            special.add(pick);
            chest(w, o.add(pick.x() * S + S - 1, 1, pick.z() * S + S - 1), List.of(
                    new ItemStack(keycards[k - 1]),
                    new ItemStack(ModItems.AMMO, 8 + rnd.nextInt(8)),
                    new ItemStack(ModItems.MEDKIT, 1)));
            // Охрана ключа
            spawnMutant(w, o, pick, rnd);
        }

        // Финальная комната: сокровища и охрана
        chest(w, o.add(finalCell.x() * S + 4, 1, finalCell.z() * S + 4), List.of(
                new ItemStack(Items.DIAMOND, 8),
                new ItemStack(Items.GOLDEN_APPLE, 3),
                new ItemStack(Items.NETHERITE_SCRAP, 2),
                new ItemStack(ModItems.AMMO, 16)));
        spawnMutant(w, o, finalCell, rnd);
        spawnMutant(w, o, finalCell, rnd);

        // Остальные комнаты: газ и мутанты
        for (Cell c : all) {
            if (special.contains(c)) continue;
            int z = zone(depth[c.x()][c.z()], maxDepth);
            if (z >= 1 && rnd.nextFloat() < 0.3f) {
                fillGas(w, o, c);
            }
            int mobs = switch (z) {
                case 0 -> rnd.nextFloat() < 0.25f ? 1 : 0;
                case 1 -> rnd.nextFloat() < 0.5f ? 1 : 0;
                case 2 -> 1 + rnd.nextInt(2);
                default -> 2;
            };
            for (int i = 0; i < mobs; i++) spawnMutant(w, o, c, rnd);
        }
    }

    private static int zone(int depth, int maxDepth) {
        if (depth <= maxDepth / 4) return 0;
        if (depth <= maxDepth / 2) return 1;
        if (depth <= (maxDepth * 3) / 4) return 2;
        return 3;
    }

    private static String key(Cell a, Cell b) {
        return a.x() + "," + a.z() + ">" + b.x() + "," + b.z();
    }

    private static void connect(ServerWorld w, BlockPos o, Cell a, Cell b, int level) {
        int x, z;
        Direction facing;
        if (a.x() == b.x()) {
            x = a.x() * S + 4;
            z = Math.max(a.z(), b.z()) * S;
            facing = Direction.NORTH;
        } else {
            x = Math.max(a.x(), b.x()) * S;
            z = a.z() * S + 4;
            facing = Direction.EAST;
        }
        BlockPos low = o.add(x, 1, z);
        if (level == 0) {
            set(w, low, Blocks.AIR.getDefaultState());
            set(w, low.up(), Blocks.AIR.getDefaultState());
        } else {
            BlockState d = ModBlocks.LAB_DOORS[level - 1].getDefaultState().with(DoorBlock.FACING, facing);
            set(w, low, d.with(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            set(w, low.up(), d.with(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        }
    }

    private static void fillGas(ServerWorld w, BlockPos o, Cell c) {
        BlockState gas = ModBlocks.TOXIC_GAS.getDefaultState();
        for (int dx = 1; dx < S; dx++)
            for (int dz = 1; dz < S; dz++)
                for (int y = 1; y <= 3; y++)
                    set(w, o.add(c.x() * S + dx, y, c.z() * S + dz), gas);
    }

    private static void chest(ServerWorld w, BlockPos pos, List<ItemStack> items) {
        set(w, pos, Blocks.CHEST.getDefaultState());
        if (w.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            for (int i = 0; i < items.size(); i++) chest.setStack(i, items.get(i).copy());
            chest.markDirty();
        }
    }

    private static void spawnMutant(ServerWorld w, BlockPos o, Cell c, Random rnd) {
        MutantEntity m = ModEntities.MUTANT.create(w);
        if (m == null) return;
        double x = o.getX() + c.x() * S + 4 + (rnd.nextDouble() * 4 - 2) + 0.5;
        double z = o.getZ() + c.z() * S + 4 + (rnd.nextDouble() * 4 - 2) + 0.5;
        m.refreshPositionAndAngles(x, o.getY() + 1, z, rnd.nextFloat() * 360f, 0f);
        m.setPersistent();
        w.spawnEntity(m);
    }

    private static void set(ServerWorld w, BlockPos pos, BlockState state) {
        w.setBlockState(pos, state, FLAGS);
    }
}
