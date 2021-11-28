package com.dp.genaw.search;

import java.util.List;
import java.util.Optional;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopRecipeSearchTest {

    private static final String ROOT_URL = "http://root/";

    @Mock
    private TableLinkExtractor tableLinkExtractor;
    @Mock
    private RecipeExtractor recipeExtractor;

    @Test
    void searchRootUrlNoDocument() {
        var fixture = new TopRecipeSearch(tableLinkExtractor, recipeExtractor) {
            @Override
            Optional<Document> loadDocument(String url) {
                return Optional.empty();
            }
        };

        assertThat(fixture.findTopRecipe(ROOT_URL, "root"))
                .isEmpty();
    }

    /**
     * Test with mocked page structure, see {@link #mockWithPageStructure(Document, Document, Document, Document)}:
     * <tt>
     * root
     * -- section1
     * ------ recipe1, 5-star
     * -- section2
     * ------ sub-section1
     * ---------- recipe2, 5-star
     * ---------- recipe3, 3-star
     * ------ sub-section2
     * ---------- recipe4, 4-star
     * ---------- recipe5 - with URL load error
     * </tt>
     */
    @Test
    void searchWithSubPages() {
        var recipe1 = mock(Document.class);
        var recipe2 = mock(Document.class);
        var recipe3 = mock(Document.class);
        var recipe4 = mock(Document.class);

        TopRecipeSearch fixture = mockWithPageStructure(recipe1, recipe2, recipe3, recipe4);

        // recipe2 is top 5-star Protein to Net Carb ratio
        assertThat(fixture.findTopRecipe(ROOT_URL, "root"))
                .contains(new Recipe("recipe2", "url2", "v2", "info2", 5, 2D));

        // assert all recipes were visited
        verify(recipeExtractor).extractFromDocument(recipe1);
        verify(recipeExtractor).extractFromDocument(recipe2);
        verify(recipeExtractor).extractFromDocument(recipe3);
        verify(recipeExtractor).extractFromDocument(recipe4);
    }

    private TopRecipeSearch mockWithPageStructure(
            Document recipe1,
            Document recipe2,
            Document recipe3,
            Document recipe4) {

        var root = mock(Document.class);
        var section1 = mock(Document.class);
        var section2 = mock(Document.class);
        var subSection1 = mock(Document.class);
        var subSection2 = mock(Document.class);

        // mock document loading
        var fixture = new TopRecipeSearch(tableLinkExtractor, recipeExtractor) {
            @Override
            Optional<Document> loadDocument(String url) {
                var document = switch (url) {
                    case ROOT_URL + "root" -> root;
                    case ROOT_URL + "section1" -> section1;
                    case ROOT_URL + "section2" -> section2;
                    case ROOT_URL + "sub-section1" -> subSection1;
                    case ROOT_URL + "sub-section2" -> subSection2;
                    case ROOT_URL + "recipe1" -> recipe1;
                    case ROOT_URL + "recipe2" -> recipe2;
                    case ROOT_URL + "recipe3" -> recipe3;
                    case ROOT_URL + "recipe4" -> recipe4;
                    case ROOT_URL + "recipe5" -> null; // null corresponds to page load error
                    default -> throw new IllegalStateException("Unexpected url");
                };

                return Optional.ofNullable(document);
            }
        };

        // mock-page structure
        when(tableLinkExtractor.extractFromDocument(root))
                .thenReturn(List.of("section1", "section2"));
        when(tableLinkExtractor.extractFromDocument(section1))
                .thenReturn(List.of("recipe1"));
        when(tableLinkExtractor.extractFromDocument(section2))
                .thenReturn(List.of("sub-section1", "sub-section2"));
        when(tableLinkExtractor.extractFromDocument(subSection1))
                .thenReturn(List.of("recipe2", "recipe3"));
        when(tableLinkExtractor.extractFromDocument(subSection2))
                .thenReturn(List.of("recipe4", "recipe5"));
        when(tableLinkExtractor.extractFromDocument(recipe1))
                .thenReturn(List.of());
        when(tableLinkExtractor.extractFromDocument(recipe2))
                .thenReturn(List.of());
        when(tableLinkExtractor.extractFromDocument(recipe3))
                .thenReturn(List.of());
        when(tableLinkExtractor.extractFromDocument(recipe4))
                .thenReturn(List.of());

        // mock recipes
        when(recipeExtractor.extractFromDocument(recipe1))
                .thenReturn(List.of(
                        new Recipe("recipe1", "url1", "v1", "info1", 5, 1D)));
        when(recipeExtractor.extractFromDocument(recipe2))
                .thenReturn(List.of(
                        new Recipe("recipe2", "url2", "v2", "info2", 5, 2D)));
        when(recipeExtractor.extractFromDocument(recipe3))
                .thenReturn(List.of(
                        new Recipe("recipe3", "url3", "v3", "info3", 3, 3D)));
        when(recipeExtractor.extractFromDocument(recipe4))
                .thenReturn(List.of(
                        new Recipe("recipe4", "url4", "v4", "info4", 4, 4D)));

        return fixture;
    }
}
