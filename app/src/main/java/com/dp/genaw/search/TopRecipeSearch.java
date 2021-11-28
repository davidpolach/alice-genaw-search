package com.dp.genaw.search;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main {@link Recipe recipe} search class implementing traversal of all Genaw Low-Carb sections and sub-sections to
 * detect all recipes.
 * <p>
 * Traversal runs in parallel and is using standard {@code ForkJoinPool.common} thread pool which by default has one
 * thread less than machine's vCPU cores. Parallelism can be overridden by adjusting system property {@code java.util
 * .concurrent.ForkJoinPool.common.parallelism}.
 */
public class TopRecipeSearch {

    private static final Logger logger = LoggerFactory.getLogger(TopRecipeSearch.class);

    private final TableLinkExtractor tableLinkExtractor;
    private final RecipeExtractor recipeExtractor;

    private final AtomicInteger loadedPages = new AtomicInteger(0);

    TopRecipeSearch(TableLinkExtractor tableLinkExtractor, RecipeExtractor recipeExtractor) {
        this.tableLinkExtractor = tableLinkExtractor;
        this.recipeExtractor = recipeExtractor;
    }

    /**
     * Genaw top 5-star Protein to Net Carb ratio recipe search method.
     *
     * @param rootUrl     Root URL for all relative links/pages to traverse.
     * @param rootSection Search starting point (relative to {@code rootUrl}).
     * @return Optional top recipe by given criteria, {@code Optional.empty} when no recipe is found.
     */
    public Optional<Recipe> findTopRecipe(String rootUrl, String rootSection) {
        logger.info("Starting top recipe search");

        var topRecipe = extractRecipes(rootUrl, rootSection).stream()
                .filter(recipe -> recipe.starRating() == 5)
                .max(Comparator.comparing(Recipe::proteinToNetCarb));

        logger.info("Finished, number of loaded pages: {}", loadedPages.get());

        return topRecipe;
    }

    /**
     * Provides recursive traversal from given URL and returns all {@link Recipe recipes} under this root.
     *
     * @param rootUrl  Root URL for all relative links/pages to traverse.
     * @param rootLink Root link for traversal, relative to {@code rootUrl}.
     * @return List or recipes under given root URL.
     */
    private List<Recipe> extractRecipes(String rootUrl, String rootLink) {
        // traverse multiple level category pages with recipe pages as leaf nodes
        return loadDocument(rootUrl + rootLink)
                .map(document -> {
                    if (loadedPages.get() % 50 == 0) {
                        logger.info("Already visited {} pages and running", loadedPages.get());
                    }

                    // document is either a page with links to sub-pages (links to sub-category pages or recipe pages)
                    // or a standalone recipe page
                    var links = tableLinkExtractor.extractFromDocument(document);

                    if (links.isEmpty()) {
                        // no links to sub-pages -> recipe page
                        return recipeExtractor.extractFromDocument(document);
                    } else {
                        // process links to sub-pages
                        return links.stream()
                                .parallel()
                                .flatMap(link -> extractRecipes(rootUrl, link).stream())
                                .toList();
                    }
                })
                // no data to process when document cannot be loaded
                .orElse(List.of());
    }

    // visible for testing
    Optional<Document> loadDocument(String url) {
        try {
            loadedPages.incrementAndGet();
            return Optional.of(Jsoup.connect(url).get());
        } catch (IOException e) {
            logger.error("Cannot load %s, skipping document".formatted(url), e);
            return Optional.empty();
        }
    }
}
