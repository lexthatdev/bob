package com.lexthedev.bob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = BobMod.MODID)
public class ModEntities {
    // register all entities to the "bob" mod id
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BobMod.MODID);

    // register all items (we'll only use this for the spawn egg here)
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, BobMod.MODID);

    // the actual Bob entity
    public static final RegistryObject<EntityType<BobEntity>> BOB = ENTITY_TYPES.register("bob",
        () -> EntityType.Builder.of(BobEntity::new, MobCategory.CREATURE)
            .sized(1.2f, 3.6f)
            .build("bob"));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}