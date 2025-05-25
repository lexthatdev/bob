package com.lexthedev.bob;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;

public class BobEntity extends PathfinderMob {
	
	private int hitCount = 0;
	private boolean isEvil = false;
	private UUID revengeTarget = null;
	private int argumentTimer = -1;
	
    private int talkCooldown = 0;
    
    @Override
    public double getMyRidingOffset() {
        return 1.0D; // adjust this value to lower or raise the name tag
    }
    
    @Override
    public boolean isCustomNameVisible() {
        return true;
    }
    
    protected BobEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCustomName(Component.literal("bob"));  // sets the name to "Bob"
        this.setCustomNameVisible(true);               // makes the name tag always visible
    }
    
    @Override
    public boolean fireImmune() {
        return isEvil;
    }
    
    private void becomeEvil(Player attacker) {
        isEvil = true;
        this.setTarget(attacker); // attack them now
        this.setLastHurtByMob(attacker); // triggers targeting AI
        this.setPersistenceRequired(); // keeps bob around even if far away

        // sound effect (like lightning or boss music cue)
        this.level().playSound(null, this.blockPosition(), ModSounds.SWORD.get(), SoundSource.HOSTILE, 1.0f, 1.0f);
        
        this.getAttribute(Attributes.MAX_HEALTH)
        	.setBaseValue(150);
        
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} // ~1 second
        
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.setWeatherParameters(0, 0, true, true); // optional: thunderstorm
            serverLevel.addFreshEntity(new net.minecraft.world.entity.LightningBolt(
                EntityType.LIGHTNING_BOLT, serverLevel));
            serverLevel.getServer().execute(() -> {
                net.minecraft.world.entity.LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (bolt != null) {
                    bolt.moveTo(this.getX(), this.getY(), this.getZ());
                    serverLevel.addFreshEntity(bolt);
                }
            });
        }
        
        // optional: angry particles or lightning
        ((ServerLevel) this.level()).sendParticles(ParticleTypes.WARPED_SPORE, this.getX(), this.getY() + 1.0, this.getZ(), 10, 0.5, 0.5, 0.5, 0.01);
        
        // speed buff
        this.getAttribute(Attributes.MOVEMENT_SPEED)
            .setBaseValue(0.45); // was 0.25 — now double that
        
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);

        if (!this.level().isClientSide) {
            // play hurt sound
            float pitch = 0.8f + this.random.nextFloat() * 0.4f;
            this.level().playSound(null, this.blockPosition(), ModSounds.BOB_OW.get(), SoundSource.NEUTRAL, 1.0f, pitch);

            // check if player hurt bob
            if (source.getEntity() instanceof Player player) {
                if (!isEvil) {
                    hitCount++;
                    if (hitCount >= 9) {
                        becomeEvil(player);
                    }
                }
            }
        }

        return result;
    }

    public void doDoubleHop() {
        new Thread(() -> {
            try {
                if (this.onGround()) {
                    this.setDeltaMovement(this.getDeltaMovement().x, 0.4, this.getDeltaMovement().z);
                    Thread.sleep(1000); // wait ~0.3 sec
                    this.setDeltaMovement(this.getDeltaMovement().x, 0.4, this.getDeltaMovement().z);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!player.level().isClientSide) {
            if (player.getItemInHand(hand).is(Items.BREAD)) {
                // take 1 bread
                if (!player.getAbilities().instabuild) {
                    player.getItemInHand(hand).shrink(1);
                }

                // play voiceline
                float pitch = 0.8f + this.random.nextFloat() * 0.4f;
                this.level().playSound(null, this.blockPosition(), ModSounds.BOB_THANKS.get(), SoundSource.NEUTRAL, 1.0f, pitch);

                // heart particles
                ((ServerLevel) this.level()).sendParticles(
                    ParticleTypes.HEART,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    5, 0.5, 0.5, 0.5, 0.1
                );

                // heal him
                this.heal(4.0f);

                // make him hop twice
                this.doDoubleHop();

                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    
    @Override
    public void die(DamageSource cause) {
        super.die(cause);

        if (!this.level().isClientSide && !isInArgument) {
            this.level().playSound(null, this.blockPosition(), ModSounds.BOB_DEATH.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        }
    }
    
    
    
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.25, Ingredient.of(Items.BREAD), false));
        this.goalSelector.addGoal(1, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));

        // evil attack goal
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true, player -> isEvil));
        this.goalSelector.addGoal(0, new MeleeAttackGoal(this, 1.4, false));
    }
    
    private static class BobArgumentStep {
        final BobEntity speaker;
        final SoundEvent sound;
        final int delayAfter; // ticks to wait after sound ends before next

        BobArgumentStep(BobEntity speaker, SoundEvent sound, int delayAfter) {
            this.speaker = speaker;
            this.sound = sound;
            this.delayAfter = delayAfter;
        }
    }

    // fields in BobEntity:
    private List<BobArgumentStep> argumentSteps = null;
    private int argumentStepTimer = 0;
    private boolean isInArgument = false;
    private BobEntity argumentTarget = null;
    private int currentStepIndex = 0;

    private void startArgument(BobEntity otherBob) {
        this.argumentTarget = otherBob;
        this.argumentSteps = List.of(
            new BobArgumentStep(this, ModSounds.BOB_SEESBOB.get(), 2),
            new BobArgumentStep(otherBob, ModSounds.BOB_NOTREAL.get(), 2),
            new BobArgumentStep(this, ModSounds.BOB_IAM.get(), 2),
            new BobArgumentStep(otherBob, ModSounds.BOB_ANGRY.get(), 2),
            new BobArgumentStep(this, ModSounds.BOB_IAMTHE.get(), 2) // ends in boom
        );
        this.argumentStepTimer = 0;
        this.currentStepIndex = 0;
        this.isInArgument = true;
        otherBob.isInArgument = true;
        otherBob.argumentTarget = this;
        otherBob.argumentSteps = this.argumentSteps;
        otherBob.argumentStepTimer = 0;
        otherBob.currentStepIndex = 0;
    }

    private int explosionTimer = -1; // new field at top of class

    private void handleArgumentSequence() {
        if (!isInArgument || argumentSteps == null || currentStepIndex >= argumentSteps.size()) return;

        // stop moving + stare
        if (argumentTarget != null) {
            this.setDeltaMovement(0, 0, 0);
            argumentTarget.setDeltaMovement(0, 0, 0);
            lookAtEachOther(this, argumentTarget);
        }

        // handle delayed explosion
        if (explosionTimer >= 0) {
            explosionTimer--;
            if (explosionTimer <= 0) {
                Level level = this.level();
                level.explode(null, this.getX(), this.getY(), this.getZ(), 2.5F, Level.ExplosionInteraction.NONE);
                if (argumentTarget != null) {
                    level.explode(null, argumentTarget.getX(), argumentTarget.getY(), argumentTarget.getZ(), 2.5F, Level.ExplosionInteraction.NONE);
                    argumentTarget.discard();
                    argumentTarget.isInArgument = false;
                }
                this.discard();
                this.isInArgument = false;
            }
            return; // skip further logic while waiting to explode
        }

        // normal voice line logic
        BobArgumentStep step = argumentSteps.get(currentStepIndex);

        if (argumentStepTimer == 0) {
            step.speaker.playSound(step.sound, 1.0F, 1.0F);

            if (currentStepIndex == argumentSteps.size() - 1) {
                // start countdown for boom (0.5s = 10 ticks)
                explosionTimer = 10;
                return;
            }

            argumentStepTimer = getSoundDurationTicks(step.sound) + step.delayAfter;
        } else {
            argumentStepTimer--;
            if (argumentStepTimer <= 0) {
                currentStepIndex++;
            }
        }
    }

    private void lookAtEachOther(BobEntity bob1, BobEntity bob2) {
        if (bob1 == null || bob2 == null) return;

        bob1.getLookControl().setLookAt(bob2, 10.0F, bob1.getMaxHeadXRot());
        bob2.getLookControl().setLookAt(bob1, 10.0F, bob2.getMaxHeadXRot());
    }

    // estimate sound duration in ticks (1 second = 20 ticks)
    private int getSoundDurationTicks(SoundEvent sound) {
        if (sound == ModSounds.BOB_SEESBOB.get()) return 20;
        if (sound == ModSounds.BOB_NOTREAL.get()) return 30;
        if (sound == ModSounds.BOB_IAM.get()) return 20;
        if (sound == ModSounds.BOB_ANGRY.get()) return 25;
        if (sound == ModSounds.BOB_IAMTHE.get()) return 30;
        return 20; // default guess
    }


    @Override
    public void tick() {
        super.tick();

        // talk randomly
        if (!this.level().isClientSide && !isInArgument && talkCooldown-- <= 0) {
            float pitch = 0.8f + this.random.nextFloat() * 0.4f;
            this.level().playSound(null, this.blockPosition(), ModSounds.BOB_HELLO.get(), SoundSource.NEUTRAL, 1.0f, pitch);
            talkCooldown = 100 + this.random.nextInt(100);
        }

        // look for nearby bob to start argument every second
        if (!level().isClientSide && !isInArgument && tickCount % 20 == 0) {
            List<BobEntity> nearbyBobs = level().getEntitiesOfClass(BobEntity.class, this.getBoundingBox().inflate(5.0D), 
                e -> e != this && !e.isInArgument && !e.getPersistentData().getBoolean("bobArgued"));

            if (!nearbyBobs.isEmpty()) {
                BobEntity other = nearbyBobs.get(0);

                // only one bob starts the argument (lowest ID wins)
                if (this.getId() < other.getId()) {
                    this.getPersistentData().putBoolean("bobArgued", true);
                    other.getPersistentData().putBoolean("bobArgued", true);

                    startArgument(other);
                }
            }
        }

        // handle argument if in progress
        if (isInArgument) {
            handleArgumentSequence();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25);
    }
}
