package astropdf.util;

/** 
 Width and height of something, usually a page or document.
 See as well document-template.ps for other settings. 
*/
public final class Dimensions {
  
  /**
   Constructor.
   @param dim example format:  '8.5 x 11'.
   The 'x' can be upper case as well.   
  */
  public Dimensions(String dim) {
    this.dim = dim;
  }
  
  public Float width() {return dimension(WIDTH); }
  public Float height() {return dimension(HEIGHT); }
  
  /** Used by Adobe's Document Structuring Conventions.  Multiplies by 72. */
  public String floatingPoint() {
    return width() * INCH + " " + height() * INCH;
  }
  
  /** Used by Adobe's Document Structuring Conventions. Multiplies by 72. */
  public String integer() {
    return width().intValue() * INCH + " " + height().intValue() * INCH;
  }

  /** The '8.5 x 11' text (for example) passed to the constructor. */
  @Override public String toString() {return dim;}
  
  private String dim = "";
  private static final int WIDTH = 0;
  private static final int HEIGHT = 1;
  private static final int INCH = 72;
  private Float dimension(int part) {
    String[] parts = dim.split("x|X");
    return Float.valueOf(parts[part].trim());
  }
}
