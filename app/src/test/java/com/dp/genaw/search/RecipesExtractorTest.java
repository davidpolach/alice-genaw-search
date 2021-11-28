package com.dp.genaw.search;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static com.dp.genaw.search.TestUtils.BASE_URL;
import static com.dp.genaw.search.TestUtils.loadDocument;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RecipeExtractor} tests using HTML test resource files in {@code src/main/resources} as test-cases.
 */
class RecipesExtractorTest {

    private final RecipeExtractor fixture = new RecipeExtractor();

    @Test
    void extractSingleRecipeVariant() throws IOException {
        var doc = loadDocument("recipe_single_variant.html");
        var result = fixture.extractFromDocument(doc);

        assertThat(result)
                .containsExactly(
                        new Recipe(
                                "BEEFY-MUSHROOM SOUP",
                                BASE_URL,
                                "Per Cup",
                                "302 Calories; 19g Fat; 23g Protein; 7g Carbohydrate; 3g Dietary Fiber; 4g Net Carbs",
                                5,
                                23D / 4));
    }

    @Test
    void extractMultipleRecipeVariants() throws IOException {
        var doc = loadDocument("recipe_multiple_variants.html");
        var result = fixture.extractFromDocument(doc);

        assertThat(result)
                .containsExactly(
                        new Recipe(
                                "ZESTY CHEDDAR WAFERS",
                                BASE_URL,
                                "Per 4 Wafers",
                                "117 Calories; 10g Fat; 6g Protein; 2g Carbohydrate; 1g Dietary Fiber; 1g Net Carb",
                                4,
                                6D),
                        new Recipe(
                                "ZESTY CHEDDAR WAFERS",
                                BASE_URL,
                                "Per 8 Wafers",
                                "235 Calories; 20g Fat; 13g Protein; 3g Carbohydrate; 1.5g Dietary Fiber; 1.5g Net Carbs",
                                4,
                                13 / 1.5D));
    }

    @Test
    void extractRecipeWithTraceProteinMultipleVariants() throws IOException {
        var doc = loadDocument("recipe_trace_protein_multiple.html");
        var result = fixture.extractFromDocument(doc);

        // recipe with two variants but one of them contains trace protein info:
        // <i>
        // Per tablespoon: 104 Calories; 11g Fat; trace Protein; 1g Carbohydrate; trace Dietary Fiber; 1g Net Carbs
        // <br>
        // Per 1/4 cup: 418 Calories; 45g Fat; 1g Protein; 5g Carbohydrate; 2g Dietary Fiber; 3g Net Carbs
        // </i>
        // -> return only one variant with protein numbers
        assertThat(result)
                .containsExactly(
                        new Recipe(
                                "CHIPOTLE MAYO",
                                BASE_URL,
                                "Per 1/4 cup",
                                "418 Calories; 45g Fat; 1g Protein; 5g Carbohydrate; 2g Dietary Fiber; 3g Net Carbs",
                                3,
                                1D / 3));
    }

    @Test
    void extractRecipeWithTraceNetCarbSingleVariant() throws IOException {
        var doc = loadDocument("recipe_trace_protein_single.html");
        var result = fixture.extractFromDocument(doc);

        // recipe with single variant containing trace net carb info:
        // <i>
        // Per tablespoon: 104 Calories; 11g Fat; 1g Protein; 1g Carbohydrate; trace Dietary Fiber; trace Net Carbs
        // </i>
        // -> recipe not evaluated
        assertThat(result)
                .isEmpty();
    }

    @Test
    void extractRecipeWithMultipleMethodsAndVariants() throws IOException {
        var doc = loadDocument("recipe_multiple_methods.html");
        var result = fixture.extractFromDocument(doc);

        // recipe with two variants but one of them contains trace protein info:
        // <i>
        // Per tablespoon: 104 Calories; 11g Fat; trace Protein; 1g Carbohydrate; trace Dietary Fiber; 1g Net Carbs
        // <br>
        // Per 1/4 cup: 418 Calories; 45g Fat; 1g Protein; 5g Carbohydrate; 2g Dietary Fiber; 3g Net Carbs
        // </i>
        // -> return only one variant with protein numbers
        assertThat(result)
                .containsExactly(
                        new Recipe(
                                "MOCK CLOTTED CREAM",
                                BASE_URL,
                                "Per Tablespoon",
                                "76 Calories; 8g Fat; 1g Protein; trace Carbohydrate; 0g Dietary Fiber; .5g Net Carbs",
                                3,
                                1 / 0.5D));
    }

