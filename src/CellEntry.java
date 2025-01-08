public class CellEntry implements Index2D {
    private int x,y;

    @Override
    public String toString() {
        char letter = 'A';
        letter = (char) (letter + x);
        return letter + "" + (y);
    }

    public CellEntry(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean isValid() {
        return x >= 0 && x < 26 && y >= 0 && y < 100;
    }

    public int XCell(String c) {
        if (c == null || c.isEmpty()) {
            return -1;
        }
        // מטפל באות הראשונה בלבד
        char firstChar = c.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            return firstChar - 'A';
        }
        return -1;
    }

    public int YCell(String c) {
        if (c == null || c.isEmpty() || c.length() < 2) {
            return -1;
        }

        // מוצא את החלק המספרי (כל מה שאחרי האות)
        String numberPart = c.substring(1);
        try {
            int number = Integer.parseInt(numberPart);
            // בדיקה שהמספר בטווח תקין
            if (number >= 0 && number < 100) {
                return number;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
        return -1;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }
}