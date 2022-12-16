package com.stereowalker.survive.resource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stereowalker.survive.Survive;
import com.stereowalker.survive.json.ArmorJsonHolder;
import com.stereowalker.survive.world.DataMaps;
import com.stereowalker.unionlib.resource.IResourceReloadListener;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Maps marker type to texture.
 * @author Stereowalker
 */
public class ArmorDataManager implements IResourceReloadListener<Map<ResourceLocation, ArmorJsonHolder>> {
	@Override
	public CompletableFuture<Map<ResourceLocation, ArmorJsonHolder>> load(ResourceManager manager, ProfilerFiller profiler, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<ResourceLocation, ArmorJsonHolder> drinkMap = new HashMap<>();

			for (ResourceLocation id : manager.listResources("survive_modifiers/armors", (s) -> s.endsWith(".json"))) {
				ResourceLocation drinkId = new ResourceLocation(
						id.getNamespace(),
						id.getPath().replace("survive_modifiers/armors/", "").replace(".json", "")
						);

				if (ForgeRegistries.ITEMS.containsKey(drinkId)) {
					try {
						Resource resource = manager.getResource(id);
						try (InputStream stream = resource.getInputStream(); 
								InputStreamReader reader = new InputStreamReader(stream)) {
							
							JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
							ArmorJsonHolder drinkData = new ArmorJsonHolder(drinkId, object);
							Survive.getInstance().getLogger().info("Found armor modifier for the item {}", drinkId);
							
							drinkMap.put(drinkId, drinkData);
						}
					} catch (Exception e) {
						Survive.getInstance().getLogger().warn("Error reading the armor modifier for the item {}!", drinkId, e);
					}
				} else {
					Survive.getInstance().getLogger().warn("No such armor exists with the item id {}!", drinkId);
				}
			}

			return drinkMap;
		});
	}

	@Override
	public CompletableFuture<Void> apply(Map<ResourceLocation, ArmorJsonHolder> data, ResourceManager manager, ProfilerFiller profiler, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			DataMaps.Server.syncedToClient = false;
			for (ResourceLocation drinkId : data.keySet()) {
				Survive.registerArmorTemperatures(drinkId, data.get(drinkId));
			}
		});
	}
}
