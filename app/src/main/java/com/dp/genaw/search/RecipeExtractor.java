package com.dp.genaw.search;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DataExtractor} returning all {@link Recipe recipes} from a single recipe page.
 * <p>
 * Single recipe page can contain multiple "recipe variants", e.g.:
 * <tt>
 * Per 4 Wafers: 117 Calories; 10g Fat; 6g Protein; 2g Carbohydrate; 1g Dietary Fiber; 1g Net Carb
 * Per 8 Wafers: 235 Calories; 20g Fat; 13g Protein; 3g Carbohydrate; 1.5g Dietary Fiber; 1.5g Net Carbs
 * </tt>
 * Snippet above corresponds to two recipe variants, each with a different Protein to Net Carb ratio.
 */
class RecipeExtractor extends DataExtractor<Recipe> {

    private static final Logger logger = LoggerFactory.getLogger(RecipeExtractor.class);

    private static final String STAR_RATING_IMAGE_SUFFIX = "_star.gif";

    // case-insensitive pattern for Protein ingredient, e.g. "16g Protein", gram unit is optional
    private static final Pattern PROTEIN_PATTERN =
            Pattern.compile(".*[\\s;]([\\d.]+)g?\\s+Protein.*", Pattern.CASE_INSENSITIVE);
    // case-insensitive pattern for Net Carb ingredient, e.g. "1.5g Net Carbs", gram unit and "Net" keyword are optional
    private static final Pattern NET_CARB_PATTERN =
            Pattern.compile(".*[\\s;]([\\d.]+)g?\\s+(Net\\s+)?Carb.*", Pattern.CASE_INSENSITIVE);

    @Override
    protected List<Recipe> extract(Document doc) {
        var recipeName = doc.selectXpath("//b")
                .get(0)
                .text();
        var starRating = doc.selectXpath("//img")
                .stream()
                .map(element -> element.attr("src"))
                .filter(imageUrl -> imageUrl.endsWith(STAR_RATING_IMAGE_SUFFIX))
                .map(imageUrl -> Integer.valueOf(imageUrl.charAt(0) + ""))
                .findFirst()
                .orElse(0);

        // handle multiple nutrition variants for a single recipe name, e.g. ZESTY CHEDDAR WAFERS:
        //
        // Per 4 Wafers: 117 Calories; 10g Fat; 6g Protein; 2g Carbohydrate; 1g Dietary Fiber; 1g Net Carb
        // <br>
        // Per 8 Wafers: 235 Calories; 20g Fat; 13g Protein; 3g Carbohydrate; 1.5g Dietary Fiber; 1.5g Net Carbs
        //
        // each variant results in a separate recipe since variant can have different Protein to Net Carbs ratios
        var nutritionInfos = doc.selectXpath("//i")
                .stream()
                .flatMap(element -> element.textNodes().stream())
                .map(TextNode::text)
                .toList();

        return nutritionInfos.stream()
                .filter(nutritionInfo -> !nutritionInfo.isBlank())
                .map(nutritionInfo -> fromNutritionInfo(nutritionInfo, recipeName, doc.location(), starRating))
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Extract recipe variant from a nutrition info string, e.g.:
     * <tt>
     * Per 8 Wafers: 235 Calories; 20g Fat; 13g Protein; 3g Carbohydrate; 1.5g Dietary Fiber; 1.5g Net Carbs
     * </tt>
     * <p>
     * When a nutrition info cannot be parsed, variant is skipped (method does not fail). Also, variants with zero
     * or "trace" Protein or Net Carb ingredients are skipped as well. As exact Protein to Net Carb ratio cannot be
     * evaluated.
     *
     * @param nutritionInfo Nutrition info string.
     * @param recipeName    Main recipe name from this variant.
     * @param url           Recipe URL.
     * @param starRating    Recipe Star Rating.
     * @return Recipe variant.
     */
    private Recipe fromNutritionInfo(String nutritionInfo, String recipeName, String url, int starRating) {
        // variant name, e.g. for:
        // Per 4 Wafers: 117 Calories; 10g Fat; 6g Protein; 2g Carbohydrate; 1g Dietary Fiber; 1g Net Carb
        // -> variant name = "Per 4 Wafers"
        String variantName = "";
        if (nutritionInfo.contains(":")) {
            variantName = nutritionInfo.substring(0, nutritionInfo.indexOf(":")).trim();
            nutritionInfo = nutritionInfo.substring(nutritionInfo.indexOf(":") + 1).trim();
        }

        // replace "trace" nutrition info with "0g" - recipes with trace proteins or trace net carb are not evaluated
        var nutritionInfoNoTrace = nutritionInfo.replaceAll("trace", "0g");
        var proteinMatcher = PROTEIN_PATTERN.matcher(nutritionInfoNoTrace);
        var netCarbsMatcher = NET_CARB_PATTERN.matcher(nutritionInfoNoTrace);

        if (proteinMatcher.matches() && netCarbsMatcher.matches()) {
            var proteins = Double.parseDouble(proteinMatcher.group(1));
            var netCarbs = Double.parseDouble(netCarbsMatcher.group(1));

            if (proteins == 0 || netCarbs == 0) {
                logger.debug("Skipping {}: variants with trace protein or trace net carb are not evaluated", url);
                return null;
            } else {
                return new Recipe(recipeName, url, variantName, nutritionInfo, starRating, proteins / netCarbs);
            }
        } else {
            // not a hard error - some recipe pages have nutrition unrelated text in <i> tags
            // -> skip and do not treat as recipe variant
            logger.debug("Cannot parse protein and/or net carb data from {}: {}, skipping variant", url, nutritionInfo);
            return null;
        }
    }
}
