package astropdf.project.weeklyplanner;

import static astropdf.util.Constants.NL;
import static astropdf.util.PostScript.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

import astropdf.pdf.PlaceholderData;
import astropdf.util.PostScript;

/** Data for a yearly calendar. */
final class YearlyCalendar {
  
  PlaceholderData dataFor(Integer year, Locale locale){
    PlaceholderData result = new PlaceholderData();
    result.stringLiteral("YEAR", year.toString());
    result.add("YEARLY-CALENDAR-DATA", calendarDataFor(year, locale));
    result.add("DAY_NAMES", weekdayAbbreviations(locale));
    return result;
  }

  /**
   An array of maps. 
   Each map has one entry, month-name and an array-of-numbers.
   <pre>
    [ 
     <<(January) 
      [() (1) (2) (3) (4) (5) (6) 
      (7) (8) (9) (10) (11) (12) (13) 
      (14)(15) (16) (17) (18) (19) (20) 
      (21)(22) (23) (24) (25) (26) (27) 
      (28) (29) (30) (31) () () () () 
      () () () () () () ()]
     >>
     ...
    ]  </pre>
  */
  private String calendarDataFor(Integer year, Locale locale) {
    StringBuilder result = new StringBuilder();
    for(Month month : Month.values()) {
      result.append(monthData(year, month, locale));  
    }
    return PostScript.array(result.toString());
  }
  
  private String monthData(Integer year, Month month, Locale locale) {
    StringBuilder result = new StringBuilder(" " + string(monthName(month, locale)) + " ");
    StringBuilder days = new StringBuilder();
    LocalDate startOfMonth = LocalDate.of(year, month, 1);
    int lastDay = startOfMonth.lengthOfMonth();
    //the Java class: 1 is Monday, 7 is Sunday!
    //I want to rebase to 0 as Sunday, 6 as Saturday
    int startWeekday = startOfMonth.getDayOfWeek().getValue() % 7; 
    for(int i=0; i<(7*6); ++i) {
      if (i < startWeekday || i >= (startWeekday + lastDay)){
        days.append(string("") + " ");
      }
      else {
        days.append(string((i - startWeekday + 1)) + " "); 
     }
    }
    result.append(array(days));
    return dict(result.toString() + NL);
  }
  
  private String monthName(Month month, Locale locale) {
    String result = month.getDisplayName(TextStyle.FULL, locale);
    //make sure the initial letter is capitalized:
    String firstLetter = result.substring(0, 1);
    String remainingLetters = result.substring(1);
    return firstLetter.toUpperCase() + remainingLetters;
  }
  
  /**
   One-letter abbreviations for the days of the week.
     << 1 (S) 2 (M) 3 (T) 4 (W) 5 (T) 6 (F) 7 (S) >>
   */
  private String weekdayAbbreviations(Locale locale) {
    StringBuilder result = new StringBuilder();
    //warning: the week starts on Monday here!
    //this is an ISO standard, apparently
    for(DayOfWeek weekday : DayOfWeek.values()) {
      int myDayOfWeek = (weekday.getValue() + 1);
      if (myDayOfWeek == 8) { myDayOfWeek = 1; }
      result.append(" " + myDayOfWeek + " " + string(weekday.getDisplayName(TextStyle.NARROW_STANDALONE, locale)));
    }
    return dict(result);
  }
}
