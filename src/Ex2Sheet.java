import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ex2Sheet implements Sheet {
    private Cell[][] table;

    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        initializeTable(x, y);
        eval();
    }

    private void initializeTable(int x, int y) {
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell(Ex2Utils.EMPTY_CELL, this, getCellName(i, j));
            }
        }
    }

    private String getCellName(int col, int row) {
        return String.valueOf((char)('A' + col)) + row;
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
            if (evaluatedValue != null) {
                return evaluatedValue;
            }
            evaluateCell(x, y);
            return sCell.getEvaluatedValue();
        }
        return "";
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
    public void set(int col, int row, String val) {
        if (!isIn(col, row)) return;

        if (val == null || val.trim().isEmpty()) {
            table[col][row] = new SCell(Ex2Utils.EMPTY_CELL, this, getCellName(col, row));
            return;
        }

        table[col][row] = new SCell(val, this, getCellName(col, row));
    }

    @Override
    public void eval() {
        int[][] depths = depth();

        // Reset previously evaluated values
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (table[i][j] instanceof SCell) {
                    ((SCell) table[i][j]).setEvaluatedValue(null);
                }
            }
        }

        // Evaluate cells by depth order
        for (int currentDepth = 0; currentDepth <= getMaxDepth(depths); currentDepth++) {
            for (int i = 0; i < width(); i++) {
                for (int j = 0; j < height(); j++) {
                    if (depths[i][j] == currentDepth) {
                        evaluateCell(i, j);
                    }
                }
            }
        }
    }

    private int getMaxDepth(int[][] depths) {
        int maxDepth = 0;
        for (int[] row : depths) {
            for (int depth : row) {
                if (depth > maxDepth) {
                    maxDepth = depth;
                }
            }
        }
        return maxDepth;
    }

    private void evaluateCell(int x, int y) {
        if (!isIn(x, y)) return;

        Cell cell = table[x][y];
        if (!(cell instanceof SCell)) return;

        SCell sCell = (SCell) cell;
        String data = sCell.getData();

        // Empty cell
        if (data == null || data.trim().isEmpty()) {
            sCell.setEvaluatedValue("");
            return;
        }

        // Formula
        if (data.startsWith("=")) {
            Double result = sCell.computeForm(data);
            if (result != null) {
                sCell.setEvaluatedValue(String.format("%.1f", result));
            } else {
                sCell.setEvaluatedValue("Error");
            }
            return;
        }

        // Number
        if (sCell.isNumber()) {
            try {
                double val = Double.parseDouble(data);
                sCell.setEvaluatedValue(String.format("%.1f", val));
            } catch (NumberFormatException e) {
                sCell.setEvaluatedValue(data);
            }
            return;
        }

        // Text
        sCell.setEvaluatedValue(data);
    }

    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < width() && yy < height();
    }

    @Override
    public int[][] depth() {
        int[][] depths = new int[width()][height()];
        boolean[][] visited = new boolean[width()][height()];

        // Initialize depths to -1
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                depths[i][j] = -1;
            }
        }

        // Calculate depth for each cell
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (table[i][j] != null) {
                    depths[i][j] = calculateCellDepth(i, j, visited, depths);
                }
            }
        }

        return depths;
    }

    private int calculateCellDepth(int row, int col, boolean[][] visited, int[][] depths) {
        if (!isIn(row, col) || table[row][col] == null) {
            return -1;
        }

        if (visited[row][col]) {
            return -1;
        }

        Cell cell = table[row][col];
        if (!(cell instanceof SCell) || !((SCell) cell).isForm()) {
            return 0;
        }

        String data = cell.getData();
        if (data == null || data.trim().isEmpty()) {
            return 0;
        }

        visited[row][col] = true;
        int maxDepth = 0;

        try {
            Pattern pattern = Pattern.compile("[A-Z][0-9]+");
            Matcher matcher = pattern.matcher(data);

            while (matcher.find()) {
                String ref = matcher.group();
                int nextCol = ref.charAt(0) - 'A';
                int nextRow = Integer.parseInt(ref.substring(1));

                if (isIn(nextCol, nextRow)) {
                    int d = calculateCellDepth(nextCol, nextRow, visited, depths);
                    if (d == -1) {
                        return -1;
                    }
                    maxDepth = Math.max(maxDepth, d);
                }
            }

            return maxDepth + 1;
        } finally {
            visited[row][col] = false;
        }
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
    public void save(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(width() + "," + height() + "\n");

            for (int i = 0; i < width(); i++) {
                for (int j = 0; j < height(); j++) {
                    Cell cell = table[i][j];
                    String cellData = cell.getData();
                    if (cellData == null || cellData.trim().isEmpty()) {
                        cellData = "EMPTY";
                    }
                    cellData = cellData.replace(",", "\\,").replace("\n", "\\n");
                    writer.write(cellData);

                    if (j < height() - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");
            }
        }
    }

    @Override
    public void load(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String[] dimensions = reader.readLine().split(",");
            int width = Integer.parseInt(dimensions[0]);
            int height = Integer.parseInt(dimensions[1]);

            table = new SCell[width][height];

            for (int i = 0; i < width; i++) {
                String[] row = reader.readLine().split("(?<!\\\\),");
                for (int j = 0; j < height; j++) {
                    String cellData = row[j]
                            .replace("\\,", ",")
                            .replace("\\n", "\n");

                    if (cellData.equals("EMPTY")) {
                        cellData = "";
                    }

                    table[i][j] = new SCell(cellData, this, getCellName(i, j));
                }
            }
            eval();
        }
    }

    private int[] parseCoordinates(String cords) {
        if (cords == null || cords.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid coordinates: empty input");
        }

        cords = cords.trim().toUpperCase();
        if (!cords.matches("[A-Z][0-9]+")) {
            throw new IllegalArgumentException("Invalid coordinates format: " + cords);
        }

        int col = cords.charAt(0) - 'A';
        int row = Integer.parseInt(cords.substring(1));

        if (!isIn(col, row)) {
            throw new IllegalArgumentException("Invalid coordinates: " + cords);
        }

        return new int[] { col, row };
    }
}