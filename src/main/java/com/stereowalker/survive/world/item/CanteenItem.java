package com.stereowalker.survive.world.item;

/*
public class CanteenItem extends Item {

	public CanteenItem(Properties properties) {
		super(properties);
	}

	public static CompoundTag canteenTag(int drinks) {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("DrinksLeft", drinks);
		return nbt;
	}

	public static ItemStack addToCanteen(ItemStack stack, int drinks, Potion potion) {
		stack.setTag(canteenTag(drinks));
		PotionUtils.setPotion(stack, potion);
		return stack;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public ItemStack getDefaultInstance() {
		return addToCanteen(super.getDefaultInstance(), Survive.THIRST_CONFIG.canteen_fill_amount, Potions.WATER);
	}

	public int getDrinksLeft(ItemStack stack) {
		return stack.getOrCreateTag().getInt("DrinksLeft");
	}

	public void setDrinksLeft(ItemStack stack, int drinks) {
		stack.getOrCreateTag().putInt("DrinksLeft", Mth.clamp(drinks, 0, Survive.THIRST_CONFIG.canteen_fill_amount));
	}

	public void decrementDrinks(ItemStack stack) {
		setDrinksLeft(stack, getDrinksLeft(stack) - 1);
	}

	// * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
	// * the Item before the action is complete.
	
	@Override
	public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
		Player player = pEntityLiving instanceof Player ? (Player)pEntityLiving : null;
		if (player instanceof ServerPlayer) {
			CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, pStack);
		}

		if (!pLevel.isClientSide) {
			for(MobEffectInstance mobeffectinstance : PotionUtils.getMobEffects(pStack)) {
				if (mobeffectinstance.getEffect().isInstantenous()) {
					mobeffectinstance.getEffect().applyInstantenousEffect(player, player, pEntityLiving, mobeffectinstance.getAmplifier(), 1.0D);
				} else {
					pEntityLiving.addEffect(new MobEffectInstance(mobeffectinstance));
				}
			}
		}

		if (player != null) {
			player.awardStat(Stats.ITEM_USED.get(this));
		}

		if (getDrinksLeft(pStack) <= 1) {
			if (player == null || !player.getAbilities().instabuild) {
				pStack.shrink(1);
			}

			if (player == null || !player.getAbilities().instabuild) {
				if (pStack.isEmpty()) {
					return new ItemStack(SItems.CANTEEN);
				}

				if (player != null) {
					player.getInventory().add(new ItemStack(SItems.CANTEEN));
				}
			}
		}

		if (getDrinksLeft(pStack) > 1) {
			if (player == null || !player.getAbilities().instabuild) {
				decrementDrinks(pStack);
			}
		}

		pLevel.gameEvent(pEntityLiving, GameEvent.DRINKING_FINISH, pEntityLiving.eyeBlockPosition());
		return pStack;
	}

	
	 // How long it takes to use or consume an item
	
	@Override
	public int getUseDuration(ItemStack stack) {
		return 32;
	}

	
	 // returns the action that specifies what animation to play when the items is being used
	 
	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.DRINK;
	}

	
	 // Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
	 // {@link #onItemUse}.
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
		ItemStack stack = pPlayer.getItemInHand(pHand);
		if (Survive.POTION_FLUID_MAP.containsKey(PotionUtils.getPotion(stack))) {
			if (getDrinksLeft(stack) < Survive.THIRST_CONFIG.canteen_fill_amount) {
				HitResult raytraceresult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.SOURCE_ONLY);
				BlockPos blockpos = ((BlockHitResult)raytraceresult).getBlockPos();
				if (pLevel.getFluidState(blockpos).is(FluidTags.WATER) && Survive.POTION_FLUID_MAP.get(PotionUtils.getPotion(stack)).contains(pLevel.getFluidState(blockpos).getType())) {
					setDrinksLeft(stack, Survive.THIRST_CONFIG.canteen_fill_amount);
				}
			}
			pPlayer.startUsingItem(pHand);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, pPlayer.getItemInHand(pHand));
		} else {
			return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
		}
	}

	@Override
	public void appendHoverText(ItemStack pStack, Level worldIn, List<Component> pTooltip, TooltipFlag flagIn) {
		pTooltip.add(new TranslatableComponent("tooltip.drinks_left").append(": "+getDrinksLeft(pStack)).withStyle(ChatFormatting.AQUA));
		if (Survive.POTION_FLUID_MAP.containsKey(PotionUtils.getPotion(pStack)))
			pTooltip.add(new TranslatableComponent(PotionUtils.getPotion(pStack).getName(this.getDescriptionId()+".effect.")).withStyle(ChatFormatting.YELLOW));
		else
			pTooltip.add(new TranslatableComponent(PotionUtils.getPotion(pStack).getName("item.minecraft.potion.effect.")).withStyle(ChatFormatting.GOLD));
		PotionUtils.addPotionTooltip(pStack, pTooltip, 1.0F);
	}


	@Override
	public boolean isFoil(ItemStack pStack) {
		return super.isFoil(pStack) || !PotionUtils.getMobEffects(pStack).isEmpty();
	}

	
	 // returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
	
	 @Override
	public void fillItemCategory(CreativeModeTab pGroup, NonNullList<ItemStack> pItems) {
		if (this.allowdedIn(pGroup)) {
			for(Potion potion : Registry.POTION) {
				if (potion != Potions.EMPTY) {
					pItems.add(addToCanteen(new ItemStack(this), Survive.THIRST_CONFIG.canteen_fill_amount, potion));
				}
			}
		}

	}

}
*/