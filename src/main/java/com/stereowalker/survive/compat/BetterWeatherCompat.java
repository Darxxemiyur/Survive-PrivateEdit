package com.stereowalker.survive.compat;

import com.stereowalker.survive.world.seasons.Season;
import com.stereowalker.survive.world.seasons.Seasons;

import corgitaco.betterweather.api.season.Season.Key;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BetterWeatherCompat {

	public static boolean snowsHere(World world, BlockPos pos) {
		if (world.getDimensionKey() == World.OVERWORLD && world.func_242406_i(pos).isPresent()) {
			return corgitaco.betterweather.api.season.Season.getSeason(world).getKey() == Key.WINTER;
		}
		return false;
	}

	public static Season modifyTemperatureBySeason(World world, BlockPos pos) {
		if (world.getDimensionKey() == World.OVERWORLD && world.func_242406_i(pos) != null && world.func_242406_i(pos).isPresent()) {
			switch (corgitaco.betterweather.api.season.Season.getSeason(world).getKey()) {
			case WINTER:
				switch (corgitaco.betterweather.api.season.Season.getSeason(world).getPhase()) {
				case START: return Seasons.WINTER_BEGIN;
				case MID: return Seasons.WINTER_MIDST;
				case END: return Seasons.WINTER_CLOSE;
				}

			case SPRING:
				switch (corgitaco.betterweather.api.season.Season.getSeason(world).getPhase()) {
				case START: return Seasons.SPRING_BEGIN;
				case MID: return Seasons.SPRING_MIDST;
				case END: return Seasons.SPRING_CLOSE;
				}

			case SUMMER:
				switch (corgitaco.betterweather.api.season.Season.getSeason(world).getPhase()) {
				case START: return Seasons.SUMMER_BEGIN;
				case MID: return Seasons.SUMMER_MIDST;
				case END: return Seasons.SUMMER_CLOSE;
				}

			case AUTUMN:
				switch (corgitaco.betterweather.api.season.Season.getSeason(world).getPhase()) {
				case START: return Seasons.AUTUMN_BEGIN;
				case MID: return Seasons.AUTUMN_MIDST;
				case END: return Seasons.AUTUMN_CLOSE;
				}
			}
		}
		return Seasons.NONE;
	}
}
