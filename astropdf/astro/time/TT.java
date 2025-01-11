package astropdf.astro.time;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import astropdf.config.Config;
import astropdf.util.DataFileReader;

/**
 <P>The two main ideas are civil time (UTC) and physics time (TT).
 
 <P>When leap-seconds are added to UTC, then UTC differs by one more second from TT.
 
<pre>
 local       leap sec's go here                  ET(1984.0)
---|------|---------------------|----------------|-------
 offset  UTC       37s         TAI    32.184s    TT    
</pre>

 <P>After 1984, UTC differs from TT by N + 32.184s, where N is an integer. In 2025, N=37s.

 <P>In this project, <pre>ΔT = TT - UTC = (N + 32.184s)</pre>.
 
 <P>Moments in the deep future can only be represented precisely as TT, because the rotation of the Earth 
 can't be precisely predicted. 
   
 <P>The time UT1 differs by at most 0.9s from UTC, and it varies erratically because of variations
 in the rotation of the Earth. UT1 is ignored by this project.
 
 <P>Most astronomical calculations convert UTC to TT for the main body of the computation.

  
  
 
 <P>This implementation models the amount with a polynomial fit for years in the range 1900..2150.
 For low-precision results, an exact value is not critical.
 
 <P>A hard-coded value is used for the year 2000.
 If more precision is needed, then the caller should look up a precise historical value, and use that instead.
 
 Ref: http://maia.usno.navy.mil/
 Ref: http://eclipse.gsfc.nasa.gov/SEhelp/deltatpoly2004.html, which models UT1, not UTC.
*/
public final class TT {

  /** This amount is fixed 'by decree'. */
  private static double TT_minus_TAI = 32.184; //seconds
  
  /** The underlying data file should be retrieved yearly. */ 
  private static List<Double> LEAP_SECONDS_DATES = leapSecondDates();
  
  /**
   The current value of ΔT = TT-UTC, as of this writing (2024-12-16) - {@value}.
   MOST CALCULATIONS FOR THIS PROJECT SHOULD USE THIS VALUE.
   When leap-seconds are added, this value increases by exactly 1.0 seconds. 
  */
  public static double CURRENT_ΔT = 37.0 + TT_minus_TAI; //seconds
  
  /** Convert a Julian date in the civil calendar to a Julian Date (TT). */
  public static double jd_TT_From(double jd, Config config) {
    return jd + ΔT(jd, config) / AstroUtil.SECONDS_PER_DAY;
  }
  
  /** Convert a Julian date in the civil calendar to a Julian Ephemeris Date (TT), using an estimated value for ΔT. */
//  public static double jd_TT_From(double jd, int year) {
//    return jd + ΔT(year) / AstroUtil.SECONDS_PER_DAY; 
//  }
  
  /** 
   Use a tabular value from 1972 until the present year + 2.
   Outside of that range, a polynomial approximation is used. 
  */
  private static double ΔT(double jd, Config config) {
    double result = CURRENT_ΔT; //default for safety
    double firstLeapSecond = LEAP_SECONDS_DATES.get(0);
    double mostRecentLeapSecond = LEAP_SECONDS_DATES.get(LEAP_SECONDS_DATES.size() - 1);
    if (firstLeapSecond <= jd && jd <= (mostRecentLeapSecond + 2*AstroUtil.DAYS_PER_JULIAN_YEAR)) {
      result = numLeapSeconds(jd) + TT_minus_TAI;
    }
    else {
      int year = GregorianCal.localCalendarDateFrom(jd, config).getYear();
      result = ΔTpolynomial(year);
    }
    return result;
  }
  
  /** The number of leap seconds that came before the given JD. */
  private static int numLeapSeconds(double jd) {
    int result = 0;
    for(Double leapSecondDate : LEAP_SECONDS_DATES) {
      if (jd >= leapSecondDate) {
        ++result;
      }
    }
    return result;
  }
  
  /** 
   Approximate values for ΔT = TT-UT1, for years in the range 1900..2150.
   (In the olden days, UTC was not yet defined.)
   
   <P>WARNING: This method should not be used for most calculations made with this tool. 
   Instead, you should usually use the {@link #CURRENT_ΔT} value.
  */
  public static double ΔTpolynomial(int year) {
    double result = 69.184; //current value at time of writing; a default, for safety 69.184 (since 2017).
    
    //first the polynomial approximation    
    if (year >= 2150){
     double u = (year-1820)/100;    
     result = -20 + 32 * Math.pow(u,2);
    }
    else if (year >= 2050){
      result = -20 + 32 * Math.pow((year-1820)/100, 2) - 0.5628 * (2150 - year);    
    }
    else if (year >= 2005){
     double t = year - 2000;    
     result = 62.92 + 0.32217 * t + 0.005589 * Math.pow(t,2);
    }
    else if (year >= 1986){
      double t = year - 2000;    
      result = 63.86 + 0.3345 * t - 0.060374 * Math.pow(t,2) + 0.0017275 * Math.pow(t,3) + 0.000651814 * Math.pow(t,4) + 0.00002373599 * Math.pow(t,5);
    }
    else if (year >= 1961){
      double t = year - 1975;    
      result = 45.45 + 1.067*t - Math.pow(t,2)/260 - Math.pow(t,3) / 718;
    }
    else if (year >= 1941){
      double t = year - 1950;    
      result = 29.07 + 0.407*t - Math.pow(t,2)/233 + Math.pow(t,3) / 2547;
    }
    else if (year >= 1920){
      double t = year - 1920;    
      result = 21.20 + 0.84493*t - 0.076100 * Math.pow(t,2) + 0.0020936 * Math.pow(t,3);
    }
    else {
      double t = year - 1900;    
      // uses the estimate from 1900..1920 as the value for all other past dates as well, as a simple approximation
      result = -2.79 + 1.494119 * t - 0.0598939 * Math.pow(t,2) + 0.0061966 * Math.pow(t,3) - 0.000197 * Math.pow(t,4);
    }
    
    //for j2000, overwrite with the actual measurement
    if (year == 2000){
      //http://maia.usno.navy.mil/ser7/deltat.data
      result = 63.828 ; 
    }
    
    return result;
  }
  
  /** Return the list of JDs that had a leap second. */
  private static List<Double> leapSecondDates() {
    List<Double> result = new ArrayList<>();
    DataFileReader reader = new DataFileReader();
    List<String> lines = reader.readFileUTF8(TT.class, "leap_second.txt");
    for(String line : lines) {
      if (!line.startsWith(DataFileReader.COMMENT)) {
        //MJD        D  M YYYY      TAI-UTC
        //41317.0    1  1 1972       10
        String[] parts = line.trim().split(" ");
        String mjd = parts[0].trim();
        Double jd = Double.valueOf(mjd) + AstroUtil.MODIFIED_JULIAN_DATE_BASE;
        result.add(jd);
      }
    }
    return result;
  }
}
