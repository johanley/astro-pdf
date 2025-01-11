package astropdf.config;

import static astropdf.util.LogUtil.log;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import astropdf.math.Maths;
import astropdf.util.DataFileReader;

/** Use a text file as the source of configuration data. */
public final class ConfigFromFile {

  /** 
   Read the <code>config.ini</code> file (encoded in UTF8) and return a corresponding object.
   An init() METHOD MUST BE CALLED IMMEDIATELY UPON STARTUP.
   The returned object will be passed around to many other classes.
 
   <P>By default, the config.ini file is located in the same directory as the caller.
   To use an explicit file name for the config file, use the command line setting: 
   {@code -DastropdfConfigFile=C:\mydirectory\myfile}
  */
  public Config init(Object caller) {
    DataFileReader reader = new DataFileReader();
    String fileLocationOverride = System.getProperty("astropdfConfigFile");
    List<String> lines = null;
    if (fileLocationOverride == null) {
      log("Reading config file: " + CONFIG_INI);
      lines = reader.readFileUTF8(caller.getClass(), CONFIG_INI);
    }
    else {
      log("Reading config file: " + fileLocationOverride);
      lines = reader.readFileUTF8(fileLocationOverride);
    }

    for(String line : lines) {
      processEach(line.trim());
    }
    return buildConfigObjectFromSettings();
  }
  
  /** 
   As in {@link #init()}, but use the default directory is in the same directory as this class.
   This is intended for testing purposes. 
  */
  public Config init() {
    return init(this);
  }

  // PRIVATE 

  private static final String CONFIG_INI = "config.ini";
  private static final String SEPARATOR = "=";
  private static final int NAME = 0;
  private static final int VALUE = 1;

  private String title;
  private String author;
  private String pageDimensions;
  private Integer year;
  private LocalDate startMonday;
  private LocalDate endSunday;
  private String location = "";
  private String lang = "";
  private Double latitude;
  private Double longitude;
  private Integer hoursOffsetFromUT;
  private Integer minutesOffsetFromUT;
  private Double magneticDeclination;
  private String tidePredictions;
  private String weatherJurisdiction = "";
  private String weatherStationId = "";
  private String weatherStartMonth = "";
  private String weatherEndMonth = "";
  private List<String> skyDiaryIgnorableObjects = new ArrayList<>();
  private String outputFileName = "";
  private String outputDir = "";
  private String ghostscriptBinDir = "";  

  private void processEach(String line) {
    if (line.startsWith(DataFileReader.COMMENT) || line.length() == 0) {
      //ignore it!
    }
    else {
      String[] parts = line.split(Pattern.quote(SEPARATOR));
      if (matches(Setting.output_dir, parts)) {
        outputDir = asString(parts);
      }
      else if (matches(Setting.title, parts)) {
        title = asString(parts);
      }
      else if (matches(Setting.author, parts)) {
        author = asString(parts);
      }
      else if (matches(Setting.page_dimensions, parts)) {
        pageDimensions = asString(parts);
      }
      else if (matches(Setting.year, parts)) {
        year = asInteger(parts);
      }
      else if (matches(Setting.start_monday, parts)) {
        startMonday = asLocalDate(parts);
      }
      else if (matches(Setting.end_sunday, parts)) {
        endSunday = asLocalDate(parts);
      }
      else if (matches(Setting.lang, parts)) {
        lang = asString(parts);
      }
      else if (matches(Setting.location, parts)) {
        location = asString(parts);
      }
      else if (matches(Setting.latitude, parts)) {
        latitude = asRads(parts);
      }
      else if (matches(Setting.longitude, parts)) {
        longitude = asRads(parts);
      }
      else if (matches(Setting.hours_offset_from_ut, parts)) {
        hoursOffsetFromUT = asInteger(parts);
      }
      else if (matches(Setting.minutes_offset_from_ut, parts)) {
        minutesOffsetFromUT = asInteger(parts);
      }
      else if (matches(Setting.magnetic_declination, parts)) {
        magneticDeclination = asDouble(parts);
      }
      else if (matches(Setting.tide_predictions, parts)) {
        tidePredictions = asString(parts);
      }
      else if (matches(Setting.weather_jurisdiction, parts)) {
        weatherJurisdiction = asString(parts);
      }
      else if (matches(Setting.weather_station_id, parts)) {
        weatherStationId = asString(parts);
      }
      else if (matches(Setting.weather_start_month, parts)) {
        weatherStartMonth = asString(parts);
      }
      else if (matches(Setting.weather_end_month, parts)) {
        weatherEndMonth = asString(parts);
      }
      else if (matches(Setting.sky_diary_ignorable_objects, parts)){
        skyDiaryIgnorableObjects = asStringList(parts);
      }
      else if (matches(Setting.ghostscript_bin_dir, parts)) {
        ghostscriptBinDir = asString(parts);
      }
      else if (matches(Setting.output_file_name, parts)) {
        outputFileName = asString(parts);
      }
    }
  }
  
  private boolean matches(Setting setting, String[] parts) {
    return parts[NAME].trim().equalsIgnoreCase(setting.toString());
  }

  private String asString(String[] parts) {
    return parts[VALUE].trim();
  }
  
  private List<String> asStringList(String[] parts){
    List<String> result = new ArrayList<>();
    String list = parts[VALUE].trim();
    for(String item : list.split(Pattern.quote(","))) {
      result.add(item.trim());
    }
    return result;
  }

  
  private Double asDouble(String[] parts) {
    return Double.valueOf(asString(parts));
  }
  
  private Double asRads(String[] parts) {
    return Maths.degToRads(asDouble(parts));
  }
  
  private Integer asInteger(String[] parts) {
    return Integer.valueOf(asString(parts));
  }
  
  /** Text date must have the format '2024-07-01'. */
  private LocalDate asLocalDate(String[] parts) {
    return LocalDate.parse(parts[VALUE].trim());
  }
  
  private Config buildConfigObjectFromSettings() {
    return new Config(
      title, author, pageDimensions, year, startMonday, endSunday, lang, location, latitude, longitude, hoursOffsetFromUT, minutesOffsetFromUT, 
      magneticDeclination, tidePredictions, weatherJurisdiction, weatherStationId, weatherStartMonth, weatherEndMonth,
      skyDiaryIgnorableObjects, outputFileName, outputDir, ghostscriptBinDir 
    );
  }
}
