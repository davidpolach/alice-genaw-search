package com.dp.genaw.search;

import java.util.List;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract and generic data extractor working with a single {@link Document document} instance and extracting a list
 * of items.
 * <p>
 * E.g. see {@link TableLinkExtractor} extracting list of {@code String}-typed links.
 *
 * @param <T> Type of items returned by this extractor.
 */
abstract class DataExtractor<T> {

    private static final Logger logger = LoggerFactory.getLogger(DataExtractor.class);

    /**
     * Extraction logic to be implemented by sub-classes.
     *
     * @param doc Document to work with.
     * @return List of extracted items.
     */
    protected abstract List<T> extract(Document doc);

    /**
     * Interface method to be called by clients - wraps abstract {@link #extract(Document)}, logs all extraction
     * exceptions and returns empty list in case of any extraction errors.
     *
     * @param doc Document to work with.
     * @return Extracted items or empty list in case of any {@link Exception}.
     */
    List<T> extractFromDocument(Document doc) {
        try {
            logger.debug("Extracting data from: {}", doc.location());

            return extract(doc);
        } catch (Exception e) {
            logger.error("Cannot extract data from document %s, skipping document".formatted(doc.location()), e);
            return List.of();
        }
    }
}
