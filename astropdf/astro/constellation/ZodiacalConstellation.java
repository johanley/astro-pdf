package astropdf.astro.constellation;

import astropdf.math.Maths;

public enum ZodiacalConstellation {
  
  Pisces("Pisces", "Psc"),
  Aries("Aries", "Ari"),
  Taurus("Taurus", "Tau"),
  Gemini("Gemini", "Gem"),
  Cancer("Cancer", "Cnc"),
  Leo("Leo", "Leo"), 
  Virgo("Virgo", "Vir"),
  Libra("Libra", "Lib"),
  Scorpius("Scorpius", "Sco"),
  Sagittarius("Sagittarius", "Sgr"),
  Capricorn("Capricorn", "Cap"),
  Aquarius("Aquarius", "Aqr");
  
  public static ZodiacalConstellation fromλ(double λ) {
    ZodiacalConstellation result = Pisces; //a default (since it's the weird one)
    double degs = Maths.radsToDegs(λ);
    //the result here can be overwritten repeatedly, until the correct value is reached
    if (degs > 31.0) { result = Aries; }
    if (degs > 53.0) { result = Taurus; }
    if (degs > 88.0) { result = Gemini; }
    if (degs > 118.0) { result = Cancer; }
    if (degs > 138.0) { result = Leo; }
    if (degs > 171.0) { result = Virgo; }
    if (degs > 218.0) { result = Libra; }
    if (degs > 239.0) { result = Scorpius; }
    if (degs > 268.0) { result = Sagittarius; }
    if (degs > 295.0) { result = Capricorn; }
    if (degs > 327.0) { result = Aquarius; }
    if (degs > 351.0) { result = Pisces; }
    return result;
  }

  /** The English name! Needs translation if presented to the user. */
  public String fullName() { return fullName; }
  
  /** This is an IAU standard abbreviation. */
  public String abbr() { return abbr; }
  
  private ZodiacalConstellation(String fullName, String abbr) {
    this.fullName = fullName;
    this.abbr = abbr;
  }
  
  private String fullName;
  private String abbr;
}