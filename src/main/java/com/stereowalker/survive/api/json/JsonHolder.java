package com.stereowalker.survive.api.json;

import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundTag;


/**
 * @author Stereowalker
 *
 */
public interface JsonHolder {
	public String getworkingOn();
	
	public void setWorkingOn(String member);
	
	public default float workOnFloat(String member, JsonObject object) {
		float f = 0;
		setWorkingOn(member);
		f = object.get(member).getAsFloat();
		stopWorking();
		return f;
	}
	
	public default void stopWorking() {
		setWorkingOn("NOTHING");
	}
	
	public default boolean hasMemberAndIsPrimitive(String member, JsonObject object) {
		return object.has(member) && object.get(member).isJsonPrimitive();
	}
	
	public default boolean hasMemberAndIsObject(String member, JsonObject object) {
		return object.has(member) && object.get(member).isJsonObject();
	}
	
	public default boolean hasMemberAndIsJsonArray(String member, JsonObject object) {
		return object.has(member) && object.get(member).isJsonArray();
	}
	
	public CompoundTag serialize();
}
