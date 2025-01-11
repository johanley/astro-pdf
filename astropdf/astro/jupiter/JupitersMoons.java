package astropdf.astro.jupiter;

import static astropdf.math.Maths.degToRads;
import static astropdf.math.Maths.sqr;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import astropdf.astro.time.TT;
import astropdf.astro.time.GregorianCal;
import astropdf.config.Config;
import astropdf.config.ConfigFromFile;
import astropdf.math.Maths;

/** 
The XY positions of the Galilean satellites of Jupiter relative to the apparent disk of the planet.
Expressed in terms of the size of Jupiter's apparent disk. 
*/
public final class JupitersMoons {

  public static final class SatPosition {
    public double x;
    public double y;
  }
  
  /** Uses the current value of ΔT. */
  public Map<String/*name*/, SatPosition> findAll(LocalDateTime dateTime, Config config){
    //Meeus 1991 Astronomical Algorithms page 285
    //BE CAREFUL WITH THE UNITS! degrees vs rads, and the use of custom 'sine' and 'cosine'
    //double jde = TT.jd_TT_From(GregorianCal.jdForLocal(dateTime, config), config);
    double jde = TT.jd_TT_From(GregorianCal.jdForLocal(dateTime, config), config); //FOR USE WITH THE TEST CASE
    double d = jde - 2451545.0; //since Jan 1 2000
    double V = Maths.in360(172.74 + 0.00111588 * d); //deg
    double M = Maths.in360(357.529 + 0.9856003 * d); //deg
    double N = Maths.in360(20.020 + 0.0830853 * d + 0.329 * sine(V)); //deg
    double J = Maths.in360(66.115 + 0.9025179 * d - 0.329 * sine(V)); //deg
    double A = 1.915 * sine(M) + 0.020 * sine(2*M); //deg
    double B = 5.555 * sine(N) + 0.168 * sine(2*N); //deg
    double K = J + A - B; //deg
    double R = 1.00014 - 0.01671 * cosine(M) - 0.00014 * cosine(2*M); //AU
    double r = 5.20872 - 0.25208 * cosine(N) - 0.00611 * cosine(2*N); //AU
    double Δ = Math.sqrt(sqr(r) + sqr(R) - 2*r*R*cosine(K)); //AU
    double ψ = Maths.radsToDegs(Math.asin((R / Δ) * sine(K))); //rads -pi/2..+pi/2; always -12..+12 in this case
    //page 282
    double λ = 34.35 + 0.083091 * d  + 0.329 * sine(V) + B; //deg 
    double DS = 3.12 * sine(λ + 42.8); //deg
    double DE = DS - 2.22 * sine(ψ) * cosine(λ + 22) - 1.30 * ((r - Δ) / Δ) * sine(λ - 100.5); //degs
    
    double dd = d - Δ/173.0;
    double u1 = u(163.8067, 203.4058643, dd, Δ, ψ, B); //deg
    double u2 = u(358.4108, 101.2916334, dd, Δ, ψ, B); //deg
    double u3 = u(5.7129, 50.2345179, dd, Δ, ψ, B); //deg
    double u4 = u(224.8151,21.4879801, dd, Δ, ψ, B); //deg
    double G = 331.18 + 50.310482 * dd; //deg
    double H = 87.40 + 21.569231 * dd; //deg
    double u1_corr = u1 + 0.473 * sine(2*(u1 - u2)); //interactions between satellites!
    double u2_corr = u2 + 1.065 * sine(2*(u2 - u3));
    double u3_corr = u3 + 0.165 * sine(G);
    double u4_corr = u4 + 0.841 * sine(H);
    double r1 = 5.9073 - 0.0244 * cosine(2*(u1 - u2)); //uncorrected u's!
    double r2 = 9.3991 - 0.0822 * cosine(2*(u2 - u3));
    double r3 = 14.9924 - 0.0216 * cosine(G);
    double r4 = 26.3699 - 0.1935 * cosine(H);
    
    Map<String , SatPosition> result = new LinkedHashMap<>();
    result.put("1", satPos(r1, u1_corr, DE));
    result.put("2", satPos(r2, u2_corr, DE));
    result.put("3", satPos(r3, u3_corr, DE));
    result.put("4", satPos(r4, u4_corr, DE));
    
    return result;
  }
  
  private double u(double a, double b, double dd, double Δ, double ψ, double B) {
    double result = a + b * dd + ψ - B; //deg
    return Maths.in360(result);
  }
  
  private double sine(double deg) {
    return Math.sin(degToRads(deg));
  }
  
  private double cosine(double deg) {
    return Math.cos(degToRads(deg));
  }
  
  private SatPosition satPos(double r, double u, double DE) {
    SatPosition result = new SatPosition();
    result.x = r * sine(u);
    result.y = -r * cosine(u) * sine(DE);
    return result;
  }
  
  public static void main(String[] args) {
    JupitersMoons moons = new JupitersMoons();
    Config config = new ConfigFromFile().init();
    moons.findAll(LocalDateTime.parse("1992-12-15T20:00:00"), config);
  }
}
