package com.dp.genaw.search;

import java.util.List;

import org.jsoup.nodes.Document;

/**
 * {@link DataExtractor} returning all {@code <a href>} links under {@code <table>} elements.
 */
class TableLinkExtractor extends DataExtractor<String> {

    @Override
    protected List<String> extract(Document doc) {
        // xpath - all <a> links under <table> element
        var categoryLinks = doc.selectXpath("//table//a");

        return categoryLinks.stream()
                .map(link -> link.attr("href"))
                .filter(link -> !link.isBlank())
                .toList();
    }
}
