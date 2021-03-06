package org.terpo.waterworks.init;

import org.terpo.waterworks.api.constants.WaterworksReference;
import org.terpo.waterworks.api.constants.WaterworksRegistryNames;
import org.terpo.waterworks.item.crafting.AntiRainRocketRecipe;
import org.terpo.waterworks.item.crafting.RainRocketRecipe;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.registries.IForgeRegistry;

public class InitRecipes {

	public static void register(IForgeRegistry<IRecipeSerializer<?>> registry) {
		final SpecialRecipeSerializer<AntiRainRocketRecipe> specialRecipeFireworkAntiRain = new SpecialRecipeSerializer<>(
				AntiRainRocketRecipe::new);
		specialRecipeFireworkAntiRain.setRegistryName(WaterworksReference.MODID,
				WaterworksRegistryNames.RECIPE_FIREWORK_ANTI_RAIN);
		registry.register(specialRecipeFireworkAntiRain);

		final SpecialRecipeSerializer<RainRocketRecipe> specialRecipeFireworkRain = new SpecialRecipeSerializer<>(
				RainRocketRecipe::new);
		specialRecipeFireworkRain.setRegistryName(WaterworksReference.MODID,
				WaterworksRegistryNames.RECIPE_FIREWORK_RAIN);
		registry.register(specialRecipeFireworkRain);
	}

	private InitRecipes() {
		// hide me
	}
}
