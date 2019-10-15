package net.nixill.tables.csv;

import net.nixill.tables.TextTable;

@SuppressWarnings("serial")
public class CSVParseException extends RuntimeException {
  private TextTable tableSoFar = null;
  private String remainder = null;
  
  public CSVParseException() {
    super();
  }
  
  public CSVParseException(String message) {
    super(message);
  }
  
  public CSVParseException(TextTable tsf, String rem) {
    super();
    tableSoFar = tsf;
    remainder = rem;
  }
  
  public CSVParseException(String message, TextTable tsf, String rem) {
    super(message);
    tableSoFar = tsf;
    remainder = rem;
  }
  
  public TextTable getTableSoFar() {
    return tableSoFar;
  }
  
  public String getRemainingString() {
    return remainder;
  }
}
