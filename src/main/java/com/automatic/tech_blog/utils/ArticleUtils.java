package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.repository.q_repo.PastedImageQRepository;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleUtils {
  private final PastedImageQRepository imageQRepository;

  public String editImageTags(String content) {
    log.info("Start editImageTags");

    // 1. Use regex to find <img> tags in the HTML
    Pattern pattern = Pattern.compile("<img\\s+[^>]*src=[\"']([^\"']+)[\"'][^>]*>");
    Matcher matcher = pattern.matcher(content);

    // 2. Create a StringBuilder to store the result
    StringBuilder updatedContent = new StringBuilder();

    // 3. Iterate through matches and process each tag
    while (matcher.find()) {
      String matchedTag = matcher.group(0); // The entire matched <img> tag
      String imageSrc = matcher.group(1); // The value of the src attribute (image path)

      log.info("Matched tag: {}, Extracted src: {}", matchedTag, imageSrc);

      // 4. Search the database for the new image URL
      Optional<String> imageUrl = imageQRepository.findByImageName(imageSrc);

      if (imageUrl.isPresent()) {
        // 5.1 If the image URL exists, replace the src with the new URL
        String newImgTag = matchedTag.replace(imageSrc, imageUrl.get());
        log.info("Image URL found. Replacing src with: {}", imageUrl.get());
        matcher.appendReplacement(updatedContent, Matcher.quoteReplacement(newImgTag));
      } else {
        // 5.2 If the image URL does not exist, keep the original tag
        log.warn("Image URL not found for: {}. Keeping original tag.", imageSrc);
        matcher.appendReplacement(updatedContent, Matcher.quoteReplacement(matchedTag));
      }
    }

    // 6. Append the remaining content after the last match
    matcher.appendTail(updatedContent);
    log.info("Finished editing image tags.{}", updatedContent);
    return updatedContent.toString();
  }

  public String editLinkTags(String content) {
    log.info("Start editLinkTags");

    // 1. Use regex to find <a> tags in the HTML
    Pattern pattern = Pattern.compile("<a\\s+[^>]*href=[\"']([^\"']+)[\"']>([^<]+)</a>");
    Matcher matcher = pattern.matcher(content);

    // 2. Create a StringBuilder to store the result
    StringBuilder updatedContent = new StringBuilder();

    // 3. Iterate through matches and process each <a> tag
    while (matcher.find()) {
      String matchedTag = matcher.group(0); // The entire matched <a> tag
      String hrefValue = matcher.group(1); // Extracted href value (not used here)
      String displayText = matcher.group(2); // Extracted display text (title of the article)

      log.info("Matched tag: {}, Display text: {}", matchedTag, displayText);

      // 4. Convert the display text into a link-compatible format
      String linkPath = "https://kiwijam.kr/" + displayText.trim().toLowerCase().replace(" ", "-");

      // 5. Replace href with the generated link path
      String newLinkTag = matchedTag.replace(hrefValue, linkPath);
      log.info("Generated link path: {}. Replacing href.", linkPath);
      matcher.appendReplacement(updatedContent, Matcher.quoteReplacement(newLinkTag));
    }

    // 6. Append the remaining content after the last match
    matcher.appendTail(updatedContent);
    log.info("Finished editing link tags.{}", updatedContent);
    return updatedContent.toString();
  }

  public String editHtmlStructure(String content) {
    // 1. Remove the opening ```html
    content = content.replaceFirst("^```html\\s*", "");

    // 2. Remove the closing ```
    content = content.replaceFirst("\\s*```$", "");

    // 3. Parse HTML content into a document object
    Document document = Jsoup.parse(content);

    // 4. Process tables
    for (Element table : document.select("table")) {
      // 4-1. Set table styles
      table.attr(
          "style",
          "background-color: #f2f2ea; border-collapse: collapse; border: 1px solid #041979; border-radius: 5px;");

      // 4-2. Process rows
      Elements rows = table.select("tr");
      for (int i = 0; i < rows.size(); i++) {
        Element row = rows.get(i);

        // Apply different styles for the first row
        if (i == 0) {
          for (Element cell : row.select("th, td")) {
            cell.attr(
                "style",
                "border: 1px solid #041979; padding: 8px; white-space: nowrap; font-weight: bold;");
          }
        } else {
          // Default styles for other rows
          row.attr("style", "border: 1px solid #041979;");
          for (Element cell : row.select("th, td")) {
            cell.attr("style", "border: 1px solid #041979; padding: 8px;");
          }
        }
      }
    }
    // 5. Return the updated HTML content as a string
    return document.html();
  }
}
