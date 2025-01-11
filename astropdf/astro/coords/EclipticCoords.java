package astropdf.astro.coords;

import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

import astropdf.astro.constellation.ZodiacalConstellation;
import astropdf.astro.precession.LongTermPrecession;
import astropdf.math.Maths;

/*** Struct for celestial longitude and latitude, with respect to the ecliptic. */
public final class EclipticCoords {
  
  /** Radians. Latitude. */
  public double β;
  
  /** Radians. Longitude. */
  public double λ;
  
  public EclipticCoords(Double λ, Double β) {
    this.λ = λ;
    this.β = β;
  }
  
  public EclipticCoords() { }
  
  public Position toRaDec(double jd) {
    Position result = new Position();
    LongTermPrecession precession = new LongTermPrecession();
    double ε = precession.obliquity(jd);
    result.δ = asin(sin(β)*cos(ε) + cos(β)*sin(ε)*sin(λ)); //rads, -pi/2..pi/2
    double numer = sin(λ)*cos(ε) - tan(β)*sin(ε);
    double denom = cos(λ);
    result.α = Maths.atan3(numer, denom); //rads, 0..2pi
    return result;
  }
  
  public ZodiacalConstellation toZodiacalConstellation() {
    return ZodiacalConstellation.fromλ(λ);
  }
  
  /** Debugging only. */
  @Override public String toString() {
    return "λ :" + Maths.radsToDegs(λ) + " β:" + Maths.radsToDegs(β);
  }
}
