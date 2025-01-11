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
 <P>Finds the local time that an object has a specific azimuth. Returns the altitude as well.
 These times are treated as being unaffected by refraction.
 
 <P>This can be used to find time of transit. 
 
 <P>An object may not attain a given azimuth on a given day day.
 This is especially true of the Moon, which moves quickly. 
*/
public final class AzimuthEvent {

  /**
   Constructor. 
   
   @param bracketMinutes a positive duration in minutes used to bracket the target event.
   Within this bracket, linear interpolation is used. To increase accuracy, use a smaller value.
   @param targetValue an azimuth in degrees. 
  */
  public AzimuthEvent(Config config, int bracketMinutes, double targetValue) {
    this.config = config;
    if (bracketMinutes < 1) {
      throw new IllegalArgumentException("Bracket minutes must be 1 or more: " + bracketMinutes);
    }
    this.bracketMinutes = bracketMinutes;
    this.targetValue = Maths.degToRads(targetValue);
  }
 
  /** The result of this calculation. */
  public static final class Data {
    public LocalDateTime time;
    /** Degrees. */
    public Double altitude;
  }
  
  /**
   Search for an event on the given date (if any). 
   Return the local date-time of the event, along with the altitude of the object expressed in degrees.
   The time is local standard time, expressed using the location's offset from UT.
  
   @param date according to the location.
   @param jdToPosition the function that returns the position of the celestial object.
   @return local date and time of the event, plus its altitude in degrees. 
   If no event happens for that day, then return null.
  */
  public Optional<Data> searchFor(LocalDate date, Function<Double, Position> jdToPosition){
    Optional<Data> result = Optional.empty();
    List<DataPoints> dataPoints = dataForTheGiven(date, jdToPosition);
    Optional<Bracket> eventBracket = bracketInWhichTheEventOccurs(dataPoints);
    if (eventBracket.isPresent()) {
      result = Optional.of(linearInterpolationWithinThe(eventBracket.get(), jdToPosition));
    }
    return result;
  }
  
  private Config config;
  private double targetValue;
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
    for(int minutes = 0; minutes <= 24 * 60; ++minutes) { // include 24h, in case the event happens in the final hour
      if (minutes % bracketMinutes == 0) {
        LocalDateTime local = midnight.plusMinutes(minutes);
        AltAz altaz = altAzFrom(local, jdToPosition);
        
        result.add(new DataPoints(local, altaz.A));
        
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
  private Optional<Bracket> bracketInWhichTheEventOccurs(List<DataPoints> targetData) {
    Bracket result = null;
    //examine pair-wise; see if the target value is between the start and end
    for(int i = 0; i < targetData.size() - 1; ++i) {
      DataPoints start = targetData.get(i);
      DataPoints end = targetData.get(i+1); //pair-wise (the last one never starts a pair)
      
      if(start.value <= targetValue && targetValue <= end.value) {
        result = new Bracket(start, end);
        break;
      }
      
    }
    return Optional.ofNullable(result);
  }

  /** This might be replaced with Newton-Raphson search, or with a quadratic interplolation instead of a linear one. */
  private Data linearInterpolationWithinThe(Bracket bracket,  Function<Double, Position> jdToPosition) {
    double p = (targetValue - bracket.start.value) / (bracket.end.value - bracket.start.value); //fraction of the bracket
    long seconds = Math.round(p * bracketMinutes * 60); 
    LocalDateTime when = bracket.start.time.plusSeconds(seconds);
    AltAz altaz = altAzFrom(when, jdToPosition);
    Data data = new Data();
    data.time = when;
    
    data.altitude = Maths.radsToDegs(altaz.h);
    
    return data;
  }
  
  /** Informal test harness. */ 
  @SuppressWarnings("unused")
  public static void main(String... args) {
    //this depends on Config settings
    //for comparison with tables, you'll need to config a location on the prime meridian
    System.out.println("Testing transit logic.");
    Config config = new ConfigFromFile().init();
    System.out.println(config);
    AzimuthEvent rise = new AzimuthEvent(config, 1, 180);
    LunarPosition moon = new LunarPosition();
    SolarPosition sun = new SolarPosition();
    //Optional<Data> event = rise.searchFor(LocalDate.parse("2024-12-15"), sun::apparentPosition);
    Optional<Data> event = rise.searchFor(LocalDate.parse("2024-12-16"), moon::position);
    if (event.isPresent()) {
      System.out.println("Event: " + event.get().time + ": " + event.get().altitude);
    }
    else {
      System.out.println("No event found.");
    }
  }
}
