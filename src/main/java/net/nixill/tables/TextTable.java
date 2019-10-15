package net.nixill.tables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TextTable implements ITable<String> {
  private ArrayList<ArrayList<String>> items = new ArrayList<>();
  int width = 0;
  
  /**
   * Creates a new, blank Table.
   */
  public TextTable() { }
  
  /**
   * Creates a new, empty table of the specified size.
   * 
   * @param initHeight The initial height to give the table; not negative.
   * @param initWidth The initial width to give the table; not negative.
   * @throws IllegalArgumentException If either table dimension is negative.
   */
  public TextTable(int initHeight, int initWidth) {
    if (initHeight < 0 || initWidth < 0) {
      throw new IllegalArgumentException("Sizes of tables must be positive.");
    }
    
    for (int r = 0; r < initHeight; r++) {
      ArrayList<String> row = new ArrayList<>();
      for (int w = 0; w < initWidth; w++) {
        row.add("");
      }
      items.add(row);
    }
    
    width = initWidth;
  }
  
  /**
   * Creates a new table from an existing {@link Collection} of Collections of Objects.
   * <p>
   * Objects are converted to {@link String}s using their {@link Object#toString()} methods.
   * 
   * @param sourceItems The existing Collection.
   */
  public TextTable(Collection<? extends Collection<?>> sourceItems) {
    for (Collection<?> sourceRow : sourceItems) {
      ArrayList<String> row = new ArrayList<>();
      for (Object obj : sourceRow) {
        if (obj == null) {
          row.add("");
        } else {
          row.add(obj.toString());
        }
      }
      width = Math.max(width, row.size());
      items.add(row);
    }
    
    // pad all rows to the same width
    for (ArrayList<String> row : items) {
      for (int i = row.size(); i < width; i++) {
        row.add("");
      }
    }
  }
  
  /**
   * Creates a new TextTable from an existing {@link ITable}.
   * <p>
   * Non-string objects are converted to strings.
   * 
   * @param sourceItems The existing ITable.
   */
  public TextTable(ITable<?> tab) {
    // Make a new table of the required size.
    this(tab.height(), tab.width());
    
    for (int r = 0; r < items.size(); r++) {
      ArrayList<String> row = items.get(r);
      for (int c = 0; c < width; c++) {
        if (tab.cellExists(r, c)) {
          row.set(c, tab.get(r, c).toString());
        }
      }
    }
  }
  
  /**
   * Returns the width - the number of columns - of the table.
   * @return The width of the table.
   */
  public int width() {
    return width;
  }
  
  /**
   * Returns the height - the number of rows - of the table.
   * @return The height of the table.
   */
  public int height() {
    return items.size();
  }
  
  /**
   * Returns the size - total number of cells - of the table.
   * <p>
   * Since a TextTable is guaranteed to be rectangular without nulls, this is equal to {@link #area()}.
   * 
   * @return The area of the table.
   */
  public int size() {
    return width * items.size();
  }
  
  /**
   * Returns the area - columns times rows - of the table.
   * @return The area of the table.
   */
  public int area() {
    return width * items.size();
  }
  
  /**
   * Returns whether the specified cell exists.
   * @param row The row of the cell to check.
   * @param col The column of the cell to check.
   * @return true if the specified cell exists.
   */
  public boolean cellExists(int row, int col) {
    // If row is out of bounds
    if (row < 0 || row > items.size()) {
      return false;
    }
    
    // If col is out of bounds
    if (col < 0 || col > width) {
      return false;
    }
    
    // If both are in bounds, return true
    // TextTable is rectangular, so any value within the bounds exists
    return true;
  }
  
  /**
   * Returns the specified cell of the table.
   * @param row The row of the cell to get.
   * @param col The column of the cell to get.
   * @return The specified cell of the table.
   */
  public String get(int row, int col) {
    if (row < 0 || row >= items.size()) {
      throw new IndexOutOfBoundsException("Expected 0 to " + (items.size() - 1) + ", got " + row);
    }
    
    if (col < 0 || col >= width) {
      throw new IndexOutOfBoundsException("Expected 0 to " + (width - 1) + ", got " + col);
    }
    
    return items.get(row).get(col);
  }
  
  /**
   * Returns the specified row of the table.
   * @param row The row to get.
   * @return The specified row of the table.
   */
  public ArrayList<String> getRow(int row) {
    if (row < 0 || row >= items.size()) {
      throw new IndexOutOfBoundsException("Expected 0 to " + (items.size() - 1) + ", got " + row);
    }
    
    return new ArrayList<>(items.get(row));
  }
  
  /**
   * Returns the specified column of the table.
   * @param col The column to get.
   * @return The specified column of the table.
   */
  public ArrayList<String> getColumn(int col) {
    if (col < 0 || col >= width) {
      throw new IndexOutOfBoundsException("Expected 0 to " + (width - 1) + ", got " + col);
    }
    
    ArrayList<String> out = new ArrayList<>();
    
    for (int r = 0; r < items.size(); r++) {
      out.add(items.get(r).get(col));
    }
    
    return out;
  }
  
  /**
   * Sets the specified cell to the specified value.
   * @param row The row of the cell to set.
   * @param col The column of the cell to set.
   * @param value The value to set in the cell.
   * @return The previous value in the cell.
   * @throws NullPointerException If <code>value</code> is empty.
   */
  public String set(int row, int col, String value) {
    if (value == null) {
      throw new NullPointerException("TextTables cannot have null values.");
    }
    
    if (row < 0 || row >= items.size()) {
      throw new IndexOutOfBoundsException("Expected 0 to " + (items.size() - 1) + ", got " + row);
    }
    
    if (col < 0 || col >= width) {
      throw new IndexOutOfBoundsException("Expected 0 to " + (width - 1) + ", got " + col);
    }
    
    return items.get(row).set(col, value);
  }

  /**
   * Adds a blank row to a table.
   */
  public void addRow() {
    addRow(items.size(), new ArrayList<>());
  }
  
  /**
   * Adds a new row to the table with predefined values.
   * @param values The values to add.
   */
  public void addRow(List<String> values) {
    addRow(items.size(), values);
  }

  /**
   * Adds a new row to the table above an existing row with predefined values.
   * @param row The location at which to add the row.
   * @param values The values to add.
   */
  public void addRow(int row, List<String> values) {
    // The row argument must be in a valid position to insert a new row.
    // It can equal items.size(), which inserts the new row at the end.
    if (row < 0 || row > items.size()) {
      throw new IndexOutOfBoundsException("Expected 0 to " + items.size() + ", got " + row);
    }
    
    // If the table is 0x0, make it be 0x(width of new row).
    if (width == 0 && items.size() == 0) {
      resize(0, values.size());
    }
    
    // There must not be more values to add than the width of the table.
    // If the table has no columns, an empty row can still be added.
    if (values.size() > width) {
      throw new IllegalArgumentException("Row width exceeds table width.");
    }
    
    ArrayList<String> list = new ArrayList<>(values);
    
    // There must not be fewer values to add than the width of the table.
    // However, that's not an error; the row can simply be padded with blank values.
    for (int c = values.size(); c < width; c++) { // no, java
      list.add("");
    }
    
    // And finally, add the items to the list.
    items.add(row, list);
  }
  
  public void addRow(String... values) {
    addRow(items.size(), Arrays.asList(values));
  }
  
  public void addRow(int row, String... values) {
    addRow(row, Arrays.asList(values));
  }
  
  public void addColumn() {
    addColumn(width, new ArrayList<>());
  }
  
  public void addColumn(List<String> values) {
    addColumn(width, values);
  }

  public void addColumn(int col, List<String> values) {
    // The col argument must be in a valid position to insert a new column.
    // It can equal width, which inserts the new column at the end.
    if (col < 0 || col > width) {
      throw new IndexOutOfBoundsException("Expected 0 to " + width + ", got " + col);
    }
    
    // If the table is 0x0, make it be (height of new column)x0.
    if (width == 0 && items.size() == 0) {
      resize(values.size(), 0);
    }
    
    // There must not be more values to add than the height of the table.
    // If the table has no rows, an empty column can still be added.
    if (values.size() > items.size()) {
      throw new IllegalArgumentException("Column height exceeds table height.");
    }
    
    // Since we have to add the values one by one anyway,
    // there's no point in making a new ArrayList of them right now.
    
    // Put all the values in the right spot in each row.
    for (int r = 0; r < values.size(); r++) {
      items.get(r).add(col, values.get(r));
    }
    
    // Add blank ones if the values ran out.
    for (int r = values.size(); r < items.size(); r++) {
      items.get(r).add(col, "");
    }
    
    // Update the width.
    width += 1;
  }
  
  public void addColumn(String... values) {
    addColumn(width, Arrays.asList(values));
  }
  
  public void addColumn(int col, String... values) {
    addColumn(col, Arrays.asList(values));
  }
  
  /**
   * Resizes the TextTable to be at least this large. Does nothing to an axis that is already larger.
   * 
   * @param h The height to ensure.
   * @param w The width to ensure.
   */
  public void resize(int h, int w) {
    // Resize every existing row to the new column count, if necessary
    if (width < w) {
      for (int r = 0; r < items.size(); r++) {
        ArrayList<String> row = items.get(r);
        for (int c = width; c < w; c++) {
          row.add("");
        }
      }
      width = w;
    }
    
    // Add new rows, if necessary
    if (items.size() < h) {
      for (int r = items.size(); r < h; r++) {
        ArrayList<String> row = new ArrayList<>(width);
        for (int c = 0; c < width; c++) {
          row.add("");
        }
        items.add(row);
      }
    }
  }
}
