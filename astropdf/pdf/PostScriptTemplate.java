package astropdf.pdf;

import static astropdf.util.Constants.NL;
import static astropdf.util.LogUtil.log;
import static astropdf.util.LogUtil.warn;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import astropdf.config.CoreConfig;
import astropdf.util.Constants;
import astropdf.util.DataFileReader;
import astropdf.util.FileWriter;

/**
 Read in a PostScript template file (.ps), and populate it with data by replacing placeholders with 
 PostScript data (usually a dict) or PostScript code.
 
 <P>An example of the format of placeholder text: <pre>${WEEKLY_DATA}</pre>.
  
 <P>When finished building a document, outputs a valid PostScript document to a file (.ps).
 
 <P>IMPORTANT: IN THIS PROJECT, SNIPPETS OF PostScript CODE HAVE THEIR TEXT ENCODED USING ISO-8859-1 (Latin-1).
 In addition, ISO-LATIN-1 must be used when reading/writing PostScript code.
 If that is not the case, then accented characters will appear incorrectly.
*/
public final class PostScriptTemplate {
  
  public PostScriptTemplate(CoreConfig config) {
    this.config = config;
  }

  /** 
   Populate a template PostScript file (.ps) with data, and return the populated template as a String object.
   Replace placeholder-text with the 'real' data. 
   This is typically used to return the body of a single page, but without its 'header-trailer'.

   @param caller is used to identify the directory in which the .pst file resides. 
   @param inputTemplateFileName simple name of the input file
   @param withData key is placeholder-text appearing in the input file (example: "${WEEKLY_DATA}"), and value is the replacement text (a snippet of PostScript).
  */
  public String snippet(String inputTemplateFileName, Map<String, String> withData, Object caller) throws IOException {
    DataFileReader reader = new DataFileReader();
    List<String> lines = reader.readFileLATIN1(caller.getClass(), inputTemplateFileName);
    StringBuilder all = new StringBuilder();
    for(String line : lines) {
      all.append(line + Constants.NL);
    }
    String fullText = all.toString();
    for(String placeholderKey : withData.keySet()) {
      String replacement = withData.get(placeholderKey);
      if (fullText.contains(placeholderKey)) {
        fullText = fullText.replace(placeholderKey, replacement);
      }
      else {
        warn("PLACEHOLDER KEY NOT FOUND: " + placeholderKey);
      }
    }
    return fullText;
  }

  /** Populate a single page of the document, and add page header-trailer info. */
  public String page(String content, Integer pageNum, boolean isRotated, Object caller) throws IOException {
    log("Page " + pageNum);
    PlaceholderData with = new PlaceholderData();
    with.add("PAGE_CONTENT", content);
    with.add("PAGE_NUM", pageNum);
    with.add("ODD_OR_EVEN_PAGE_NUM", (pageNum % 2 == 0) ? "is-even-page" : "is-odd-page");
    with.add("ROTATED_OR_NOT", isRotated ? "is-rotated" : "is-not-rotated");
    return snippet("page-template.ps", with.data(), caller);
  }

  /** 
   Populate the complete document, and save it to the output directory.
   Once this method is called, it makes no sense to continue using this object. 
  */
  public void documentAndSave(String fileName, List<String> pages, Object caller) throws IOException {
    PlaceholderData with = new PlaceholderData();
    with.add("ALL_PAGES", singleStringFor(pages));
    with.add("NUM_PAGES", pages.size());
    with.add("TITLE", config.title());
    with.add("CREATOR", config.author());
    with.add("CREATION_DATE", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
    with.add("PAGE_WIDTH", config.pageDimensions().width());
    with.add("PAGE_HEIGHT", config.pageDimensions().height());
    with.add("DIMENSIONS_FLOAT", config.pageDimensions().floatingPoint());
    with.add("DIMENSIONS_INTEGER", config.pageDimensions().integer());
    String document = snippet("document-template.ps", with.data(), caller);
    FileWriter writer = new FileWriter();
    log("*** OUTPUTTING PostScript FILE (8859-1 encoding): " + fileName + ".ps ***");
    writer.outputLATIN_1(document, config.outputDir(), fileName + ".ps");
  }
  
  private CoreConfig config;
  
  private String singleStringFor(List<String> pages) {
    StringBuilder result = new StringBuilder();
    for(String page : pages) {
      result.append(page + NL);
    }
    return result.toString();
  }
}