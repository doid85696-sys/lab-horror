package dev.horrorlab;

import dev.horrorlab.entity.MutantEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModEntities {
    public static final EntityType<MutantEntity> MUTANT = Registry.register(Registries.ENTITY_TYPE,
            HorrorLabMod.id("mutant"),
            EntityType.Builder.create(MutantEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.7f, 2.1f).maxTrackingRange(10).build("mutant"));

    public static void init() {
        FabricDefaultAttributeRegistry.register(MUTANT, MutantEntity.createAttributes());
    }
}
