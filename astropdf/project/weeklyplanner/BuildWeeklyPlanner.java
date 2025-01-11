package astropdf.project.weeklyplanner;

import static astropdf.util.LogUtil.log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import astropdf.config.Config;
import astropdf.config.ConfigFromFile;
import astropdf.pdf.PlaceholderData;
import astropdf.pdf.PostScriptTemplate;
import astropdf.pdf.PostScriptToPdf;

/** Create a PDF for a weekly planner, for a given location and year. */
public final class BuildWeeklyPlanner {
  
  public static void main(String[] args)  throws IOException, InterruptedException {
    BuildWeeklyPlanner planner = new BuildWeeklyPlanner();
    planner.buildBody();
    //planner.buildCover();
  }

  /** Build the interior, without the cover. */
  void buildBody() throws IOException, InterruptedException {
    log("Building the body of the weekly planner.");
    Config config = new ConfigFromFile().init(this);
    log("The PDF file will be output to this directory: " + config.outputDir());
    log("Full config:");
    log(config.toString());
    
    createAndSaveTheBody(config);
    convertPStoPDF(config);
  }

  /** 
   Build the cover, without the interior. 
   The cover might be of slightly larger dimensions than the body. 
  */
  void buildCover() throws IOException, InterruptedException {
    log("Building the cover for the weekly planner. ");
    Config config = new ConfigFromFile().init(this);
    log("The PDF file will be output to this directory: " + config.outputDir());
    log("Full config:");
    log(config.toString());
    createAndSaveTheCover(config);
    convertPStoPDF(config);
  }
  
  private void createAndSaveTheBody(Config config) throws IOException {
    log("Generating the PostScript file. Populating PostScript templates with data.");
    PostScriptTemplate populateThe = new PostScriptTemplate(config);
    List<String> pages = new ArrayList<>();
    
    //first populate the body of a page
    //assemble the data to be injected into the page
    PlaceholderData with = new PlaceholderData();
    
    //title page
    with.stringLiteral("TITLE", config.title());
    with.stringLiteral("LOCATION", config.location());
    with.stringLiteral("YEAR", config.year().toString());
    addPage("title.ps", with, pages, populateThe);
    
    //two pages for every week
    with = new PlaceholderData();
    addPage("weekly-left-hand.ps", with, pages, populateThe);
    
    //unusual: a 'notes' page has no placeholder data; they are hard-coded
    addPage("notes.ps", pages, populateThe);
    addPage("notes.ps", pages, populateThe);
    
    populateThe.documentAndSave(config.outputFileName(), pages, this);
  }
  
  private void createAndSaveTheCover(Config config) throws IOException {
    log("Generating the PostScript file. Populating PostScript templates with data.");
    PostScriptTemplate populateThe = new PostScriptTemplate(config);
    List<String> pages = new ArrayList<>();
    PlaceholderData with = new PlaceholderData();

    //the front 
    with.stringLiteral("TITLE", config.title());
    with.stringLiteral("LOCATION", config.location());
    with.stringLiteral("YEAR", config.year().toString());
    addPage("title.ps", with, pages, populateThe);
    
    //interior of the covers: two yearly calendars, year and year + 1, 
    YearlyCalendar yearlyCal = new YearlyCalendar();
    with = yearlyCal.dataFor(config.year(), config.locale());
    addPage("yearly-calendar.ps", with, pages, populateThe);
    
    with = yearlyCal.dataFor(config.year() + 1, config.locale());
    addPage("yearly-calendar.ps", with, pages, populateThe);
    
    //the back
    addPage("back.ps", pages, populateThe);
    
    populateThe.documentAndSave(config.outputFileName(), pages, this);
  }
  
  private void convertPStoPDF(Config config) throws IOException, InterruptedException {
    log("Final Step: Converting the output PostScript file to PDF using GhostScript.");
    PostScriptToPdf convert = new PostScriptToPdf();
    boolean success = convert.convertPStoPDFUsingGhostScriptOnWindowsOS(config);
    if (success) {
      log("Done! Your PDF was created successfully.");
    }
    else {
      log("Something went wrong. The PDF was not created as expected.");
    }
  }

  /** In this project, none of the pages are rotated. */
  private static final boolean UNROTATED = false;
  
  /** An unrotated page. */
  private void addPage(String templateName, PlaceholderData with, List<String> pages, PostScriptTemplate populateThe) throws IOException {
    String body = populateThe.snippet(templateName, with.data(), this);
    pages.add(populateThe.page(body, pages.size() + 1 , UNROTATED, this));
  }
  
  /** An unrotated page having no data for placeholders. */
  private void addPage(String templateName, List<String> pages, PostScriptTemplate populateThe) throws IOException {
    PlaceholderData noData = new PlaceholderData();
    addPage(templateName, noData, pages, populateThe);
  }
}
