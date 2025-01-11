package astropdf.pdf;

import static astropdf.util.LogUtil.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import astropdf.config.CoreConfig;

/**
  Convert a PostScript file to a PDF file.
  
  <P>Such conversion requires a tool installed on your computer, either Adobe Distiller or GhostScript (ghostscript.com).
  
  <P>THIS IMPLEMENTATION USES GhostScript, AND ASSUMES THE WINDOWS OPERATING SYSTEM.
*/
public final class PostScriptToPdf {

  /** 
   This assumes Ghostscript is installed on your host, and that your host has Windows OS.
   It may also need environment variables such as <tt>GS_FONTPATH</tt>.
    
   @see https://www.baeldung.com/run-shell-command-in-java 
   @see https://stackoverflow.com/questions/8496494/running-command-line-in-java 
  */
  public boolean convertPStoPDFUsingGhostScriptOnWindowsOS(CoreConfig config) throws IOException, InterruptedException {
    //create a command line expression and execute it, as if you were typing it in on a command line
    ProcessBuilder builder = new ProcessBuilder();
    String input = fullFileName(config, ".ps"); 
    String output = fullFileName(config, ".pdf");
    String command = "gswin64c.exe -sDEVICE=pdfwrite -sFONTPATH=C:\\Windows\\Fonts -o  " + output + " " + input;
    log("Command: " + command);
    //the option   -dCompatibilityLevel=1.4    can downgrade the output file format, if desired (currently it defaults to 1.7)
    builder.command("cmd.exe", "/c", command);
    builder.directory(new File(config.ghostscriptBinDir()));  //the home dir for the process
    Process process = builder.start();
    StreamGobbler streamGobbler =  new StreamGobbler(process.getInputStream(), System.out::println);
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    Future<?> future = executorService.submit(streamGobbler);
    int exitCode = process.waitFor();
    boolean result = false;
    if (exitCode == 0) {
      log("Success: gswin64c.exe completed normally.");
      result = true;
    }
    else {
      log("FAILURE: gswin64c.exe completed abnormally.");
    }
    return result;
  }
  
  private String fullFileName(CoreConfig config, String extension) {
    return config.outputDir() + File.separator + config.outputFileName() + extension;
  }
  
  /** The stream needs to be consumed, otherwise the process hangs. */
  private static class StreamGobbler implements Runnable {
    private InputStream inputStream;
    private Consumer<String> consumer;
    public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
      this.inputStream = inputStream;
      this.consumer = consumer;
    }
    @Override
    public void run() {
      new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
    }
  }
}
