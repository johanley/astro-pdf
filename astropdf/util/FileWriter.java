package astropdf.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/** Output a text file to the file system, using the indicated encoding. */
public final class FileWriter {
  
  public void outputUTF8(String content, String directory, String filename) throws IOException {
    output(content, directory,  filename, DataFileReader.UTF8);
  }
  
  public void outputLATIN_1(String content, String directory, String filename) throws IOException {
    output(content, directory,  filename, DataFileReader.LATIN1);
  }

  /** 
   Write using the given encoding. Creates the directory if needed.
   For PostScript files the charset must be Latin-1. 
  */
  private void output(String content, String directory, String filename, Charset charset) throws IOException {
    File dir = new File(directory);
    if (!dir.exists()) {
      dir.mkdir();
    }
    File outputFile = new File(directory, filename);
    Writer out = new OutputStreamWriter(new FileOutputStream(outputFile), charset);
    try {
      out.write(content);
    }
    finally {
      out.close();
    }
  }
  
}
