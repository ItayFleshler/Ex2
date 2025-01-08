/**
 * Represents a cell's coordinates and provides conversion between
 * spreadsheet notation (e.g., "A0") and array indices.
 * @author ItayFleshler
 * @version 1.0
 * @since 2025-01-08
 */
public class CellEntry implements Index2D {
    private int x, y;

    /**
     * Creates a new cell entry with given coordinates
     * @param x Column index (0-25 for A-Z)
     * @param y Row index (0-99)
     */
    public CellEntry(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Converts cell coordinates to spreadsheet notation
     * @return String representation of the cell (e.g., "A0")
     */
    @Override
    public String toString() {
        char letter = 'A';
        letter = (char) (letter + x);
        return letter + "" + (y);
    }

    /**
     * Checks if the coordinates are within valid range
     * @return true if coordinates are valid (A-Z for columns, 0-99 for rows)
     */
    @Override
    public boolean isValid() {
        return x >= 0 && x < 26 && y >= 0 && y < 100;
    }

    /**
     * Extracts column index from cell reference
     * @param c Cell reference string (e.g., "A0")
     * @return Column index (0-25) or -1 if invalid
     */
    public int XCell(String c) {
        if (c == null || c.isEmpty()) {
            return -1;
        }
        char firstChar = c.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            return firstChar - 'A';
        }
        return -1;
    }
    
    /**
     * Extracts row index from cell reference
     * @param c Cell reference string (e.g., "A0")
     * @return Row index (0-99) or -1 if invalid
     */
    public int YCell(String c) {
        if (c == null || c.isEmpty() || c.length() < 2) {
            return -1;
        }

        String numberPart = c.substring(1);
        try {
            int number = Integer.parseInt(numberPart);
            if (number >= 0 && number < 100) {
                return number;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
        return -1;
    }

    /**
     * Gets the column index
     * @return Column index (0-25)
     */
    @Override
    public int getX() {
        return x;
    }

    /**
     * Gets the row index
     * @return Row index (0-99)
     */
    @Override
    public int getY() {
        return y;
    }
}