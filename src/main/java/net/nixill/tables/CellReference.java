package net.nixill.tables;

import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Nixill
 *
 */
public class CellReference implements Comparable<CellReference> {
  private final int row;
  private final int column;
  private final boolean isRefError;

  /**
   * The "error" cell reference, <code>#REF!</code>.
   * <p>
   * This can be used to substitute for non-existent cells, for example when
   * searching for something that doesn't exist. It has row and column values of
   * -1, always compares to less than a valid reference (and equal to itself), and
   * returns "<code>#REF!</code>" in any string form.
   */
  public static final CellReference REF_ERROR = new CellReference(-1, -1);
  
  /**
   * A comparator that orders CellReferences by column first.
   */
  public static final Comparator<CellReference> TRANSPOSED_COMPARE = (CellReference left, CellReference right) -> {
    Objects.requireNonNull(left);
    Objects.requireNonNull(right);

    if (left.isRefError && right.isRefError) {
      return 0;
    } else if (left.isRefError && !right.isRefError) {
      return Integer.MIN_VALUE;
    } else if (!left.isRefError && right.isRefError) {
      return Integer.MAX_VALUE;
    }

    if (left.column != right.column) {
      return left.column - right.column;
    } else if (left.row != right.row) {
      return left.row - right.row;
    } else {
      return 0;
    }
  };

  private CellReference(int r, int c) {
    row = r;
    column = c;
    if (r == -1 && c == -1) {
      isRefError = true;
    } else {
      isRefError = false;
    }
  }

  /**
   * Returns the cell reference as defined by the given row and column.
   * <p>
   * Using this method, the top left cell of a {@link TextTable} is row 0, column 0.
   * 
   * @param row The row being referenced, >= 0.
   * @param col The column being referenced, >= 0.
   * @return The CellReference as defined by the given row and column.
   * @throws IllegalArgumentException If either row or column is negative.
   */
  public static CellReference of(int row, int col) {
    if (row < 0 || col < 0) {
      throw new IllegalArgumentException(
          "Relative or negative cell references are not allowed: Row " + row + ", column " + col);
    }

    return new CellReference(row, col);
  }

  /**
   * Returns the cell reference defined as the given A1 notation string, as may be
   * found in popular spreadsheet software.
   * <p>
   * In this interpreter's definition of A1 notation, column letters start at A,
   * then continue to Z through all single letters. After Z, the next column to
   * the right is AA, then AB, all the way to AZ, then BA to BZ, and so on until
   * ZZ before entering triple letters, and so on.
   * <p>
   * Using this notation, the top left cell of a {@link TextTable} is A1.
   * 
   * @param a1n The A1-notation cell reference.
   * @return The CellReference as defined by the given A1 notation string.
   * @throws IllegalArgumentException If the string given isn't valid A1 notation,
   *                                  or the row number is zero or negative.
   */
  public static CellReference ofA1Notation(String a1n) {
    // Matches any string of letters, followed by an integer >= 1, either of which
    // may or may not be preceded by a $ symbol.
    Matcher m = Pattern.compile("^\\$?([A-Za-z]+)\\$?([1-9]\\d*)$", Pattern.CASE_INSENSITIVE).matcher(a1n);

    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid A1 notation: " + a1n);
    }

    int c = columnFromA1(m.group(1));
    int r = Integer.parseInt(m.group(2));

