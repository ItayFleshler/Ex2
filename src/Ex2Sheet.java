import java.io.IOException;

public class Ex2Sheet implements Sheet {
    private Cell[][] table;

    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell(Ex2Utils.EMPTY_CELL);
            }
        }
        eval();
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL;

        try {
            Cell c = get(x, y);

            if (c != null) {
                if (c instanceof SCell) {
                    SCell sCell = (SCell) c;
                    if (sCell.isForm()) {
                        try {
                            // Debug: Printing the formula for debugging purposes
                            System.out.println("Debug: Computing formula: " + sCell.getData());

                            Double computedValue = sCell.computeForm(sCell.getData());
                            ans = computedValue.toString();
                        } catch (Exception e) {
                            ans = "Error: " + e.getMessage();
                            e.printStackTrace();
                        }
                    } else {
                        ans = sCell.toString();
                    }
                } else {
                    ans = c.toString();
                }
            }
        } catch (Exception e) {
            System.out.println("Error in value(): " + e.getMessage());
            e.printStackTrace();
        }

        return ans;
    }

    @Override
    public Cell get(int x, int y) {
        if (isIn(x, y)) {
            return table[x][y];
        }
        return null;
    }

    @Override
    public Cell get(String cords) {
        try {
            int[] xy = parseCoordinates(cords);
            return get(xy[0], xy[1]);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int width() {
        return table.length;
    }

    @Override
    public int height() {
        return table[0].length;
    }

    @Override
    public void set(int x, int y, String s) {
        if (isIn(x, y)) {
            Cell c = new SCell(s);
            table[x][y] = c;
            eval();
        }
    }

    @Override
    public void eval() {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                Cell c = table[i][j];

                if (c instanceof SCell) { // בודקים אם התא הוא String Cell
                    SCell sCell = (SCell) c;

                    if (sCell.isForm()) { // אם התא מכיל נוסחה
                        try {
                            Double computedValue = sCell.computeForm(sCell.getData()); // מחשבים ערך
                            sCell.setEvaluatedValue(computedValue.toString()); // שומרים את הערך המחושב
                        } catch (Exception e) {
                            sCell.setEvaluatedValue("Error"); // במקרה של שגיאה מחזירים "Error"
                        }
                    } else {
                        sCell.setEvaluatedValue(sCell.getData()); // אם זה לא נוסחה, משאירים את המידע המקורי
                    }
                }
            }
        }
    }

    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < width() && yy < height();
    }

    @Override
    public int[][] depth() {
        return new int[width()][height()];
    }

    @Override
    public void load(String fileName) throws IOException {
        // Placeholder for load logic
    }

    @Override
    public void save(String fileName) throws IOException {
        // Placeholder for save logic
    }

    @Override
    public String eval(int x, int y) {
        String ans = null;
        if (get(x, y) != null) {
            ans = value(x, y);
        }
        return ans;
    }

    private int[] parseCoordinates(String cords) {
        cords = cords.trim().toUpperCase();
        int col = cords.charAt(0) - 'A';
        int row = Integer.parseInt(cords.substring(1)) - 1;

        if (!isIn(row, col)) {
            throw new IllegalArgumentException("Invalid coordinates: " + cords);
        }

        return new int[] { row, col };
    }
}