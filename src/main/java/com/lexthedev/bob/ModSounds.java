package com.lexthedev.bob;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "bob");

    public static final RegistryObject<SoundEvent> BOB_HELLO = SOUNDS.register("bob_hello",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("bob", "bob_hello")));
    
    public static final RegistryObject<SoundEvent> BOB_OW = SOUNDS.register("bob_ow",
    	    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("bob", "bob_ow")));
    
    public static final RegistryObject<SoundEvent> BOB_DEATH = SOUNDS.register("bob_death",
    	    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("bob", "bob_death")));
    
    public static final RegistryObject<SoundEvent> BOB_THANKS = SOUNDS.register("bob_thanks",
    	    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("bob", "bob_thanks")));

    public static final RegistryObject<SoundEvent> SWORD = SOUNDS.register("bob_sword",
    	    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("bob", "bob_sword")));
    
    public static final RegistryObject<SoundEvent> BOB_SEESBOB = SOUNDS.register("bob_seesbob",
    	    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("bob", "bob_seesbob")));
    
    public static final RegistryObject<SoundEvent> BOB_NOTREAL = SOUNDS.register("bob_notreal",
    	    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("bob", "bob_notreal")));
    
    public static final RegistryObject<SoundEvent> BOB_IAM = SOUNDS.register("bob_iam",
    	    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("bob", "bob_iam")));
    
    public static final RegistryObject<SoundEvent> BOB_ANGRY = SOUNDS.register("bob_makingmemad",
    	    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("bob", "bob_makingmemad")));
    
    public static final RegistryObject<SoundEvent> BOB_IAMTHE = SOUNDS.register("bob_iamthe",
    	    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("bob", "bob_iamthe")));

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}