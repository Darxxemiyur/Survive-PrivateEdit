package com.stereowalker.survive;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.stereowalker.survive.commands.SCommands;
import com.stereowalker.survive.compat.OriginsCompat;
import com.stereowalker.survive.compat.SItemProperties;
import com.stereowalker.survive.config.Config;
import com.stereowalker.survive.config.HygieneConfig;
import com.stereowalker.survive.config.ServerConfig;
import com.stereowalker.survive.config.StaminaConfig;
import com.stereowalker.survive.config.WellbeingConfig;
//import com.stereowalker.survive.core.cauldron.SCauldronInteraction;
import com.stereowalker.survive.json.ArmorJsonHolder;
import com.stereowalker.survive.json.BiomeTemperatureJsonHolder;
import com.stereowalker.survive.json.BlockTemperatureJsonHolder;
import com.stereowalker.survive.json.EntityTemperatureJsonHolder;
import com.stereowalker.survive.json.FoodJsonHolder;
import com.stereowalker.survive.json.PotionJsonHolder;
import com.stereowalker.survive.json.property.BlockPropertyHandlerImpl;
import com.stereowalker.survive.network.protocol.game.ClientboundDataTransferPacket;
import com.stereowalker.survive.network.protocol.game.ClientboundDrinkSoundPacket;
import com.stereowalker.survive.network.protocol.game.ClientboundSurvivalStatsPacket;
import com.stereowalker.survive.network.protocol.game.ServerboundArmorStaminaPacket;
import com.stereowalker.survive.network.protocol.game.ServerboundInteractWithWaterPacket;
import com.stereowalker.survive.network.protocol.game.ServerboundRelaxPacket;
import com.stereowalker.survive.network.protocol.game.ServerboundStaminaExhaustionPacket;
import com.stereowalker.survive.resource.ArmorDataManager;
import com.stereowalker.survive.resource.ItemConsummableDataManager;
import com.stereowalker.survive.resource.PotionDrinkDataManager;
import com.stereowalker.survive.tags.FluidSTags;
import com.stereowalker.survive.tags.ItemSTags;
import com.stereowalker.survive.world.DataMaps;
import com.stereowalker.survive.world.effect.SMobEffects;
import com.stereowalker.survive.world.entity.ai.attributes.SAttributes;
import com.stereowalker.survive.world.item.HygieneItems;
import com.stereowalker.survive.world.item.SItems;
import com.stereowalker.survive.world.item.alchemy.BrewingRecipes;
import com.stereowalker.survive.world.item.alchemy.SPotions;
import com.stereowalker.survive.world.item.crafting.SRecipeSerializer;
import com.stereowalker.survive.world.level.CGameRules;
import com.stereowalker.survive.world.level.block.SBlocks;
//import com.stereowalker.survive.world.level.material.PurifiedWaterFluid;
import com.stereowalker.survive.world.level.material.SFluids;
import com.stereowalker.survive.world.spellcraft.SSpells;
import com.stereowalker.unionlib.client.gui.screens.config.MinecraftModConfigsScreen;
import com.stereowalker.unionlib.config.ConfigBuilder;
import com.stereowalker.unionlib.event.potionfluid.FluidToPotionEvent;
import com.stereowalker.unionlib.mod.IPacketHolder;
import com.stereowalker.unionlib.mod.MinecraftMod;
import com.stereowalker.unionlib.network.PacketRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(value = "survive")
public class Survive extends MinecraftMod implements IPacketHolder {

	//public static final float DEFAULT_TEMP = 37.0F;
	//public static final int PURIFIED_WATER_COLOR = 0x41d3f8;
	public static Map<Potion, List<Fluid>> POTION_FLUID_MAP;
	public static final String MOD_ID = "survive";

	public static final Config CONFIG = new Config();
	public static final StaminaConfig STAMINA_CONFIG = new StaminaConfig();
	public static final HygieneConfig HYGIENE_CONFIG = new HygieneConfig();
	public static final WellbeingConfig WELLBEING_CONFIG = new WellbeingConfig();