    @Test
    void extractRecipeWithMultipleIngredientVariants() throws IOException {
        var doc = loadDocument("recipe_multiple_ingredient_variants.html");
        var result = fixture.extractFromDocument(doc);

        // recipe with two ingredient sub-recipes each with two variants:
        // <p>
        // With granular Splenda:
        // <br>
        // <i>
        // Per 1/8 recipe: 113 Calories; 11g Fat; 1g Protein; 3g Carbohydrate; 1g Dietary Fiber; 2g Net Carbs
        // <br>
        // Per 1/10 recipe: 91 Calories; 9g Fat; 1g Protein; 3g Carbohydrate; 1g Dietary Fiber; 2g Net Carbs
        // <p>
        // With liquid Splenda:
        // <br>
        // <i>
        // Per 1/8 recipe: 109 Calories; 11g Fat; 1g Protein; 2g Carbohydrate; 1g Dietary Fiber; 1g Net Carbs
        // <br>
        // Per 1/10 recipe: 87 Calories; 9g Fat; 1g Protein; 2g Carbohydrate; 1g Dietary Fiber; 1g Net Carbs
        // </i>
        // -> return four recipe variants
        assertThat(result)
                .containsExactly(
                        new Recipe(
                                "MOCHA CREAM FROSTING",
                                BASE_URL,
                                "Per 1/8 recipe",
                                "113 Calories; 11g Fat; 1g Protein; 3g Carbohydrate; 1g Dietary Fiber; 2g Net Carbs",
                                3,
                                1D / 2),
                        new Recipe(
                                "MOCHA CREAM FROSTING",
                                BASE_URL,
                                "Per 1/10 recipe",
                                "91 Calories; 9g Fat; 1g Protein; 3g Carbohydrate; 1g Dietary Fiber; 2g Net Carbs",
                                3,
                                1D / 2),
                        new Recipe(
                                "MOCHA CREAM FROSTING",
                                BASE_URL,
                                "Per 1/8 recipe",
                                "109 Calories; 11g Fat; 1g Protein; 2g Carbohydrate; 1g Dietary Fiber; 1g Net Carbs",
                                3,
                                1D),
                        new Recipe(
                                "MOCHA CREAM FROSTING",
                                BASE_URL,
                                "Per 1/10 recipe",
                                "87 Calories; 9g Fat; 1g Protein; 2g Carbohydrate; 1g Dietary Fiber; 1g Net Carbs",
                                3,
                                1D));
    }

    @Test
    void extractRecipeWithoutGrams() throws IOException {
        var doc = loadDocument("recipe_optional_grams.html");
        var result = fixture.extractFromDocument(doc);

        // recipe with Net Carb part not having a gram ("g") unit
        // <i>
        // Per Serving: 131 Calories; 9g Fat; 9g Protein; 5g Carbohydrate; 1.5g Dietary Fiber; 3.5 Net Carbs
        // </i>
        // -> still valid, assuming grams are used, return recipe variant
        assertThat(result)
                .containsExactly(
                        new Recipe(
                                "LCMILLER461'S FAUX ATKINS PEANUT BUTTER BARS",
                                BASE_URL,
                                "Per Serving",
                                "131 Calories; 9g Fat; 9g Protein; 5g Carbohydrate; 1.5g Dietary Fiber; 3.5 Net Carbs",
                                2,
                                9 / 3.5D));
    }

