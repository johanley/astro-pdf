package astropdf.astro.coords;

/** Heliocentric ecliptical coords. */
public enum Coord {
  
  L, B, R;
  
  /** Map 1,2,3 into L,B,R. */
  public static Coord valueFrom(int idx) {
    return values()[idx -1 ];
  }
}
