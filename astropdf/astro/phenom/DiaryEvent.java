package astropdf.astro.phenom;

import java.time.LocalDateTime;

/** A diary event. The time can be in different levels of precision. */
public final class DiaryEvent {
  
  /** This may contain no time information, or it may be precise to the second. */
  public LocalDateTime when;
  
  public String text = "";
  
  public boolean isImportant = false;
  
  @Override public String toString() {
    return when + " " + text;
  }
  
}