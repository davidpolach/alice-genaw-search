package com.dp.genaw.search;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static com.dp.genaw.search.TestUtils.loadDocument;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TableLinkExtractor} tests using HTML test resource files in {@code src/main/resources} as test-cases.
 */
class TableLinkExtractorTest {

    private final TableLinkExtractor fixture = new TableLinkExtractor();

    @Test
    void extractLinks() throws IOException {
        var doc = loadDocument("links.html");
        var result = fixture.extractFromDocument(doc);

        assertThat(result)
                .containsExactly(
                        "broccoli_quiche.html",
                        "grilled_cheese_sandwich_pie.html",
                        "leek_quiche.html",
                        "tijuana_quiche.html");
    }

    @Test
    void extractLinksInvalid() throws IOException {
        var doc = loadDocument("links_invalid.html");
        var result = fixture.extractFromDocument(doc);

        assertThat(result)
                .isEmpty();
    }
}
