package astropdf.config;

import static astropdf.astro.time.AstroUtil.DEGREES_PER_HOUR;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import astropdf.astro.time.AstroUtil;
import astropdf.jurisdiction.Jurisdiction;
import astropdf.lang.Lang;
import astropdf.math.Maths;
import astropdf.util.Constants;
import astropdf.util.Dimensions;
import astropdf.util.Util;

/** 
 Configuration data for the generation of the PDF.
 
 The configuration data can come from different sources. 
 When run as a standalone program, the data can come from a text file.
 In a servlet environment, it may come from user input in a web form.
 
 <P>All fields are immutable objects.
 <P>This object is 'pilgrim data'. It's passed around from the top-level object to many lower-level objects.
 This design avoids storing data in static fields. That design can be used both in a standalone program, and in 
 server environments. 
*/
public final class Config implements CoreConfig {
  
  public Config(
    String title, String author, String pageDimensions,
    Integer year, LocalDate startMonday, LocalDate endSunday, String lang, String location, Double latitude, Double longitude, 
    Integer hoursOffsetFromUT, Integer minutesOffsetFromUT, Double magneticDeclination, String tidePredictions,
    String weatherJurisdiction, String weatherStationId, String weatherStartMonth, String weatherEndMonth, List<String> skyDiaryIgnorableObjects,
    String outputFileName, String outputDir, String ghostScriptBinDir 
  ){
    this.title = title;
    this.author = author;
    this.pageDimensions = new Dimensions(pageDimensions);
    this.year = year;
    this.startMonday = startMonday;
    this.endSunday = endSunday;
    this.lang = Util.textHasContent(lang) ? Lang.valueOf(lang.trim()) : Lang.en;
    this.location = location;
    this.latitude = latitude;
    this.longitude = longitude;
    this.hoursOffsetFromUT = hoursOffsetFromUT;
    this.minutesOffsetFromUT = minutesOffsetFromUT;
    this.magneticDeclination = magneticDeclination;
    this.tidePredictions = tidePredictions;
    this.weatherJurisdiction = Util.textHasContent(weatherJurisdiction) ? Jurisdiction.valueOf(weatherJurisdiction) : null;
    this.weatherStationId = weatherStationId;
    this.weatherStartMonth = weatherStartMonth;
    this.weatherEndMonth = weatherEndMonth;
    this.skyDiaryIgnorableObjects = skyDiaryIgnorableObjects;
    this.outputFileName = outputFileName;
    this.outputDir = outputDir;
    this.ghostscriptBinDir = ghostScriptBinDir;
    validate();
  }

  /** Text on the title page. */
  public String title() { return title; }
  
  /** The creator of the PDF (person or organization). */
  public String author() { return author; }
  
  /** The width and height of the document/page (inches). */
  public Dimensions pageDimensions() { return pageDimensions; }
  
  /** The core year for the almanac. */
  public Integer year() { return year; }
  
  /** The date yyyy-mm-dd on which to start the almanac. It must be a Monday. */
  public LocalDate startMonday() { return startMonday; }
  
  /** The date yyyy-mm-dd on which to endt the almanac. It must be a Sunday. */
  public LocalDate endSunday() { return endSunday; }
  
  /** Language code for the locale, used to translate text. */
  public Lang lang() {return lang; }
  
  /** Locale corresponding to the language. */
  public Locale locale() {return Locale.forLanguageTag(lang.name()); }
  
  /** Simple description of the observer's location. */
  public String location() {return location;  }
  
  /** The observer's geographical latitude. Radians. */
  public Double latitude() { return latitude; }
  
  /** The observer's geographical longitude. Radians. */
  public Double longitude() { return longitude; }
  
  /** How many hours between the observer's time zone and the prime meridian. */
  public Integer hoursOffsetFromUT() { return hoursOffsetFromUT;  }
  
  /** 
   How many minutes (0..59) to be added to {@link #hoursOffsetFromUT()}.
   For most jurisdictions, this number is 0, since most time zones are offset by a whole
   number of hours from UT. 
  */
  public Integer minutesOffsetFromUT() {  return minutesOffsetFromUT;  }
  
  /** The difference in azimuth between magnetic north and true north, in degrees. Negative towards the west. */
  public Double magneticDeclination() { return magneticDeclination; }
  
  /** Names of files containing tide prediction data. */
  public String tidePredictions() { return tidePredictions; }
  
  public Jurisdiction weatherJurisdiction() { return weatherJurisdiction; }
  /** A code used by Environment Canada to identify a weather station. */
  public String weatherStationId() { return weatherStationId; }
  /** The month for which the system starts its weather stats. */
  public String weatherStartMonth() { return weatherStartMonth; }
  /** The month for which the system ends its weather stats. */
  public String weatherEndMonth() { return weatherEndMonth; }

