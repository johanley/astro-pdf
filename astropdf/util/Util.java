package astropdf.util;

public final class Util {

  /** Return true only if the text has visible content. */
  public static boolean textHasContent(String text) {
    return text != null && text.trim().length() > 0;
  }
}
