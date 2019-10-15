package net.nixill.tables;

import java.util.List;

public interface ITable<T> {
  public int height();
  public int width();
  public int size();
  public int area();
  
  public default boolean cellExists(CellReference ref) {
    return cellExists(ref.getRow(), ref.getColumn());
  }
  public boolean cellExists(int row, int col);
  
  public default T get(CellReference ref) {
    return get(ref.getRow(), ref.getColumn());
  }
  public T get(int row, int col);
  public default List<T> getColumn(CellReference ref) {
    return getColumn(ref.getColumn());
  }
  public List<T> getColumn(int col);
  public default List<T> getRow(CellReference ref) {
    return getRow(ref.getRow());
  }
  public List<T> getRow(int col);
  
  public default T set(CellReference ref, T value) {
    return set(ref.getRow(), ref.getColumn(), value);
  }
  public T set(int row, int col, T value);
  
  public void addRow();
  public void addRow(List<T> values);
  public void addRow(int row, List<T> values);
  public default void addRow(CellReference ref, List<T> values) {
    addRow(ref.getRow(), values);
  }
  public void addColumn();
  public void addColumn(List<T> values);
  public void addColumn(int col, List<T> values);
  public default void addColumn(CellReference ref, List<T> values) {
    addColumn(ref.getColumn(), values);
  }
}
