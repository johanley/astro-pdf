package astropdf.pdf;

import java.util.LinkedHashMap;
import java.util.Map;

import astropdf.util.PostScript;

/** Data to be injected into a PostScript template (a map). */
public final class PlaceholderData {
  
  /**
   Add a key-value pair to the injected data.
   
   @param key simple name for a data item; this name is wrapped in text ${LIKE_THIS} in order 
   to make it easily distinguishable from ordinary PostScript.
   @param value the data associated with the given name. Typically a dict, but this can be any valid 
   snippet of PostScript.
  */
  public void add(String key, String value) {
    data.put(textFor(key), value);
  }
  public void add(String key, Number value) {
    data.put(textFor(key), value.toString());
  }
  public void add(String key, Boolean value) {
    data.put(textFor(key), value.toString());
  }

  /** The value is formatted by this method into a PostScript string literal. */
  public void stringLiteral(String key, String value) {
    data.put(textFor(key), PostScript.string(value));
  }
  
  public Map<String, String> data(){
    return data;
  }
  
  private Map<String, String> data = new LinkedHashMap<>();
  
  /** Return placeholder text for the given string. */
  private String textFor(String name) {
    return "${" + name + "}";
  }
}
