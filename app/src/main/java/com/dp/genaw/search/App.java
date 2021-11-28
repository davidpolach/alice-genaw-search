package com.dp.genaw.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main console app wrapping {@link TopRecipeSearch} implementation.
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static final String ROOT_URL = "https://www.genaw.com/lowcarb/";
    private static final String RECIPES = "recipes.html";

    public static void main(String[] args) {
        var tableLinkExtractor = new TableLinkExtractor();
        var recipesExtractor = new RecipeExtractor();

        new TopRecipeSearch(tableLinkExtractor, recipesExtractor)
                .findTopRecipe(ROOT_URL, RECIPES)
                .ifPresentOrElse(
                        App::printTopRecipeInfo,
                        () -> logger.error("No recipe with given criteria found"));
    }

    private static void printTopRecipeInfo(Recipe recipe) {
        logger.info("""
                Top Protein to Net Carb, 5-star recipe:
                Name: {}
                URL: {}
                Nutrition info: {}: {}
                Protein to Net Carb Ratio: {}
                """, recipe.name(), recipe.url(), recipe.variant(), recipe.nutritionInfo(), recipe.proteinToNetCarb());
    }
}
