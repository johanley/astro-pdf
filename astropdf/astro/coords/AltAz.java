package astropdf.astro.coords;

import static java.lang.Math.*;
import astropdf.math.Maths;

/** Local altitude and azimuth of a celestial object. */
public final class AltAz {
  
  /** Altitude in radians, -pi/2..+pi/2. */
  public double h;
  
  /** Azimuth in radians 0..2pi, measured from the North, positive to the East. */
  public double A;

  /**
   Altitude and azimuth of an object from hour angle and declination. 
   @param H hour angle of the object
   @param δ declination of the object
   @param φ latitude of the location
  */
  public static AltAz from(double H, double δ, double φ) {
    //Meeus 1991, page 89
    AltAz result = new AltAz();
    double sin_h = sin(φ) * sin(δ) + cos(φ) * cos(δ) * cos(H);
    result.h = Math.asin(sin_h);  //-pi/2..+pi/2
    
    double numer = sin(H);
    double denom = cos(H) * sin(φ) - tan(δ) * cos(φ);
    double A = atan2(numer, denom); //-pi..+pi, measured from the South 
    A = A + Math.PI; // measured from the North
    result.A = Maths.in2pi(A);  //0..2pi
    
    return result;
  }
}