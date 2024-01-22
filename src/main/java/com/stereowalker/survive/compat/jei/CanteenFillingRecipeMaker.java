package com.stereowalker.survive.compat.jei;

/* 
public final class CanteenFillingRecipeMaker {
	public static List<CraftingRecipe> createRecipes(IStackHelper stackHelper) {
		String group = "survive.fill.canteen";

		return Registry.POTION.stream()
			.<CraftingRecipe>map(potion -> {				
				ItemStack canteenStack = new ItemStack(SItems.CANTEEN);
				Ingredient canteenIngredient = Ingredient.of(canteenStack);
				
				ItemStack input = PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
				ItemStack output = CanteenItem.addToCanteen(new ItemStack(SItems.FILLED_CANTEEN), 3, potion);
				Ingredient potionIngredient = Ingredient.of(input);
				NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY,
						canteenIngredient, potionIngredient, potionIngredient, potionIngredient
				);
				ResourceLocation id = new ResourceLocation(Survive.MOD_ID, "survive.fill.canteen." + output.getDescriptionId());
				return new ShapelessRecipe(id, group, output, inputs);
			})
			.toList();
	}

	private CanteenFillingRecipeMaker() {

	}
}
 */