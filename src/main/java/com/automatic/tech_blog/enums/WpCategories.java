package com.automatic.tech_blog.enums;

import java.util.Arrays;

public enum WpCategories {
  BUSINESS("Business Insights", 1),
  CARS("Cars and Vehicles", 4),
  FINANCE("Finance", 2),
  FOOD("Food and Recipes", 12),
  HEALTH("Health and Wellness", 7),
  HOME("HOME", 6),
  JAVA("Java", 9),
  LIFESTYLE("Lifestyle Hacks", 11),
  TECHNOLOGY("Technology Trends", 3),
  TRAVEL("Travel and Adventure", 8);

  private final String name;
  private final int id;

  WpCategories(String name, int id) {
    this.name = name;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public int getId() {
    return id;
  }

  /**
   * Finds the category ID for a given directory name.
   * Uses a similarity check for finding the closest match.
   *
   * @param directoryName the directory name
   * @return the corresponding category ID, or -1 if not found
   */
  public static int findCategoryId(String directoryName) {
    return Arrays.stream(WpCategories.values())
        .filter(category -> isSimilar(directoryName, category.name))
        .findFirst()
        .map(WpCategories::getId)
        .orElse(-1);
  }

  /**
   * Checks if two strings are similar.
   * You can replace this logic with a more robust similarity check if needed.
   *
   * @param input the input directory name
   * @param categoryName the category name
   * @return true if similar, false otherwise
   */
  private static boolean isSimilar(String input, String categoryName) {
    input = input.toLowerCase();
    categoryName = categoryName.toLowerCase();
    return categoryName.contains(input) || input.contains(categoryName);
  }
}
