package net.nixill.tables.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.nixill.tables.ITable;
import net.nixill.tables.TextTable;

/**
 * A utility class for converting CSV files to {@link TextTable}s and back.
 * <p>
 * Input is read according to the specification in <a href="https://tools.ietf.org/html/rfc4180">RFC 4180</a>, with two important exceptions:
 * <ol>
 * <li>When reading and writing, tables are not expected to have the same number of columns in each row. Tables read by the parser will have rows padded by empty strings to be of equal width.</li>
 * <li>When reading, CRLF, CR, and LF styles are all acceptable as line breaks. When writing, LF alone is used.</li>
 * </ol>
 * 
 * @author nixill
 *
 */
public class CSVParser {
  /**
   * Reads a CSV file and returns its contents as a {@link TextTable}.
   * 
   * @param path The path of the file to read.
   * @return The file's contents as a TextTable.
   * @throws IOException If an I/O error occurs reading from the stream
   * @throws CSVParseException If the file cannot be successfully parsed
   */
  public static TextTable fromFile(String path) throws IOException {
    return fromString(new String(Files.readAllBytes(Paths.get(path))));
  }
  
  /**
   * Reads a CSV file and returns its contents as a {@link TextTable}.
   * 
   * @param file The file to read.
   * @return The file's contents as a TextTable.
   * @throws IOException If an I/O error occurs reading from the stream
   * @throws CSVParseException If the file cannot be successfully parsed
   */
  public static TextTable fromFile(File file) throws IOException {
    return fromFile(file.getAbsolutePath());
  }
  
  /**
   * Attempts to parse CSV from a {@link String} into a {@link TextTable}.
   * 
   * @param content The content to parse.
   * @return The content in TextTable form.
   * @throws CSVParseException If the string cannot be successfully parsed.
   */
  public static TextTable fromString(String content) {
    // Regex pattern to match a single value and its separator from the next value
    // Group 1: The value if it's enclosed in quotes (without the enclosing quotes)
    // Group 2: The value if it's not enclosed in quotes
    // Group 3: The following separator (possibly empty).
    // If there's a syntax error in any value, the matcher will stop being able to match.
    Pattern ptrn = Pattern.compile("(?:\\A|\\G)(?:\\\"((?:[^\\\"]|\"\")+?)\\\"|([^\\\",\\r\\n]*?))(\\r\\n|\\r|\\n|,|\\z)");
    Matcher match = ptrn.matcher(content);
    
    String term = null;
    
    ArrayList<ArrayList<String>> items = new ArrayList<>();
    ArrayList<String> row = new ArrayList<>();
    items.add(row);
    
    int rowSize = 0;
    int at = 0;
    
    while (match.find()) {
      String item = null;
      if (match.group(1) == null) {
        item = match.group(2);
      } else {
        item = match.group(1).replaceAll("\\\"\\\"", "\"");
      }
      
      term = match.group(3);
      
      row.add(item);
      
      rowSize = Math.max(rowSize, row.size());
      
      if (term == null || term.isEmpty()) {
        break;
      } else if (!term.equals(",")) {
        row = new ArrayList<>();
        items.add(row);
      }
      
      at = match.end();
    }
    
    // row will still be set to the most recent row, making this a little easier.
    // If the file ends on a blank line, that line shouldn't be included in the table.
    if (row.size() == 0 || (row.size() == 1 && row.get(0).isEmpty())) {
      items.remove(items.size() - 1);
    }
    
    TextTable table = new TextTable(items);
    
    if (!match.hitEnd()) {
      String rem = content.substring(at);
      throw new CSVParseException(table, rem);
    }
    
    return table;
  }
  
  /**
   * Converts an {@link ITable} to a CSV {@link String}, replacing null values with an empty string.
   * <p>
   * For null values, or cells that do not exist, an empty string is inserted.
   * 
   * @param table The table to convert to a CSV string.
   * @return The CSV string.
   */
  public static String toString(ITable<?> table) {
    return toString(table, "");
  }
  
  /**
   * Converts an {@link ITable} to a CSV {@link String}.
   * <p>
   * For null values, the string <code>def</code> is inserted. For cells that do not exist, the empty string is inserted.
   * 
   * @param table The table to convert to a CSV string.
   * @param def The value to use in place of nulls.
   * @return The CSV string.
   */
  public static String toString(ITable<?> table, String def) {
    String out = "";
    
    // For every row in the table...
    for (int r = 0; r < table.height(); r++) {
      int lastCol = 0;
      
      // Start the row with a new line, unless it's the first row.
      if (r != 0) {
        out += "\n";
      }
      
      // For every column in the table...
      for (int c = 0; c < table.width(); c++) {
        // If the cell actually exists...
        if (table.cellExists(r, c)) {
          // Add commas up to the new cell
          for (; lastCol < c; lastCol++) {
            out += ",";
          }
          
          // Add the cell's contents
          Object obj = table.get(r, c);
          if (obj != null) {
            String cell = obj.toString();
            // If the cell contains quotes, commas, or newlines, 
            if (cell.contains("\"") || cell.contains(",") || cell.contains("\n") || cell.contains("\r")) {
              cell = "\"" + cell.replaceAll("\"", "\"\"") + "\"";
            }
            out += cell;
          }
        }
      }
    }
    
    return out;
  }
  
  /**
   * Writes an {@link ITable} to a {@link File}, replacing null values with an empty string.
   * <p>
   * For null values, or cells that do not exist, the empty string is used.
   * 
   * @param table The table to convert to a CSV string.
   * @param fileName The name of the file to write.
   * @return The File being written to.
   * @throws IOException If an I/O error occurs
   */
  public static File toFile(ITable<?> table, String fileName) throws IOException {
    return toFile(table, "", fileName);
  }
  
  /**
   * Writes an {@link ITable} to a {@link File}, replacing null values with an empty string.
   * <p>
   * For null values, the string <code>def</code> is used. For cells that do not exist, the empty string is used.
   * 
   * @param table The table to convert to a CSV string.
   * @param def The default value to use for null objects.
   * @param fileName The name of the file to write.
   * @return The File being written to.
   * @throws IOException If an I/O error occurs
   */
  public static File toFile(ITable<?> table, String def, String fileName) throws IOException {
    File outFile = new File(fileName);
    
    BufferedWriter buff = new BufferedWriter(new FileWriter(outFile));
    
    // For every row in the table...
    for (int r = 0; r < table.height(); r++) {
      int lastCol = 0;
      
      // Start the row with a new line, unless it's the first row.
      if (r != 0) {
        buff.write("\n");
      }
      
      // For every column in the table...
      for (int c = 0; c < table.width(); c++) {
        // If the cell actually exists...
        if (table.cellExists(r, c)) {
          // Add commas up to the new cell
          for (; lastCol < c; lastCol++) {
            buff.write(",");
          }
          
          // Add the cell's contents
          Object obj = table.get(r, c);
          if (obj != null) {
            String cell = obj.toString();
            // If the cell contains quotes, commas, or newlines, 
            if (cell.contains("\"") || cell.contains(",") || cell.contains("\n") || cell.contains("\r")) {
              cell = "\"" + cell.replaceAll("\"", "\"\"") + "\"";
            }
            buff.write(cell);
          }
        }
      }
    }
    
    buff.flush();
    buff.close();
    
    return outFile;
  }
}
