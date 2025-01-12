import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements a spreadsheet with support for text, numbers, and formulas.
 * Provides functionality for cell evaluation, depth calculation, and file I/O.
 */
public class Ex2Sheet implements Sheet {
    private Cell[][] table;

    /**
     * Creates a new spreadsheet with specified dimensions
     * @param x Width of the spreadsheet
     * @param y Height of the spreadsheet
     */
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        initializeTable(x, y);
        eval();
    }

    /**
     * Creates a new spreadsheet with default dimensions
     */
    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    /**
     * Initializes the spreadsheet with empty cells
     * @param x Width of the spreadsheet
     * @param y Height of the spreadsheet
     */
    private void initializeTable(int x, int y) {
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell(Ex2Utils.EMPTY_CELL, this);
            }
        }
    }

    /**
     * Gets the evaluated value of a cell at specified coordinates
     */
    @Override
    public String value(int x, int y) {
        if (!isIn(x, y)) {
            return Ex2Utils.EMPTY_CELL;
        }

        Cell cell = table[x][y];
        if (cell instanceof SCell sCell) {
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
        CellEntry entry = CellEntry.parseEntry(cords);
        return (entry != null && entry.isValid()) ? get(entry.getX(), entry.getY()) : null;
    }


    @Override
    public int width() {
        return table.length;
    }

    @Override
    public int height() {
        return table[0].length;
    }

    /**
     * Sets the value of a cell at specified coordinates.
     * Handles null/empty values by creating empty cells.
     * @param col Column index
     * @param row Row index
     * @param val New cell value
     */
    @Override
    public void set(int col, int row, String val) {
        if (!isIn(col, row)) return;

        if (val == null || val.trim().isEmpty()) {
            table[col][row] = new SCell(Ex2Utils.EMPTY_CELL, this);
            return;
        }

        table[col][row] = new SCell(val, this);
    }

    /**
     * Evaluates all cells in the spreadsheet.
     * Processes cells in order of their dependency depth.
     * Handles circular dependencies and formula errors.
     * Evaluation Process:
     * 1. Calculate dependency depths
     * 2. Reset previous evaluations
     * 3. Evaluate cells in depth order
     * 4. Handle errors and circular references
     */
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

    /**
     * Finds the maximum dependency depth in the spreadsheet.
     * @param depths Array of cell depths
     * @return Maximum depth found
     */
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

    /**
     * Evaluates a single cell at specified coordinates
     */
    private void evaluateCell(int x, int y) {
        if (!isIn(x, y)) return;

        Cell cell = table[x][y];
        if (!(cell instanceof SCell sCell)) return;

        String data = sCell.getData();

        // Empty cell
        if (data == null || data.trim().isEmpty()) {
            sCell.setEvaluatedValue("");
            return;
        }

        // Formula
        if (data.startsWith("=")) {
            // Check for circular dependency
            int depth = calculateCellDepth(x, y, new boolean[width()][height()]);
            if (depth == Ex2Utils.ERR_CYCLE_FORM) {
                sCell.setType(Ex2Utils.ERR_CYCLE_FORM);  // Update cell type
                sCell.setEvaluatedValue(Ex2Utils.ERR_CYCLE);
                return;
            }

            Double result = sCell.computeForm(data);
            if (result != null) {
                sCell.setType(Ex2Utils.FORM);  // Valid formula
                sCell.setEvaluatedValue(String.format("%.1f", result));
            } else {
                sCell.setType(Ex2Utils.ERR_FORM_FORMAT);  // Formula format error
                sCell.setEvaluatedValue(Ex2Utils.ERR_FORM);
            }
            return;
        }

        // Number
        if (sCell.isNumber()) {
            try {
                double val = Double.parseDouble(data);
                sCell.setType(Ex2Utils.NUMBER);
                sCell.setEvaluatedValue(String.format("%.1f", val));
            } catch (NumberFormatException e) {
                sCell.setType(Ex2Utils.TEXT);
                sCell.setEvaluatedValue(data);
            }
            return;
        }

        // Text
        sCell.setType(Ex2Utils.TEXT);
        sCell.setEvaluatedValue(data);
    }

    /**
     * Checks if coordinates are within spreadsheet bounds
     */
    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < width() && yy < height();
    }

    /**
     * Calculates dependency depths for all cells.
     * Identifies circular references and formula chains.
     * @return 2D array of dependency depths
     * Depth Values:
     * - 0: Independent cells
     * - >0: Formula cells (depth increases with dependency chain)
     * - ERR_CYCLE_FORM: Circular reference detected
     */
    @Override
    public int[][] depth() {
        int[][] depths = new int[width()][height()];
        boolean[][] visited = new boolean[width()][height()];

        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (!visited[i][j]) {
                    depths[i][j] = calculateCellDepth(i, j, new boolean[width()][height()]);
                }
            }
        }
        return depths;
    }

    /**
     * Calculates the dependency depth of a single cell
     */
    private int calculateCellDepth(int row, int col, boolean[][] visited) {
        if (!isIn(row, col)) {
            return Ex2Utils.ERR_CYCLE_FORM;
        }

        Cell cell = table[row][col];
        if (cell == null) {
            return Ex2Utils.ERR_CYCLE_FORM;
        }

        String data = cell.getData();
        if (data == null || data.trim().isEmpty()) {
            return 0;
        }

        if (!data.startsWith("=")) {
            return 0;  // Not a formula - no dependencies
        }

        String content = data.substring(1).trim();
        if (content.matches("^-?\\d*\\.?\\d+[eE][-+]?\\d+$")) {
            return 0;  // Scientific notation - treat as literal value
        }

        Pattern pattern = Pattern.compile("[A-Za-z][0-9]+");
        Matcher matcher = pattern.matcher(data);
        if (!matcher.find()) {
            return 0;  // No cell references - no dependencies
        }

        matcher.reset();
        int maxDepth = 0;

        // Create temporary visited array to allow self-reference in formulas
        boolean[][] tempVisited = new boolean[width()][height()];
        for (int i = 0; i < width(); i++) {
            System.arraycopy(visited[i], 0, tempVisited[i], 0, height());
        }
        tempVisited[row][col] = true;

        while (matcher.find()) {
            String ref = matcher.group();
            CellEntry entry = CellEntry.parseEntry(ref);

            // If invalid reference, return error
            if (entry == null || !entry.isValid()) {
                return Ex2Utils.ERR_CYCLE_FORM;
            }

            int nextCol = entry.getX();
            int nextRow = entry.getY();

            Cell dependentCell = table[nextCol][nextRow];
            if (dependentCell == null || dependentCell.getData() == null || dependentCell.getData().trim().isEmpty()) {
                return Ex2Utils.ERR_CYCLE_FORM;
            }

            // Check for true circular dependencies using original visited array
            if (visited[nextCol][nextRow]) {
                return Ex2Utils.ERR_CYCLE_FORM;
            }

            // Recursive depth calculation using temporary visited array
            int depth = calculateCellDepth(nextCol, nextRow, tempVisited);
            if (depth == Ex2Utils.ERR_CYCLE_FORM) {
                return Ex2Utils.ERR_CYCLE_FORM;
            }
            maxDepth = Math.max(maxDepth, depth);
        }

        // Update original visited array only if no circular dependencies found
        for (int i = 0; i < width(); i++) {
            System.arraycopy(tempVisited[i], 0, visited[i], 0, height());
        }

        return maxDepth + 1;
    }

    /**
     * Evaluates all cells in the spreadsheet.
     * Processes cells in order of their dependency depth.
     * Handles circular dependencies and formula errors.
     */
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

    /**
     * Saves the spreadsheet to a CSV file.
     * Handles special characters and empty cells.
     * @param fileName Path to save file
     * @throws IOException if file operations fail
     * File Format:
     */
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

    /**
     * Loads spreadsheet data from a CSV file.
     * Restores cell values and evaluates formulas.
     * @param fileName Path to input file
     * @throws IOException if file operations fail
     * @throws NumberFormatException if dimensions are invalid
     */
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

                    table[i][j] = new SCell(cellData, this);
                }
            }
            eval();
        }
    }
}