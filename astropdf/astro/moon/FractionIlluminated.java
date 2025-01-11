package astropdf.astro.moon;

import static java.lang.Math.acos;
import static java.lang.Math.cos;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import astropdf.astro.coords.EclipticCoords;
import astropdf.astro.planets.SolarPosition;
import astropdf.astro.time.GregorianCal;
import astropdf.config.Config;
import astropdf.math.Maths;
import astropdf.util.Constants;
import astropdf.util.LogUtil;

/** 
 The fraction of the area of the Moon's disk that is illuminated, 
 and whether the Moon is waxing or waning.
*/
public final class FractionIlluminated {
  
  /** Dumb data carrier. */
  public static final class Data {
    public Double fracIllumin;
    public Boolean isWaxing;
  }
  
  public FractionIlluminated(SolarPosition solarPosition, LunarPosition lunarPosition) {
    this.solarPosition = solarPosition;
    this.lunarPosition = lunarPosition;
  }
  
  /** Return the fraction illumintaed for every day of the year at 20h, in the configured offset from UT. */
  public Map<LocalDate, Data> forEveryDayOfTheYear(Config config){
    Map<LocalDate, Data> result = new LinkedHashMap<>();
    LocalDate day = LocalDate.of(config.year(), 1, 1); //start with Jan 1
    while (day.getYear() == config.year()) {
      Double jd = GregorianCal.jdForLocal(
        day.getYear(), day.getMonthValue(), day.getDayOfMonth(), Constants.LOCAL_EVENING_HOUR, 0, 0, 0, 
        config.hoursOffsetFromUT(), config.minutesOffsetFromUT()
      );
      Data data = fraction(jd);
      result.put(day, data);
      day = day.plusDays(1);
    }
    return result;
  }
  
  /** 
   Returns a fraction between 0.00 and 1.00, inclusive.
   This approximate implementation has a resulting error never greater than 0.14.
   The result here is always rounded to two decimals. 
  */
  public Data fraction(double jd) {
    //see Meeus 1991, page 315
    //we model the Sun as being at infinite distance, which is acceptable in this context
    
    EclipticCoords moon = lunarPosition.λβ(jd);
    double λ0 = solarPosition.apparentλ(jd);
    double ψ = acos(cos(moon.β) * cos(moon.λ - λ0)); //0..pi
    double i = Math.PI - ψ; //phase angle = selenographic elongation of the Earth from the Sun; 0..pi
    double fraction = (1 + cos(i)) * 0.5;
    //avoid an approximation issue that looks bad to the user, should it occur (rare, if ever)
    if (fraction > 1.0) {
      fraction = 1.0;
    }
    fraction = Maths.round(fraction, 2); //0.45, for example
    double diff = Maths.in2pi(moon.λ - λ0);
    Data data = new Data();
    data.fracIllumin = fraction;
    data.isWaxing = (diff <= Math.PI);
    return data;
  }

  //using these fields means these objects won't repeatedly read the file system
  private SolarPosition solarPosition;
  private LunarPosition lunarPosition;
  
  public static void main(String... args) {
    FractionIlluminated frac = new FractionIlluminated(new SolarPosition(), new LunarPosition());
    for(int i = 1; i <= 28; ++i) {
      double jd = GregorianCal.jdForGreenwich(2022, 2, i); //Greenwich
      LogUtil.log(i + ": " + frac.fraction(jd));
    }
  }
}
