package astropdf.astro.phenom;

import static astropdf.math.Maths.in2pi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import astropdf.astro.coords.AltAz;
import astropdf.astro.coords.Position;
import astropdf.astro.moon.LunarPosition;
import astropdf.astro.planets.SolarPosition;
import astropdf.astro.time.GregorianCal;
import astropdf.astro.time.SiderealTime;
import astropdf.astro.time.TT;
import astropdf.config.Config;
import astropdf.config.ConfigFromFile;
import astropdf.math.Maths;

/** 
 Rise, set, and twilight phenomena. 
 
 <P>Finds the local time that an object has a specific altitude (in the east or west), along with its azimuth.
 
 <P>An object may not attain a given altitude on a given day day.
 This is especially true of the Moon, which moves quickly. 
*/
public final class AltitudeEvent {

  /**
   Constructor. 
   
   <p>For predicting sunrise and sunset, use a target altitude of -0.9 degrees.
   This accounts for refraction at the horizon, the Sun's mean semi-diameter, and for the observer being 
   about 2 meters above the real horizon. (See the Explanatory Supplement to the Ephemeris, 1961, page 401).
   Sunrise and sunset correspond to the center of the Sun's disk being at that altitude.
   For the stars, the target altitude is near -0.57 degrees. 
   For the Moon, the target altitude is in the range of +0.08 to +0.18 degrees, because of its large horizontal parallax.
   
   <P>Some test values for the <pre>bracketMinutes</pre>, for sunrise on 2024-12-13 at Charlottetown, PEI:
<pre>
     60m: 07:47:30
     45m: 07:47:26
     30m: 07:49:09
     20m: 07:47:01
     15m: 07:46:57
     10m: 07:46:57
     05m: 07:46:56
     01m: 07:46:55</pre>
   
   @param bracketMinutes a positive duration in minutes used to bracket the target event.
   Within this bracket, linear interpolation is used. To increase accuracy, use a smaller value.
   @param targetAltitude in degrees. This deals with the refraction and parallax when an object is near the horizon.
  */
  public AltitudeEvent(Config config, int bracketMinutes, double targetAltitude) {
    this.config = config;
    if (bracketMinutes < 1) {
      throw new IllegalArgumentException("Bracket minutes must be 1 or more: " + bracketMinutes);
    }
    this.bracketMinutes = bracketMinutes;
    this.targetAltitude = Maths.degToRads(targetAltitude);
  }
 
  /** Target altitude of -0.9 degrees. */
  public static AltitudeEvent forSun(Config config, int bracketMinutes) {
    return new AltitudeEvent(config, bracketMinutes, -0.9);
  }
  
  /** Target altitude of +0.13 degrees. This implementation is not responsive to variations in the Moon's parallax. */
  public static AltitudeEvent forMoon(Config config, int bracketMinutes) {
    return new AltitudeEvent(config, bracketMinutes, 0.13);
  }

  /** The result of this calculation. */
  public static final class Data {
    public LocalDateTime time;
    /** Degrees. */
    public Double azimuth;
  }
  
  public enum Phenom { 
    East(true), West(false);
    private Phenom(boolean isIncreasing) {
      this.isIncreasing = isIncreasing;
    }
    boolean isIncreasing() { return isIncreasing; }
    private boolean isIncreasing;
  }
  
  /**
   Search for an event on the given date (if any). 
   Return the local date-time of the event, along with its azimuth expressed in degrees.
   The time is local standard time, expressed using the location's offset from UT.
  
   @param date according to the location.
   @param jdToPosition the function that returns the position of the celestial object.
   @return local date and time of the event, plus its azimuth in degrees. 
   If no event happens for that day, then return null.
  */
  public Optional<Data> searchFor(Phenom phenom, LocalDate date, Function<Double, Position> jdToPosition){
    Optional<Data> result = Optional.empty();
    List<DataPoints> dataPoints = dataForTheGiven(date, jdToPosition);
    Optional<Bracket> eventBracket = bracketInWhichTheEventOccurs(phenom, dataPoints);
    if (eventBracket.isPresent()) {
      result = Optional.of(linearInterpolationWithinThe(eventBracket.get(), jdToPosition));
    }
    return result;
  }
  
  private Config config;
  private double targetAltitude;
  private int bracketMinutes;

  /** Data points for the target object, using local date-time. */
  private static final class DataPoints {
    DataPoints(LocalDateTime local, Double value){
      this.time = local;
      this.value = value; 
    }
    LocalDateTime time;
    Double value;
  }
  