	public static boolean isPrimalWinterLoaded;
	public static final ItemConsummableDataManager consummableReloader = new ItemConsummableDataManager();
	public static final PotionDrinkDataManager potionReloader = new PotionDrinkDataManager();
	public static final ArmorDataManager armorReloader = new ArmorDataManager();
	private static Survive instance;

	public static boolean isCombatLoaded() {
		return ModList.get().isLoaded("combat");
	}

	public static boolean isOriginsLoaded() {
		return ModList.get().isLoaded("origins");
	}

	public Survive() {
		super("survive", new ResourceLocation(MOD_ID, "textures/icon.png"), LoadType.BOTH);
		instance = this;
		ConfigBuilder.registerConfig(ServerConfig.class);
		ConfigBuilder.registerConfig(CONFIG);
		ConfigBuilder.registerConfig(HYGIENE_CONFIG);
		ConfigBuilder.registerConfig(WELLBEING_CONFIG);
		ConfigBuilder.registerConfig(STAMINA_CONFIG);
		new FluidSTags();
		new ItemSTags();
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::setup);
		modEventBus.addListener(this::clientRegistries);
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		// MinecraftForge.EVENT_BUS.register(this);
		/*
		MinecraftForge.EVENT_BUS.addListener((Consumer<PotionToFluidEvent>) event -> {
			if (event.getPotion() == SPotions.PURIFIED_WATER) {
				event.setFluid(SFluids.PURIFIED_WATER);
				event.setFlowingFluid(SFluids.FLOWING_PURIFIED_WATER);
			}
		}); 
		MinecraftForge.EVENT_BUS.addListener((Consumer<FluidToPotionEvent.FromStateEvent>) event -> {
			if (event.getFluid().getType() instanceof PurifiedWaterFluid) {
				event.setPotion(SPotions.PURIFIED_WATER);
			}
		});*/
		isPrimalWinterLoaded = ModList.get().isLoaded("primalwinter");
		if (isCombatLoaded()) {
			SSpells.registerAll(modEventBus);
		}
		if (isOriginsLoaded()) {
			OriginsCompat.initOriginsPatcher();
		}
		BlockPropertyHandlerImpl.init();
	}

	public void registerCommands(RegisterCommandsEvent event) {
		SCommands.registerCommands(event.getDispatcher());
	}

	@Override
	public IRegistries getRegistries() {
		return (reg) -> {
			reg.add(SBlocks.class);
			reg.add(SFluids.class);
			reg.add(SItems.class);
			reg.add(HygieneItems.class);
			reg.add(SRecipeSerializer.class);
			reg.add(SAttributes.class);
			reg.add(SMobEffects.class);
		};
	}

	@Override
	public Map<EntityType<? extends LivingEntity>, List<Attribute>> appendAttributesWithoutValues() {
		Map<EntityType<? extends LivingEntity>, List<Attribute>> map = Maps.newHashMap();
		map.put(EntityType.PLAYER,
				Lists.newArrayList(SAttributes.COLD_RESISTANCE, SAttributes.HEAT_RESISTANCE, SAttributes.MAX_STAMINA));
		return map;
	}

	@Override
	public void registerServerboundPackets(SimpleChannel channel) {
		PacketRegistry.registerMessage(channel, 0, ServerboundArmorStaminaPacket.class, (packetBuffer) -> {
			return new ServerboundArmorStaminaPacket(packetBuffer);
		});
		/*
		 * PacketRegistry.registerMessage(channel, 1,
		 * ServerboundThirstMovementPacket.class, (packetBuffer) -> {return new
		 * ServerboundThirstMovementPacket(packetBuffer);});
		 */
		PacketRegistry.registerMessage(channel, 2, ServerboundInteractWithWaterPacket.class, (packetBuffer) -> {
			return new ServerboundInteractWithWaterPacket(packetBuffer);
		});
		PacketRegistry.registerMessage(channel, 3, ServerboundStaminaExhaustionPacket.class, (packetBuffer) -> {
			return new ServerboundStaminaExhaustionPacket(packetBuffer);
		});
		PacketRegistry.registerMessage(channel, 4, ServerboundRelaxPacket.class, (packetBuffer) -> {
			return new ServerboundRelaxPacket(packetBuffer);
		});
	}

	@Override
	public void registerClientboundPackets(SimpleChannel channel) {
		channel.registerMessage(5, ClientboundSurvivalStatsPacket.class, ClientboundSurvivalStatsPacket::encode,
				ClientboundSurvivalStatsPacket::decode, ClientboundSurvivalStatsPacket::handle);
		channel.registerMessage(6, ClientboundDrinkSoundPacket.class, ClientboundDrinkSoundPacket::encode,
				ClientboundDrinkSoundPacket::decode, ClientboundDrinkSoundPacket::handle);
		PacketRegistry.registerMessage(channel, 7, ClientboundDataTransferPacket.class, (packetBuffer) -> {
			return new ClientboundDataTransferPacket(packetBuffer);
		});
	}

	// TODO: FInd Somewhere to put all these
	public static void registerDrinkDataForItem(ResourceLocation location, FoodJsonHolder drinkData) {
		DataMaps.Server.consummableItem.put(location, drinkData);
	}

	public static void registerDrinkDataForPotion(ResourceLocation location, PotionJsonHolder consummableData) {
		DataMaps.Server.potionDrink.put(location, consummableData);
	}

	public static void registerArmorTemperatures(ResourceLocation location, ArmorJsonHolder armorData) {
		DataMaps.Server.armor.put(location, armorData);
	}

	public static void registerBlockTemperatures(ResourceLocation location, BlockTemperatureJsonHolder drinkData) {
		DataMaps.Server.blockTemperature.put(location, drinkData);
	}

	public static void registerEntityTemperatures(ResourceLocation location, EntityTemperatureJsonHolder drinkData) {
		DataMaps.Server.entityTemperature.put(location, drinkData);
	}

	public static void registerBiomeTemperatures(ResourceLocation location, BiomeTemperatureJsonHolder biomeData) {
		DataMaps.Server.biomeTemperature.put(location, biomeData);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Screen getConfigScreen(Minecraft mc, Screen previousScreen) {
		return new MinecraftModConfigsScreen(previousScreen, new TranslatableComponent("gui.survive.config.title"),
				HYGIENE_CONFIG, STAMINA_CONFIG,/*  TEMPERATURE_CONFIG, THIRST_CONFIG, */ WELLBEING_CONFIG, CONFIG);
	}

	public void debug(Object message) {
		if (CONFIG.debugMode)
			getLogger().debug(message);
	}

	@SuppressWarnings("deprecation")
	private void setup(final FMLCommonSetupEvent event) {
		//SCauldronInteraction.bootStrap();
		BrewingRecipes.addBrewingRecipes();
		CGameRules.init();

		for (Item item : ForgeRegistries.ITEMS) {
			if (item.isEdible())
				DataMaps.Server.defaultFood.put(item.getRegistryName(), item.getFoodProperties());
		}
	}

	public void clientRegistries(final FMLClientSetupEvent event) {
		RenderType frendertype = RenderType.translucent();
		//ItemBlockRenderTypes.setRenderLayer(SFluids.PURIFIED_WATER, frendertype);
		//ItemBlockRenderTypes.setRenderLayer(SFluids.FLOWING_PURIFIED_WATER, frendertype);
		GuiHelper.registerOverlays();
		/*SItemProperties.registerAll(); */
	}

	@Override
	public void registerServerRelaodableResources(ReloadListeners reloadListener) {
		reloadListener.listenTo(consummableReloader);
		reloadListener.listenTo(potionReloader);
		reloadListener.listenTo(armorReloader);
		//reloadListener.listenTo(blockReloader);
		//reloadListener.listenTo(biomeReloader);
		//reloadListener.listenTo(entityReloader);
		//reloadListener.listenTo(fluidReloader);
	}

	public static List<String> defaultDimensionMods() {
		List<String> dims = new ArrayList<String>();
		dims.add("minecraft:overworld,0.0");
		dims.add("minecraft:the_nether,0.0");
		dims.add("minecraft:the_end,0.0");
		return dims;
	}

	public static Survive getInstance() {
		return instance;
	}
}
