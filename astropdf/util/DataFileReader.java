package astropdf.util;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static astropdf.util.LogUtil.log;

/**
  Read a text file and return it as a list of strings.
  
  <P>This class allows the code to follow the 
  <a href='http://www.javapractices.com/topic/TopicAction.do?Id=205'>package-by-feature</a> design principle.
*/
public final class DataFileReader {
  
  /**
   Read a text file (encoded using UTF-8) in the same directory as the calling class 
   and return it as a list of (untrimmed) Strings.
   @param aClass the calling class
   @param fileName name of a text file that resides in the same directory as the calling class.
  */
  public List<String> readFileUTF8(Class<?> aClass, String fileName){
    return readFile(aClass, fileName, UTF8);
  }
  
  /**
   Read a text file (encoded using LATIN-1) in the same directory as the calling class 
   and return it as a list of (untrimmed) Strings.
   @param aClass the calling class
   @param fileName name of a text file that resides in the same directory as the calling class.
  */
  public List<String> readFileLATIN1(Class<?> aClass, String fileName){
    return readFile(aClass, fileName, LATIN1);
  }
  
  /**
   Read a text file encoded as UTF8 and return it as a list of (untrimmed) Strings.
   @param fileName the full name of a text file. 
  */
  public List<String> readFileUTF8(String fileName) {
    return readFile(fileName, UTF8);
  }

  /**
   Read a text file encoded as LATIN1 and return it as a list of (untrimmed) Strings.
   @param fileName the full name of a text file. 
  */
  public List<String> readFileLATIN1(String fileName) {
    return readFile(fileName, LATIN1);
  }
  
  /** Used when reading/writing files that DO NOT CONTAIN the PostScript programming language. */
  public final static Charset UTF8 = StandardCharsets.UTF_8;
  
  /** MUST BE USED WHEN READING/WRITING FILES CONTAINING the PostScript programming language. */
  public final static Charset LATIN1 = StandardCharsets.ISO_8859_1;
  
  /** Ignored lines start with this character - {@value}. */
  public final static String COMMENT = "#";
  
  /** A field separator used in this project - {@value}. */
  public final static String SEP = "|";

  /**
   Read a text file and return it as a list of (untrimmed) Strings.
   @param aClass the calling class
   @param fileName name of a text file that resides in the same directory as the calling class.
   @param charset the encoding of the text file. UTF-8 for most cases, but LATIN-1 for PostScript files. 
  */
  private List<String> readFile(Class<?> aClass, String fileName, Charset charset){
    List<String> result = new ArrayList<>();
    try (
      //uses the class loader search mechanism:
      InputStream input = aClass.getResourceAsStream(fileName);
      InputStreamReader isr = new InputStreamReader(input, charset);
      BufferedReader reader = new BufferedReader(isr);
    ){
      String line = null;
      while ((line = reader.readLine()) != null) {
        result.add(line);
      }      
    }
    catch(IOException ex){
      log("CANNOT OPEN FILE: " + fileName);
    }
    return result;
  }
  
  /**
   Read a text file and return it as a list of (untrimmed) Strings.
   @param fileName the full name of a text file. 
  */
  private List<String> readFile(String fileName, Charset charset) {
    List<String> result = new ArrayList<>();
    try {
      Path path = Paths.get(fileName);
      result = Files.readAllLines(path, charset);
    }
    catch(IOException ex) {
      log("CANNOT OPEN FILE: " + fileName);
    }
    return result;
  }  
}