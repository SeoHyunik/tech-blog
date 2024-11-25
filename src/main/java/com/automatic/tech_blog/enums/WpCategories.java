package com.automatic.tech_blog.enums;

import java.util.Arrays;

public enum WpCategories {
  BUSINESS("Business Insights", "1"),
  CARS("Cars and Vehicles", "4"),
  FINANCE("Finance", "2"),
  FOOD("Food and Recipes", "12"),
  HEALTH("Health and Wellness", "7"),
  HOME("HOME", "6"),
  JAVA("JAVA-SPRING", "9"),
  LIFESTYLE("Lifestyle Hacks", "11"),
  TECHNOLOGY("IT Knowledge", "3"),
  TRAVEL("Travel and Adventure", "8");

  private final String name;
  private final String id;

  WpCategories(String name, String id) {
    this.name = name;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public String getId() {
    return id;
  }

  /**
   * Finds the category ID for a given directory name.
   * Uses a similarity check for finding the closest match.
   */
  public static String findCategoryId(String directoryName) {
    return Arrays.stream(WpCategories.values())
        .filter(category -> isSimilar(directoryName, category.name))
        .findFirst()
        .map(WpCategories::getId)
        .orElse("3");
  }

  /**
   * Checks if the input belongs to the category.
   * JAVA-SPRING contains JPA, WebFlux, etc.
   * IT Knowledge contains AI, AKKA, etc.
   */
  private static boolean isSimilar(String input, String categoryName) {
    return input.equalsIgnoreCase(categoryName) ||
        (categoryName.equals("JAVA-SPRING") && (input.contains("JPA")) || input.contains("WebFlux")) ||
        (categoryName.equals("IT Knowledge") && (input.contains("AI") || input.contains("AKKA")));
  }
}