    return new CellReference(r, c);
  }

  /**
   * Returns the cell reference as defined by the given R1C1 notation string, as
   * may be found in popular spreadsheet software.
   * <p>
   * Using this notation, the top left cell of a {@link TextTable} is R1C1.
   * 
   * @param r1c1n The R1C1-notation cell reference.
   * @return The CellReference as defined by the given R1C1 notation string.
   * @throws IllegalArgumentException If the string given isn't valid R1C1
   *                                  notation, it's a relative reference, or a
   *                                  number is zero or negative.
   */
  public static CellReference ofR1C1Notation(String r1c1n) {
    // The "strict" matcher must be matched for the function to return a value.
    // However, the "lenient" matcher exists to give a more specific error if it's
    // matched.

    // The strict matcher matches an R, an integer >= 1, a C, and another integer >=
    // 1. The lenient matcher matches an R, an integer that may or may not be
    // encased
    // in [], a C, and another integer that may or may not be encased in [].
    Matcher strict = Pattern.compile("^R([1-9]\\d*)C([1-9]\\d*)$", Pattern.CASE_INSENSITIVE).matcher(r1c1n);
    Matcher lenient = Pattern.compile("^R(-?\\d+|\\[-?\\d+\\])?C(-?\\d+|\\[-?\\d+\\])?$", Pattern.CASE_INSENSITIVE)
        .matcher(r1c1n);

    if (!strict.matches()) {
      if (lenient.matches()) {
        throw new IllegalArgumentException("Relative or negative cell references are not allowed: " + r1c1n);
      } else {
        throw new IllegalArgumentException("Invalid R1C1 notation: " + r1c1n);
      }
    }

    int r = Integer.parseInt(strict.group(1));
    int c = Integer.parseInt(strict.group(2));

    return new CellReference(r, c);
  }

  /**
   * Returns the row represented by this CellReference.
   * <p>
   * The top row of a table is row 0.
   * <p>
   * For the CellReference representing the <code>#REF!</code> error, -1 is
   * returned.
   * 
   * @return The row in question.
   */
  public int getRow() {
    return row;
  }

  /**
   * Returns the column represented by this CellReference.
   * <p>
   * The leftmost column of a table is column 0.
   * <p>
   * For the CellReference representing the <code>#REF!</code> error, -1 is
   * returned.
   * 
   * @return The column in question.
   */
  public int getColumn() {
    return column;
  }

  /**
   * Returns true if this CellReference represents <code>#REF!</code>.
   * 
   * @return true if this CellReference represents <code>#REF!</code>.
   */
  public boolean isRefError() {
    return isRefError;
  }

  /**
   * Returns the CellReference equal to swapping the row and column of this one.
   * 
   * @return The CellReference equal to swapping the row and column of this one.
   */
  public CellReference transpose() {
    return new CellReference(column, row);
  }

  /**
   * Returns a {@link String} representation of this CellReference.
   * <p>
   * Returns "<code>#REF!</code>" for the CellReference that represents such. For
   * all other CellReferences, it returns a string of the format "&lt;row
   * number&gt;, &lt;column number&gt;".
   * 
   * @return A {@link String} representation of this CellReference.
   */
  public String toString() {
    if (isRefError) {
      return "#REF!";
    } else {
      return row + ", " + column;
    }
  }

  /**
   * Returns an int that is negative, zero, or positive if this CellReference is
   * respectively less than, equal to, or greater than <code>ref2</code>.
   * <p>
   * To be more specific, this function returns the difference in their rows if
   * they're unequal, otherwise the difference in their columns (and zero if both
   * are equal).
   * 
   * @param ref2 The CellReference to compare against.
   * @return The results of the comparison - a negative integer if ref2 is less; 0
   *         if ref2 is equal; a positive integer if ref2 is greater.
   * @see {@link #compareTransposed(CellReference)} to compare columns first.
   */
  public final int compareTo(CellReference ref2) {
    Objects.requireNonNull(ref2);

    if (isRefError && ref2.isRefError) {
      return 0;
    } else if (isRefError && !ref2.isRefError) {
      return Integer.MIN_VALUE;
    } else if (!isRefError && ref2.isRefError) {
      return Integer.MAX_VALUE;
    }

    if (row != ref2.row) {
      return row - ref2.row;
    } else if (column != ref2.column) {
      return column - ref2.column;
    } else {
      return 0;
    }
  }

  /**
   * Returns an int that is negative, zero, or positive if this CellReference is
   * respectively less than, equal to, or greater than <code>ref2</code>.
   * <p>
   * To be more specific, this function returns the difference in their columns if
   * they're unequal, otherwise the difference in their rows (and zero if both are
   * equal).
   * 
   * @param ref2 The CellReference to compare against.
   * @return The results of the comparison - a negative integer if ref2 is less; 0
   *         if ref2 is equal; a positive integer if ref2 is greater.
   * @see {@link #compareTo(CellReference)} to compare rows first.
   */
  public final int compareTransposed(CellReference ref2) {
    Objects.requireNonNull(ref2);

    if (isRefError && ref2.isRefError) {
      return 0;
    } else if (isRefError && !ref2.isRefError) {
      return Integer.MIN_VALUE;
    } else if (!isRefError && ref2.isRefError) {
      return Integer.MAX_VALUE;
    }

    if (column != ref2.column) {
      return column - ref2.column;
    } else if (row != ref2.row) {
      return row - ref2.row;
    } else {
      return 0;
    }
  }

  /**
   * Returns true if the two CellReferences are equal.
   * <p>
   * More specifically, two CellReferences are equal iff their rows are equal, and
   * their columns are equal.
   * 
   * @return true if the two CellReferences are equal.
   */
  public final boolean equals(Object ref2) {
    if (ref2 == null) {
      return false;
    }

    if (!(ref2 instanceof CellReference)) {
      return false;
    }

    CellReference ref = (CellReference) ref2;

    if (ref.isRefError && isRefError) {
      return true;
    } else if (ref.isRefError != isRefError) {
      return false;
    }

    if (ref.row == row && ref.column == column) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns a hash code for the object, determined by its row and column number.
   * <p>
   * <code>#REF!</code> returns hashcode <code>0xFFFFFFFF</code>.
   */
  public final int hashCode() {
    if (isRefError) {
      return 0xffffffff;
    }

    int rowCode = row & 0xffff;
    int colCode = column & 0xffff;
    rowCode = rowCode << 16;
    return rowCode | colCode;
  }

  /**
   * Returns the CellReference's value in R1C1 notation, as may be found in
   * popular spreadsheet software.
   * <p>
   * Using this notation, the top left cell of a {@link TextTable} is R1C1.
   * <p>
   * Returns "<code>#REF!</code>" for the CellReference that represents such.
   * 
   * @return The R1C1 notation of this CellReference.
   */
  public String toR1C1Notation() {
    if (isRefError) {
      return "#REF!";
    } else {
      return "R" + (row + 1) + "C" + (column + 1);
    }
  }

  /**
   * Returns the CellReference's value in A1 notation, as may be found in popular
   * spreadsheet software.
   * <p>
   * In this interpreter's definition of A1 notation, column letters start at A,
   * then continue to Z through all single letters. After Z, the next column to
   * the right is AA, then AB, all the way to AZ, then BA to BZ, and so on until
   * ZZ before entering triple letters, and so on.
   * <p>
   * Using this notation, the top left cell of a {@link TextTable} is A1.
   * <p>
   * Returns "<code>#REF!</code>" for the CellReference that represents such.
   * 
   * @return The A1 notation of this CellReference.
   */
  public String toA1Notation() {
    if (isRefError) {
      return "#REF!";
    } else {
      return columnToA1(column) + (row + 1);
    }
  }

  /**
   * Takes a column letter(s) in A1 notation, and returns its zero-based index.
   * 
   * @param col The column letter(s)
   * @return The zero-based index, or -1 if the empty string was provided
   */
  private static int columnFromA1(String col) {
    String base26 = "";
    String base26add = "";

    if (col.isEmpty()) {
      base26 = "0";
      base26add = "0";
    } else {
      // Convert the string, character-by-character, to a base-26 number.
      // The "add" variable accounts for all columns that have fewer letters.
      for (int i = 0; i < col.length(); i++) {
        char chr = col.charAt(0);
        chr = charToBase26(chr);
        base26 += chr;

        base26add = "1" + base26add;
      }
    }

    return Integer.parseInt(base26, 26) + Integer.parseInt(base26add, 26) - 1;
  }

  /**
   * Takes a single letter, and converts it to a base-26 character.
   * 
   * @param input A single letter
   * @return That character as a base 26 digit.
   */
  private static char charToBase26(char input) {
    if (input >= 'a' && input <= 'z') {
      input -= 32; // Conversion to lowercase
    }

    input -= 10; // K -> A ... Z -> P
    // ABCDEFGHIJ -> 789:;<=>?@
    if (input >= 'A' && input <= 'J') {
      input -= 7; // A -> (7) -> 0 ... J -> (@) -> 9
    }

    return input;
  }

  /**
   * Takes a column number, converting it to the letter(s) in A1 notation.
   * 
   * @param col The column number
   * @return The A1-notation letter(s)
   */
  private static String columnToA1(int col) {
    int digits = 1;
    for (; col >= Math.pow(26, digits); digits++) {
      col -= Math.pow(26, digits);
    }

    String text = Integer.toString(col, 26);

    while (text.length() < digits) {
      text = "0" + text;
    }

    String textOut = "";

    for (int i = 0; i < text.length(); i++) {
      textOut += charFromBase26(text.charAt(i));
    }

    return textOut;
  }

  /**
   * Takes a single base-26 digit, and converts it to a letter.
   * 
   * @param input A single letter
   * @return That character as a base 26 digit.
   */
  private static char charFromBase26(char input) {
    if (input >= '0' && input <= '9') {
      input += 7;
    }

    input += 10;

    return input;
  }
}
