package astropdf.project.sanitytest;

import static astropdf.util.LogUtil.log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import astropdf.config.Config;
import astropdf.config.ConfigFromFile;
import astropdf.pdf.PlaceholderData;
import astropdf.pdf.PostScriptTemplate;
import astropdf.pdf.PostScriptToPdf;

/** 
 Build a PostScript file as a simple test, as a standalone program from the command line.
 The PostScript file is converted to a PDF file for the final output.
 
 <P>The PDF is hard-coded to show the "working area" of the page.
 This lets you specify coordinates in the working area as simple percentages of its width and height.
*/
public final class BuildTest {

 /** 
  As a standalone program, generate a simple PDF file as a test.
  The file is saved to the file system, in an existing directory (as specified in config.ini). 
 */
 public static void main(String... args)  throws IOException, InterruptedException {
   BuildTest testPDF = new BuildTest();
   testPDF.build();
 }
 
 void build()  throws IOException, InterruptedException {
   log("Building a test PDF from a config file.");
   Config config = new ConfigFromFile().init(this);
   log("The PDF file will be output to this directory: " + config.outputDir());
   log("Full config:");
   log(config.toString());

   log("Generating the PostScript file. Populating postscript template with data.");
   PostScriptTemplate populateThe = new PostScriptTemplate(config);
   
   List<String> pages = new ArrayList<>();
   
   //first populate the body of a page
   //assemble the data to be injected into the page
   PlaceholderData with = new PlaceholderData();
   with.stringLiteral("LOCATION", config.location());
   String body = populateThe.snippet("page-body-template.ps", with.data(), this);
   //then add the body plus 'bookkeeping' data to the page in order to complete it (page-num, is-rotated)
   pages.add(populateThe.page(body, pages.size() + 1 , false, this));
   
   //..add more pages here...in order of their appearance in the document
   //the page-num is taken from the size of the pages list; the true-false for is-rotated varies
   
   //when all pages have been added to the list, make the final output PostScript file
   //at this point we know the full page count as well, so that can be set here at the top of the document
   populateThe.documentAndSave(config.outputFileName(), pages, this);
   
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
}
