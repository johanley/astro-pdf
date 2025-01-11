package astropdf.lang;

import static astropdf.util.LogUtil.warn;
import static astropdf.util.LogUtil.log;
import static astropdf.util.Util.textHasContent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import astropdf.util.DataFileReader;

/** 
 Text to be emitted into the PDF, in the given language.
 
 <P>This class is intended for general text, not for dates and times (or similar items).
 
 <P>The intent is that this class is used for only ONE LANGUAGE WHEN BUILDING A GIVEN PDF (not multilingual).
 <P>This class is not suited for use in a server environment, since it loads data into a static Map. 
*/
public final class Text {
  
  public static void main(String[] args) {
    Text text = new Text(Lang.fr);
    text.logAllTranslations();
  }

  /** Side-effect: loads all translations for the given lang into a static data structure. */
  public Text(Lang lang) {
    this.lang = lang;
    if(translations.isEmpty()) {
      loadTranslations(lang);
    }
  }
  
  /** 
   Return the text to be placed into the PostScript output. 
   The key is simply the English version of the text. 
   (This allows the caller to be more expressive, since the actual English text is used as the key).
  */
  public String of(String key) {
    String result = key;
    if (Lang.en != lang) {
      result = lookup(key);
    }
    return result;
  }
  
  /** If using English, this method logs only a simple message, since there's no translation file for English. */
  public void logAllTranslations() {
    if (Lang.en == lang) {
      log("No translations present. Using en.");
    }
    else {
      for(String key : translations.keySet()) {
        log(key + SEPARATOR + translations.get(key) + " (" + this.of(key) + ")");
      }
    }
  }
  
  private Lang lang;
  
  /**  Translations for a single language.  */
  private static Map<String/*key*/, String> translations = new LinkedHashMap<>();
  private static final String COMMENT = "#";
  private static final String SEPARATOR = "=";
  
  private String lookup(String key) {
    String result = translations.get(key);
    if (result == null) {
      warn("TRANSLATION NOT FOUND for: '" + key + "'. Target lang: " + lang);
      result = key;
    }
    return result;
  }
  
  /** Scan the directory (where this class resides) for files which contain all translations for the given lang. */
  private void loadTranslations(Lang lang) {
    String translationFile = lang + ".txt";
    DataFileReader reader = new DataFileReader();
    List<String> lines = reader.readFileUTF8(this.getClass(), translationFile);
    for(String line : lines) {
      if(!line.trim().startsWith(COMMENT)) {
        String[] parts = line.trim().split(Pattern.quote(SEPARATOR));
        if (hasContent(parts)) {
          translations.put(parts[0].trim(), parts[1].trim());
        }
      }
    }
  }
  
  private boolean hasContent(String[] parts) {
    return textHasContent(parts[0]) && textHasContent(parts[1]);
  }
}