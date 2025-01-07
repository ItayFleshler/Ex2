import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (!isIn(x, y)) {
            return Ex2Utils.EMPTY_CELL;
        }

        Cell cell = table[x][y];
        if (cell instanceof SCell) {
            SCell sCell = (SCell) cell;
            String evaluatedValue = sCell.getEvaluatedValue();
            if (evaluatedValue != null && !evaluatedValue.isEmpty()) {
                return evaluatedValue;
            }
            // אם אין ערך מחושב, מנסה לחשב מחדש
            eval();
            evaluatedValue = sCell.getEvaluatedValue();
            if (evaluatedValue != null && !evaluatedValue.isEmpty()) {
                return evaluatedValue;
            }
            String data = cell.getData();
            return data != null && !data.trim().isEmpty() ? data : Ex2Utils.EMPTY_CELL;
        }
        return Ex2Utils.EMPTY_CELL;
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
        int[][] depths = depth();

        int maxDepth = 0;
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (depths[i][j] > maxDepth) {
                    maxDepth = depths[i][j];
                }
            }
        }

        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (table[i][j] instanceof SCell) {
                    ((SCell) table[i][j]).setEvaluatedValue(null);
                }
            }
        }

        for (int currentDepth = 0; currentDepth <= maxDepth; currentDepth++) {
            for (int i = 0; i < width(); i++) {
                for (int j = 0; j < height(); j++) {
                    if (depths[i][j] == currentDepth) {
                        evaluateCell(i, j);
                    }
                }
            }
        }
    }

    private void evaluateCell(int row, int col) {
        if (!isIn(row, col)) {
            return;
        }

        Cell cell = table[row][col];
        if (!(cell instanceof SCell)) {
            return;
        }

        SCell sCell = (SCell) cell;
        String data = sCell.getData();

        if (data == null || data.trim().isEmpty()) {
            sCell.setEvaluatedValue("");
            return;
        }

        if (!sCell.isForm()) {
            sCell.setEvaluatedValue(data);
            return;
        }

        try {
            // בדיקה אם זו הפניה ישירה לתא (למשל =A1)
            if (data.matches("=[A-Z][0-9]+")) {
                String cellRef = data.substring(1); // מסיר את ה-=
                CellEntry entry = new CellEntry(0, 0);
                int refCol = entry.XCell(cellRef);
                int refRow = entry.YCell(cellRef);

                if (isIn(refCol, refRow)) {
                    Cell refCell = table[refCol][refRow];
                    if (refCell instanceof SCell) {
                        SCell refSCell = (SCell) refCell;
                        String refValue = refSCell.getEvaluatedValue();
                        if (refValue != null && !refValue.isEmpty()) {
                            sCell.setEvaluatedValue(refValue);
                        } else {
                            sCell.setEvaluatedValue(refSCell.getData());
                        }
                    }
                }
                return;
            }

            String formula = data;
            Pattern cellPattern = Pattern.compile("[A-Z][0-9]+");
            Matcher matcher = cellPattern.matcher(formula);
            StringBuffer evaluatedFormula = new StringBuffer();

            while (matcher.find()) {
                String cellRef = matcher.group();
                CellEntry entry = new CellEntry(0, 0);
                int refCol = entry.XCell(cellRef);
                int refRow = entry.YCell(cellRef);

                if (refCol == -1 || refRow == -1) {
                    continue;
                }

                String cellValue = "0";
                if (isIn(refCol, refRow)) {
                    Cell refCell = table[refCol][refRow];
                    if (refCell instanceof SCell) {
                        SCell refSCell = (SCell) refCell;
                        String evaluatedValue = refSCell.getEvaluatedValue();
                        if (evaluatedValue != null && !evaluatedValue.isEmpty()) {
                            cellValue = evaluatedValue;
                        } else {
                            evaluateCell(refCol, refRow);
                            evaluatedValue = refSCell.getEvaluatedValue();
                            if (evaluatedValue != null && !evaluatedValue.isEmpty()) {
                                cellValue = evaluatedValue;
                            } else {
                                cellValue = refSCell.getData();
                                if (cellValue.startsWith("=")) {
                                    cellValue = "0";
                                }
                            }
                        }
                    }
                }
                matcher.appendReplacement(evaluatedFormula, cellValue);
            }
            matcher.appendTail(evaluatedFormula);

            Double result = sCell.computeForm(evaluatedFormula.toString());
            if (result != null) {
                sCell.setEvaluatedValue(result.toString());
            } else {
                sCell.setEvaluatedValue("Error");
            }
        } catch (Exception e) {
            System.err.println("Error evaluating cell [" + row + "," + col + "]: " + e.getMessage());
            sCell.setEvaluatedValue("Error");
        }
    }

    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < width() && yy < height();
    }

    @Override
    public int[][] depth() {
        int[][] depths = new int[width()][height()];
        boolean[][] visited = new boolean[width()][height()];

        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                depths[i][j] = calculateCellDepth(i, j, visited);
            }
        }

        return depths;
    }

    private int calculateCellDepth(int row, int col, boolean[][] visited) {
        if (!isIn(row, col)) {
            return 0;
        }

        if (visited[row][col]) {
            return -1;
        }

        Cell cell = table[row][col];
        if (!(cell instanceof SCell)) {
            return 0;
        }

        SCell sCell = (SCell) cell;
        String data = sCell.getData();

        if (data == null || data.trim().isEmpty() || !sCell.isForm()) {
            return 0;
        }

        visited[row][col] = true;
        int maxDepth = 0;

        try {
            Pattern cellPattern = Pattern.compile("[A-Z][0-9]");
            Matcher matcher = cellPattern.matcher(data);

            while (matcher.find()) {
                String cellRef = matcher.group();
                int refCol = cellRef.charAt(0) - 'A';
                int refRow = cellRef.charAt(1) - '0';

                int refDepth = calculateCellDepth(refRow, refCol, visited);

                if (refDepth == -1) {
                    return -1;
                }

                maxDepth = Math.max(maxDepth, refDepth);
            }

        } finally {
            visited[row][col] = false;
        }

        return maxDepth + 1;
    }

    @Override
    public String eval(int x, int y) {
        if (!isIn(x, y)) {
            return null;
        }

        eval();

        Cell cell = get(x, y);
        if (cell instanceof SCell) {
            String evaluatedValue = ((SCell) cell).getEvaluatedValue();
            if (evaluatedValue != null && !evaluatedValue.isEmpty()) {
                return evaluatedValue;
            }
            return cell.getData();
        }

        return null;
    }

    @Override
    public void load(String fileName) throws IOException {
        // Placeholder for load logic
    }

    @Override
    public void save(String fileName) throws IOException {
        // Placeholder for save logic
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