    @Test
    void extractRecipeCaseInsensitiveNutrition() throws IOException {
        var doc = loadDocument("recipe_case_insensitive_nutrition.html");
        var result = fixture.extractFromDocument(doc);

        // recipe with Net Carb part with different case
        // <i>
        // Per 1/5 recipe: 207 Calories; 17g Fat; 10g Protein; 2g Carbohydrate; trace Dietary Fiber; 2g net carbs
        // <br>
        // Per 1/6 recipe: 172 Calories; 14g Fat; 9g protein; 1.5g Carbohydrate; trace Dietary Fiber; 1.5g Net Carbs
        // </i>
        // -> return both recipe variants
        assertThat(result)
                .containsExactly(
                        new Recipe(
                                "EGG SALAD II",
                                BASE_URL,
                                "Per 1/5 recipe",
                                "207 Calories; 17g Fat; 10g Protein; 2g Carbohydrate; trace Dietary Fiber; 2g net carbs",
                                3,
                                5D),
                        new Recipe(
                                "EGG SALAD II",
                                BASE_URL,
                                "Per 1/6 recipe",
                                "172 Calories; 14g Fat; 9g protein; 1.5g Carbohydrate; trace Dietary Fiber; 1.5g Net Carbs",
                                3,
                                9 / 1.5D));
    }

    @Test
    void extractRecipeMissingSpaceBeforeNutritionPart() throws IOException {
        var doc = loadDocument("recipe_missing_space_before_nutrition.html");
        var result = fixture.extractFromDocument(doc);

        // recipe with missing space before Net Carb ingredient not having a gram ("g") unit
        // <i>
        // Per 1/4 Recipe: 593 Calories; 40g Fat; 50g Protein; 9g Carbohydrate; 3g Dietary Fiber;6g Net Carbs
        // </i>
        // -> return recipe variant
        assertThat(result)
                .containsExactly(
                        new Recipe(
                                "FISH CHOWDER CASSEROLE",
                                BASE_URL,
                                "Per 1/4 Recipe",
                                "593 Calories; 40g Fat; 50g Protein; 9g Carbohydrate; 3g Dietary Fiber;6g Net Carbs",
                                3,
                                50D / 6));
    }

    @Test
    void extractRecipeOptionalNetKeyWord() throws IOException {
        var doc = loadDocument("recipe_optional_net.html");
        var result = fixture.extractFromDocument(doc);

        // recipe with missing "Net" keyword containing only "Carb" nutrition info
        // <i>
        // Per 1/4 cup: 434 Calories; 48g Fat; 1g Protein; .5g Carbohydrate; trace Dietary Fiber; .5g carbs
        // </i>
        // -> return recipe variant
        assertThat(result)
                .containsExactly(
                        new Recipe(
                                "NEANDERTHIN MAYONNAISE",
                                BASE_URL,
                                "Per 1/4 cup",
                                "434 Calories; 48g Fat; 1g Protein; .5g Carbohydrate; trace Dietary Fiber; .5g carbs",
                                3,
                                1 / 0.5D));
    }

    @Test
    void extractRecipeMultiple() throws IOException {
        var doc = loadDocument("recipe_multiple.html");
        var result = fixture.extractFromDocument(doc);

        // multiple recipes per page
        assertThat(result)
                .containsExactly(
                        new Recipe(
                                "HANDMADE MARSHMALLOWS",
                                BASE_URL,
                                "Per Serving",
                                "14 Calories; 0g Fat; 2g Protein; .5g Carbohydrate; 0g Dietary Fiber; .5g Net Carbs",
                                2,
                                2 / 0.5D),
                        new Recipe(
                                "HANDMADE MARSHMALLOWS",
                                BASE_URL,
                                "Per Serving",
                                "16 Calories; 0g Fat; 2g Protein; 1g Carbohydrate; 0g Dietary Fiber; 1g Net Carbs",
                                2,
                                2D),
                        new Recipe(
                                "HANDMADE MARSHMALLOWS",
                                BASE_URL,
                                "Per Serving",
                                "18 Calories; trace Fat; 3g Protein; 1g Carbohydrate; 0g Dietary Fiber; 1g Net Carb",
                                2,
                                3D));
    }

    @Test
    void extractRecipeCannotParse() throws IOException {
        var doc = loadDocument("recipe_invalid.html");
        var result = fixture.extractFromDocument(doc);

        assertThat(result)
                .isEmpty();
    }
}
