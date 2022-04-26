package com.stereowalker.survive.needs;

import com.stereowalker.survive.Survive;
import com.stereowalker.survive.core.SurviveEntityStats;
import com.stereowalker.survive.core.WeightHandler;
import com.stereowalker.survive.network.protocol.game.ServerboundArmorStaminaPacket;
import com.stereowalker.survive.network.protocol.game.ServerboundRelaxPacket;
import com.stereowalker.survive.network.protocol.game.ServerboundStaminaExhaustionPacket;
import com.stereowalker.survive.world.DataMaps;
import com.stereowalker.survive.world.entity.ai.attributes.SAttributes;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class StaminaData extends SurviveData {
	private int energyLevel;
	private int energyReserveLevel;
	private float energyExhaustionLevel;
	private int energyTimer;
	@SuppressWarnings("unused")
	private int prevEnergyLevel;
	private int maxStamina;

	public StaminaData(double maxStamina) {
		this.energyReserveLevel = 6;
		this.energyLevel = Mth.floor(maxStamina);
		this.prevEnergyLevel = Mth.floor(maxStamina);
	}

	/**
	 * Add water stats.
	 */
	public void relax(int energyLevelIn, double maxStamina) {
		int remaining = 0;;
		remaining = (this.energyReserveLevel + energyLevelIn) - 6;
		this.energyReserveLevel = Math.min(energyLevelIn + this.energyReserveLevel, 6);
		if (remaining > 0) {
			this.energyLevel = Math.min(remaining + this.energyLevel, Mth.floor(maxStamina));
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void clientTick(AbstractClientPlayer player) {
		if (player.isPassenger() && player.tickCount%400 == 399) {
			if (player.getVehicle().getDeltaMovement().x == 0 && player.getVehicle().getDeltaMovement().z == 0)
				new ServerboundRelaxPacket(1).send();
			else
				new ServerboundStaminaExhaustionPacket(0.0312F).send();
		}
		if (player.tickCount%90 == 89) {
			if (player.level.getDifficulty() != Difficulty.PEACEFUL) {
				new ServerboundArmorStaminaPacket().send();
			}
		}
	}

	/**
	 * Handles the stamina game logic.
	 */
	public void tick(Player player) {
		this.maxStamina = Mth.floor(player.getAttributeValue(SAttributes.MAX_STAMINA));
		Difficulty difficulty = player.level.getDifficulty();
		this.prevEnergyLevel = this.energyLevel;
		if (this.energyExhaustionLevel > 10.0F) {
			this.energyExhaustionLevel -= 10.0F;
			if (difficulty != Difficulty.PEACEFUL) {
				if (this.energyLevel > 0) {
					this.energyLevel = Math.max(this.energyLevel - 1, 0);
				} else if (this.energyReserveLevel > 0) {
					this.energyReserveLevel = Math.max(this.energyReserveLevel - 1, 0);
				}
			}
		}

		boolean flag = player.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
		if (flag && this.energyReserveLevel > 6 && player.isHurt() && this.energyLevel >= 19) {
			++this.energyTimer;
			if (this.energyTimer >= 10) {
				player.heal(2.0F);
				this.addExhaustion(6.0F);
				this.energyTimer = 0;
			}
		} else if (flag && this.energyReserveLevel > 6 && player.isHurt() && this.energyLevel >= 18) {
			++this.energyTimer;
			if (this.energyTimer >= 80) {
				player.heal(1.0F);
				this.addExhaustion(6.0F);
				this.energyTimer = 0;
			}
		} else if (this.energyLevel <= 0 && this.energyReserveLevel <= 0) {
			++this.energyTimer;
			if (this.energyTimer >= 20) {
				player.hurt(SDamageSource.OVERWORK, 3.0F);
				//				if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD || player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL) {
				//				}

				this.energyTimer = 0;
			}
		} else {
			this.energyTimer = 0;
		}
		//Moved from the event. Find a way to mesh all this with what is above you
		if (player.isSleeping() && player.tickCount%20 == 19) {
			this.relax(1, this.maxStamina);
		}
		if ((Survive.STAMINA_CONFIG.stamina_recovery_ticks == 0 || player.tickCount%Survive.STAMINA_CONFIG.stamina_recovery_ticks == Survive.STAMINA_CONFIG.stamina_recovery_ticks-1) && this.isTired()) {
			if (Survive.CONFIG.nutrition_enabled) {
				NutritionData nutritionStats = SurviveEntityStats.getNutritionStats(player);
				if (nutritionStats.getCarbLevel() >= 2) {
					this.relax(1, player.getAttributeValue(SAttributes.MAX_STAMINA));
					nutritionStats.removeCarbs(2);
				}
				nutritionStats.save(player);
			} else {
				if (player.getFoodData().getFoodLevel() > 15 && WeightHandler.getTotalArmorWeight(player)/Survive.STAMINA_CONFIG.max_weight < 1.0F) {
					this.relax(1, this.maxStamina);
					player.getFoodData().addExhaustion(1.0F);
				}
			}
		}
		if (difficulty == Difficulty.PEACEFUL && player.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
			if (this.isTired() && player.tickCount % 10 == 0) {
				this.setEnergyLevel(this.getEnergyLevel() + 1);
			}
		}
	}

	/**
	 * Reads the water data for the player.
	 */
	public void read(CompoundTag compound) {
		if (compound.contains("energyLevel", 99)) {
			this.maxStamina = compound.getInt("maxStamina");
			this.energyLevel = compound.getInt("energyLevel");
			this.energyTimer = compound.getInt("energyTickTimer");
			this.energyReserveLevel = compound.getInt("energyReserveLevel");
			this.energyExhaustionLevel = compound.getFloat("energyExhaustionLevel");
		}

	}

	/**
	 * Writes the water data for the player.
	 */
	public void write(CompoundTag compound) {
		compound.putInt("maxStamina", this.maxStamina);
		compound.putInt("energyLevel", this.energyLevel);
		compound.putInt("energyTickTimer", this.energyTimer);
		compound.putInt("energyReserveLevel", this.energyReserveLevel);
		compound.putFloat("energyExhaustionLevel", this.energyExhaustionLevel);
	}

	/**
	 * Get the player's stamina level.
	 */
	public int getEnergyLevel() {
		return this.energyLevel;
	}

	/**
	 * Get whether the player must drink water.
	 */
	public boolean isTired() {
		return this.energyLevel < maxStamina;
	}

	public boolean isExhausted() {
		return this.energyLevel <= 0;
	}

	/**
	 * adds input to waterExhaustionLevel to a max of 40
	 */
	private void addExhaustion(float exhaustion) {
		this.energyExhaustionLevel = Math.min(this.energyExhaustionLevel + exhaustion, 40.0F);
	}

	/**
	 * increases exhaustion level by supplied amount
	 */
	public void addExhaustion(Player player, float exhaustion, String reason) {
		if (!player.getAbilities().invulnerable) {
			if (!player.level.isClientSide) {
				//				System.out.println("Exhause for "+reason);
				this.addExhaustion(exhaustion);
				this.save(player);
			}

		}
	}

	/**
	 * Get the player's water hydration level.
	 */
	public int getReserveLevel() {
		return this.energyReserveLevel;
	}

	public void setEnergyLevel(int energyLevelIn) {
		this.energyLevel = energyLevelIn;
	}

	@OnlyIn(Dist.CLIENT)
	public void setEnergyReserveLevel(int energyReserveLevelIn) {
		this.energyReserveLevel = energyReserveLevelIn;
	}

	@Override
	public void save(LivingEntity player) {
		SurviveEntityStats.setStaminaStats(player, this);
	}

	@Override
	public boolean shouldTick() {
		return Survive.STAMINA_CONFIG.enabled;
	}

	/////-----------EVENTS-----------/////

	@SubscribeEvent
	public static void clickItem(PlayerInteractEvent.RightClickItem clickItem) {
		if(!clickItem.isCanceled() && clickItem.getPlayer() instanceof Player && clickItem.getCancellationResult().consumesAction()) {

		}
	}

	@SubscribeEvent
	public static void rightClickEmpty(PlayerInteractEvent.RightClickEmpty clickItem) {
		if(!clickItem.isCanceled() && clickItem.getPlayer() instanceof LocalPlayer && clickItem.getCancellationResult().consumesAction()) {
			new ServerboundStaminaExhaustionPacket(0.3125F).send();
		}
	}

	@SubscribeEvent
	public static void leftClickEmpty(PlayerInteractEvent.LeftClickEmpty clickItem) {
		if(!clickItem.isCanceled() && clickItem.getPlayer() instanceof LocalPlayer && clickItem.getCancellationResult().consumesAction()) {
			new ServerboundStaminaExhaustionPacket(0.3125F).send();
		}
	}

	@SubscribeEvent
	public static void replenishEnergyOnSleep(SleepFinishedTimeEvent event) {
		for (Player player : event.getWorld().players()) {
			StaminaData energyStats = SurviveEntityStats.getEnergyStats(player);
			energyStats.relax(energyStats.maxStamina, player.getAttributeValue(SAttributes.MAX_STAMINA));
			SurviveEntityStats.setStaminaStats(player, energyStats);
		}
	}

	@SubscribeEvent
	public static void eatFood(LivingEntityUseItemEvent.Finish event) {
		if (event.getResultStack().isEdible() && DataMaps.Server.consummableItem.containsKey(event.getItem().getItem().getRegistryName())) {
			if (event.getEntityLiving() != null && !event.getEntityLiving().level.isClientSide && event.getEntityLiving() instanceof ServerPlayer) {
				ServerPlayer player = (ServerPlayer)event.getEntityLiving();
				StaminaData energyStats = SurviveEntityStats.getEnergyStats(player);
				energyStats.relax(DataMaps.Server.consummableItem.get(event.getItem().getItem().getRegistryName()).getEnergyAmount(), player.getAttributeValue(SAttributes.MAX_STAMINA));
				SurviveEntityStats.setStaminaStats(player, energyStats);
			}
		}
	}
}
