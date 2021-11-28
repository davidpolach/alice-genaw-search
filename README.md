# ALICE - Genaw Recipe Search

This repo contains Java 17 implementation of following task assignment:

> I eat a low carb diet. I also like to body-build. You're going to help me find something to eat.
> Your job is to use the recipe site http://www.genaw.com/lowcarb/recipes.html to search for a particular recipe. This website does not provide an API, so you will need to devise an automated way to search through the pages.
> The recipe I am looking for is the one with the greatest ratio of protein to net carbs. But I have discerning taste, so make sure it has at least a 5 star rating.
> To recap:
>  Find a way to get all 5 star recipes from the site
>  Select the recipe with the greatest protein for the lowest net carbs
>  Tell me the name of the recipe, the URL, and the nutrition information

## How to Build and Run

You need Java 17 configured on your machine.
Project is using Gradle with Gradle Wrapper as a build tool.

To build the project:
```
./gradlew build
```

To run tests:
```
./gradlew test
```

## How to Run It

Either use built-in run command from gradle:

```
./gradlew run
```

Or run separately the main class: `com.dp.genaw.search.App`.

Search result is logged to console in a following format:
```
[main] INFO  com.dp.genaw.search.App#printTopRecipeInfo - Top Protein to Net Carb, 5-star recipe:
Name: AMERICAN STEAKHOUSE CHICKEN
URL: https://www.genaw.com/lowcarb/american_steakhouse_chicken.html
Nutrition info: Per Serving: 193 Calories; 2g Fat; 40g Protein; 1g Carbohydrate; trace Dietary Fiber; 1g Net Carb
Protein to Net Carb Ratio: 40.0
```

## Implementation notes

Since this is an interview task, few things are simplified in the code:

  - no resiliency - there is no HTTP retry mechanism; when URL load request fails, page is skipped and error logged to 
    console, 
  - no rate limiting - HTTP requests are issued in parallel with no limit (other than the thread pool size), Genaw site
    seems to accept reasonable load without failing, 
  - no dependency injection - to minimize JAR footprint and keep the solution minimalistic,
  - traversal runs in parallel and is using standard `ForkJoinPool.common` thread pool which by default has one
    thread less than machine's vCPU cores; parallelism can be overridden by adjusting system property `java.util.concurrent.ForkJoinPool.common.parallelism`,
  - implementation is not optimized for speed - e.g. HTML parsing is provided by `Jsoup` which builds full DOM tree for
    each page.

Also following points are implemented with respect to page traversing and parsing:

  - all links within Genaw pages (root and sub-sections) are assumed to be relative URLs,
  - recipes with "trace" protein and/or "trace" net carb are not evaluated since exact ratio cannot be calculated,
  - grams are assumed to be used when gram unit is missing (e.g. `3.5 Net Carb` instead of `3.5g Net Carb`), 
  - when `Net Carb` nutrition info is missing but `Carb` is detected, recipe is considered valid (`Carb` is treated as `Net Carb`),
  - when `Net Carb`, `Carb` or `Protein` info is missing, recipe is skipped,
  - some recipes contain multiple recipe versions (e.g. [here](https://www.genaw.com/lowcarb/handmade_marshmallows.html)) - all nutrition variants are extracted however under one recipe name.