  /** The data that brackets the target event. */
  private static final class Bracket {
    Bracket(DataPoints start, DataPoints end){
      this.start = start;
      this.end = end;
    }
    DataPoints start;
    DataPoints end;
  }

  /**
   The data for the object on the given local day. 
   Ordered by time. 
  */
  private List<DataPoints> dataForTheGiven(LocalDate localDate, Function<Double, Position> jdToPosition){
    List<DataPoints> result = new ArrayList<>();
    LocalDateTime midnight = LocalDateTime.of(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), 0, 0);
    for(int minutes = 0; minutes <= 24 * 60; ++minutes) { // include 24h, in case the transit happens in the final hour
      if (minutes % bracketMinutes == 0) {
        LocalDateTime local = midnight.plusMinutes(minutes);
        AltAz altaz = altAzFrom(local, jdToPosition);
        result.add(new DataPoints(local, altaz.h));
      }
    }
    return result;
  }
  
  private AltAz altAzFrom(LocalDateTime local, Function<Double, Position> jdToPosition) {
    /*
     Be careful with the exact meaning of the date and time. 
     Need to translate from the observer's time zone into UT.
    */
    SiderealTime sidereal = new SiderealTime(config);
    double lst = sidereal.siderealTime(
      local.getYear(), local.getMonthValue(), local.getDayOfMonth(), 
      local.getHour(), local.getMinute(), local.getSecond(), 
      local.getNano(), config.hoursOffsetFromUT(), config.minutesOffsetFromUT(),
      config.longitude()
    ); // 0..2pi
    double jd = GregorianCal.jdForLocal(local, config);
    double jde = TT.jd_TT_From(jd, config);
    Position pos = jdToPosition.apply(jde);
    double ha = in2pi(lst - pos.α);
    return AltAz.from(ha, pos.δ, config.latitude());
  }

  /** 
   Return the pair which bracket the time of the event.
   WARNING: some days will have no event: the return value may be null. 
  */
  private Optional<Bracket> bracketInWhichTheEventOccurs(Phenom phenom, List<DataPoints> targetData) {
    Bracket result = null;
    //examine pair-wise; see if the target value is between the start and end
    for(int i = 0; i < targetData.size() - 1; ++i) {
      DataPoints start = targetData.get(i);
      DataPoints end = targetData.get(i+1); //pair-wise (the last one never starts a pair)
      if(start.value <= targetAltitude && targetAltitude <= end.value) {
        if (Phenom.East == phenom){
          result = new Bracket(start, end);
          break;
        }
      }
      if(start.value >= targetAltitude && targetAltitude >= end.value) {
        if (Phenom.West == phenom) {
          result = new Bracket(start, end);
          break;
        }
      }
    }
    return Optional.ofNullable(result);
  }

  /** This might be replaced with Newton-Raphson search, or with a quadratic interplolation instead of a linear one. */
  private Data linearInterpolationWithinThe(Bracket bracket,  Function<Double, Position> jdToPosition) {
    double p = (targetAltitude - bracket.start.value) / (bracket.end.value - bracket.start.value); //fraction of the bracket
    long seconds = Math.round(p * bracketMinutes * 60); 
    LocalDateTime when = bracket.start.time.plusSeconds(seconds);
    AltAz altaz = altAzFrom(when, jdToPosition);
    Data data = new Data();
    data.time = when;
    data.azimuth = Maths.radsToDegs(altaz.A);
    return data;
  }
  
  /** Informal test harness. */ 
  @SuppressWarnings("unused")
  public static void main(String... args) {
    //this depends on Config settings
    //for comparison with tables, you'll need to config a location on the prime meridian
    System.out.println("Testing rise-set logic.");
    Config config = new ConfigFromFile().init();
    System.out.println(config);
    AltitudeEvent rise = new AltitudeEvent(config, 10, -0.9);
    LunarPosition moon = new LunarPosition();
    SolarPosition sun = new SolarPosition();
    Optional<Data> event = rise.searchFor(Phenom.West, LocalDate.parse("2024-12-13"), sun::apparentPosition);
    //Optional<Data> event = rise.searchFor(Phenom.Set, LocalDate.parse("2024-12-13"), moon::position);
    if (event.isPresent()) {
      System.out.println("Event: " + event.get().time + ": " + event.get().azimuth);
    }
    else {
      System.out.println("No event found.");
    }
  }
}