  /** Celestial objects to be suppressed in the listing of daily events. */
  public List<String> skyDiaryIgnorableObjects() { return skyDiaryIgnorableObjects; }

  /** Base name for output file, to which file extensions are appended. */
  public String outputFileName() { return outputFileName; }
  /** Full name for the output file, to which file extensions are appended. */
  public String outputFullFileName(String extension) { return outputDir + File.separator + outputFileName + extension; }
  
  /**
   The output directory for generated files. The directory must already exist.
   This setting may not be needed in a servlet environment, where the files are served as a byte stream to the browser. 
  */
  public String outputDir() { return outputDir; }
  
  /** The directory in which to run Ghostscript's ps2pdf command. */
  public String ghostscriptBinDir() { return ghostscriptBinDir; }
  
  /** Radians. Calculated field. */
  public Double radsWestOfCentralMeridian() {
    double hours = hoursOffsetFromUT() + minutesOffsetFromUT()/60.0; //avoid integer division!
    double degrees = hours * DEGREES_PER_HOUR;
    double timeZoneCentralLongitude = Maths.degToRads(degrees); 
    return timeZoneCentralLongitude - longitude; // -60 - (-63) = 3 deg
  }
  
  /** Calculated field. */
  public boolean isNorthernHemisphere() { return latitude >= 0; }
  
  /** For debugging. All config settings. */
  @Override public String toString() {
    StringBuilder result = new StringBuilder();
    toStringLine(Setting.output_file_name, outputFileName(), result);
    toStringLine(Setting.output_dir, outputDir(), result);
    toStringLine(Setting.title, title(), result);
    toStringLine(Setting.author, author(), result);
    toStringLine(Setting.page_dimensions, pageDimensions(), result);
    toStringLine(Setting.year, year(), result);
    toStringLine(Setting.start_monday, startMonday(), result);
    toStringLine(Setting.end_sunday, endSunday(), result);
    toStringLine(Setting.lang, lang(), result);
    toStringLine(Setting.location, location(), result);
    toStringLine(Setting.latitude, AstroUtil.radsToDegreeString(latitude()), result);
    toStringLine(Setting.longitude, AstroUtil.radsToDegreeString(longitude()), result);
    toStringLine(Setting.hours_offset_from_ut, hoursOffsetFromUT(), result);
    toStringLine(Setting.minutes_offset_from_ut, minutesOffsetFromUT(), result);
    toStringLine(Setting.degrees_west_of_central_meridian, AstroUtil.radsToDegreeString(radsWestOfCentralMeridian()), result);
    toStringLine(Setting.magnetic_declination, magneticDeclination(), result);
    toStringLine(Setting.tide_predictions, tidePredictions(), result);
    toStringLine(Setting.weather_jurisdiction, weatherJurisdiction(), result);
    toStringLine(Setting.weather_station_id, weatherStationId(), result);
    toStringLine(Setting.weather_start_month, weatherStartMonth(), result);
    toStringLine(Setting.weather_end_month, weatherEndMonth(), result);
    toStringLine(Setting.sky_diary_ignorable_objects, skyDiaryIgnorableObjects(), result);
    toStringLine(Setting.ghostscript_bin_dir, ghostscriptBinDir(), result);
    return result.toString().trim();
  }

  private String title = "";
  private String author = "";
  private Dimensions pageDimensions;
  private Integer year;
  private LocalDate startMonday;
  private LocalDate endSunday;
  private Lang lang = Lang.en; //default!
  private String location = "";
  private Double latitude;
  private Double longitude;
  private Integer hoursOffsetFromUT;
  private Integer minutesOffsetFromUT;
  private Double magneticDeclination;
  private String tidePredictions = "";
  private Jurisdiction weatherJurisdiction;
  private String weatherStationId = "";
  private String weatherStartMonth = "";  
  private String weatherEndMonth = "";
  private List<String> skyDiaryIgnorableObjects = new ArrayList<>();
  private String outputFileName = "";
  private String outputDir = "";
  private String ghostscriptBinDir = "";
  
  private void toStringLine(Setting setting, Object value, StringBuilder result) {
    if (value != null) {
      result.append("  " + setting.toString() + " = " + value.toString() + Constants.NL); 
    }
  }
  
  @SuppressWarnings("serial")
  private static final class ConfigException extends RuntimeException{
    ConfigException(String reason){
      super(reason);
    }
  }
  
  private void validate() throws ConfigException {
    if (startMonday != null && !startMonday.isBefore(endSunday)) {
      throw new ConfigException("start-monday is not before end-sunday.");
    }
    if (startMonday != null && startMonday.getDayOfWeek() != DayOfWeek.MONDAY) {
      throw new ConfigException("start-monday is not a Monday. It's a " + startMonday.getDayOfWeek());
    }
    if (endSunday != null && endSunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
      throw new ConfigException("end-sunday is not a Sunday. It's a " + endSunday.getDayOfWeek());
    }
  }
}
