package astropdf.util;

/** Helper for building valid PostScript literals. */
public final class PostScript {

  /** A dictionary literal in PostScript. */
  public static String dict(Object body) {
    return "<< " + body.toString() + " >>"; 
  }
  
  /** An array literal in PostScript. */
  public static String array(Object body) {
    return "[ " + body.toString() + " ]"; 
  }
  
  /** A string literal in PostScript. */
  public static String string(Object body) {
    return "(" + body.toString() + ")";
  }

}
