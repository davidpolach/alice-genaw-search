package com.dp.genaw.search;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

class TestUtils {

    static final String BASE_URL = "http://recipe-url";

    static Document loadDocument(String resourceFileName) throws IOException {
        return Jsoup.parse(
                RecipesExtractorTest.class.getResourceAsStream(resourceFileName),
                StandardCharsets.UTF_8.name(),
                BASE_URL);
    }
}
