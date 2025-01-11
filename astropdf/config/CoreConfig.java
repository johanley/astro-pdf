package astropdf.config;

import astropdf.util.Dimensions;

/** 
 Minimum configuration items needed by the system.
 This interface exists mostly to simply document what is indeed required. 
*/
public interface CoreConfig {

  public String title();
  
  public String author();
  
  /** The width and height of the document/page (inches). */
  public Dimensions pageDimensions();
  
  /** Simple description of the observer's location. */
  public String location();
  
  /** The observer's geographical latitude. Radians. */
  public Double latitude();
  
  /** The observer's geographical longitude. Radians. */
  public Double longitude();
  
  /** How many hours between the observer's time zone and the prime meridian. */
  public Integer hoursOffsetFromUT();
  
  /** 
   How many minutes (0..59) to be added to {@link #hoursOffsetFromUT()}.
   For most jurisdictions, this number is 0, since most time zones are offset by a whole
   number of hours from UT. 
  */
  public Integer minutesOffsetFromUT();

  /** Base name for output files, to which file extensions are appended. */
  public String outputFileName();
  
  /**
   The output directory for generated files. The directory must already exist.
   This setting may not be needed in a servlet environment, where the files are served as a byte stream to the browser. 
  */
  public String outputDir();
  
  /** The directory in which to run a command to convert the output .ps file to a .pdf file. */
  public String ghostscriptBinDir();

}
