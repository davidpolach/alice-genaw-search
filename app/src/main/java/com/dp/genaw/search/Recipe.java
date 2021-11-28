package com.dp.genaw.search;

/**
 * Model class representing a single recipe.
 */
record Recipe(
        String name,
        String url,
        String variant,
        String nutritionInfo,
        int starRating,
        double proteinToNetCarb) {};
