import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;

public class Ex2SheetTest {

    /**
     * Tests handling of various text cell inputs.
     * Verifies correct storage and retrieval of:
     * - Simple text strings
     * - Alphanumeric combinations
     * - Text with special characters
     * - Text starting with symbols
     */
    @Test
    void testTextCells() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "Hello");      // A0
        sheet.set(1, 0, "2a");         // B0
        sheet.set(2, 0, "{2}");        // C0
        sheet.set(3, 0, "@123");       // D0

        assertEquals("Hello", sheet.value(0, 0));
        assertEquals("2a", sheet.value(1, 0));
        assertEquals("{2}", sheet.value(2, 0));
        assertEquals("@123", sheet.value(3, 0));
    }

    /**
     * Tests evaluation of valid formula expressions.
     * Verifies correct calculation of:
     * - Simple numeric formulas
     * - Decimal values
     * - Parenthesized expressions
     * - Basic arithmetic operations
     * - Complex nested expressions
     */
    @Test
    void testValidFormulas() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=1");             // A0
        sheet.set(1, 0, "=1.2");           // B0
        sheet.set(2, 0, "=(0.2)");         // C0
        sheet.set(3, 0, "=1+2");           // D0
        sheet.set(4, 0, "=1+2*3");         // E0
        sheet.set(5, 0, "=(1+2)*((3))-1"); // F0
        sheet.eval();

        assertEquals("1.0", sheet.value(0, 0));
        assertEquals("1.2", sheet.value(1, 0));
        assertEquals("0.2", sheet.value(2, 0));
        assertEquals("3.0", sheet.value(3, 0));
        assertEquals("7.0", sheet.value(4, 0));
        assertEquals("8.0", sheet.value(5, 0));
    }

    /**
     * Tests handling of invalid formula inputs.
     * Verifies error handling for:
     * - Invalid text inputs
     * - Malformed expressions
     * - Invalid operator combinations
     * - Empty formulas
     */
    @Test
    void testInvalidFormulaTypes() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "a");          // A0
        sheet.set(1, 0, "AB");         // B0
        sheet.set(2, 0, "@2");         // C0
        sheet.set(3, 0, "2+)");        // D0
        sheet.set(4, 0, "(3+1*2)-");   // E0
        sheet.set(5, 0, "=()");        // F0
        sheet.set(6, 0, "=5**");       // G0
        sheet.eval();

        assertEquals("a", sheet.value(0, 0));
        assertEquals("AB", sheet.value(1, 0));
        assertEquals("@2", sheet.value(2, 0));
        assertEquals("2+)", sheet.value(3, 0));
        assertEquals("(3+1*2)-", sheet.value(4, 0));
        assertEquals("ERR_FORM!", sheet.value(5, 0));
        assertEquals("ERR_FORM!", sheet.value(6, 0));
    }

    /**
     * Tests detection of cyclic dependencies between cells.
     * Verifies:
     * - Complex circular reference detection
     * - Proper depth calculation for cyclic references
     * - Multiple cell cycle handling
     */
    @Test
    void testComplexCyclicReferences() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=B0+1");      // A0
        sheet.set(1, 0, "=C0+2");      // B0
        sheet.set(2, 0, "=D0+3");      // C0
        sheet.set(3, 0, "=A0+4");      // D0

        int[][] depths = sheet.depth();
        for (int i = 0; i < 4; i++) {
            assertEquals(-1, depths[i][0]);
        }
    }

    /**
     * Tests dependency depth calculations.
     * Verifies correct depth assignment for:
     * - Independent cells (depth 0)
     * - Direct references (depth 1)
     * - Multi-level dependencies
     */
    @Test
    void testComplexDepthCalculation() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "5");          // A0: depth 0
        sheet.set(1, 0, "3");          // B0: depth 0
        sheet.set(2, 0, "=A0+B0");     // C0: depth 1
        sheet.set(3, 0, "=C0*2");      // D0: depth 2
        sheet.set(4, 0, "=D0+A0");     // E0: depth 3
        sheet.set(0, 1, "=E0/2");      // A1: depth 4

        int[][] depths = sheet.depth();
        assertEquals(0, depths[0][0]);
        assertEquals(0, depths[1][0]);
        assertEquals(1, depths[2][0]);
        assertEquals(2, depths[3][0]);
        assertEquals(3, depths[4][0]);
        assertEquals(4, depths[0][1]);
    }

    /**
     * Tests basic mathematical operations.
     * Verifies correct calculation of:
     * - Division
     * - Multiplication
     * - Addition
     * - Subtraction
     */
    @Test
    void testMathOperations() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=10/2");      // A0
        sheet.set(1, 0, "=10*2");      // B0
        sheet.set(2, 0, "=10+2");      // C0
        sheet.set(3, 0, "=10-2");      // D0
        sheet.eval();

        assertEquals("5.0", sheet.value(0, 0));
        assertEquals("20.0", sheet.value(1, 0));
        assertEquals("12.0", sheet.value(2, 0));
        assertEquals("8.0", sheet.value(3, 0));
    }

    /**
     * Tests division by zero handling.
     * Verifies proper handling of:
     * - Direct division by zero
     * - Indirect division by zero
     * - Positive and negative infinity results
     */
    @Test
    void testDivisionByZero() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=1/0");
        sheet.set(1, 0, "=10/(5-5)");
        sheet.set(2, 0, "=-1/0");
        sheet.set(3, 0, "=-10/(5-5)");
        sheet.eval();

        assertEquals("Infinity", sheet.value(0, 0));
        assertEquals("Infinity", sheet.value(1, 0));
        assertEquals("-Infinity", sheet.value(2, 0));
        assertEquals("-Infinity", sheet.value(3, 0));
    }

    /**
     * Tests evaluation of complex formula expressions.
     * Verifies correct handling of:
     * - Nested parentheses
     * - Multiple operations
     * - Order of operations
     */
    @Test
    void testLargeFormulas() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=((1+2)*(3+4))/(2+3)");
        sheet.eval();
        assertEquals("4.2", sheet.value(0, 0));
    }

    /**
     * Tests save and load functionality with complex data.
     * Verifies:
     * - Saving and loading of different cell types
     * - Formula preservation
     * - Depth calculation preservation
     */
    @Test
    void testSaveLoadComplex() throws IOException {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "5");
        sheet.set(1, 0, "=A0+2");
        sheet.set(2, 0, "=B0*2");
        sheet.set(3, 0, "Some Text");
        sheet.eval();

        String tempFile = "test_complex_sheet.csv";
        sheet.save(tempFile);

        Sheet loadedSheet = new Ex2Sheet();
        loadedSheet.load(tempFile);
        loadedSheet.eval();

        assertEquals(sheet.value(0, 0), loadedSheet.value(0, 0));
        assertEquals(sheet.value(1, 0), loadedSheet.value(1, 0));
        assertEquals(sheet.value(2, 0), loadedSheet.value(2, 0));
        assertEquals(sheet.value(3, 0), loadedSheet.value(3, 0));

        assertArrayEquals(sheet.depth(), loadedSheet.depth());

        new File(tempFile).delete();
    }

    /**
     * Tests nested cell reference handling.
     * Verifies:
     * - Direct cell references
     * - Chained references
     * - Combined references in formulas
     */
    @Test
    void testNestedCellReferences() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "=A0");
        sheet.set(0, 2, "=A1");
        sheet.set(0, 3, "=A2+A0");
        sheet.eval();

        assertEquals("5.0", sheet.value(0, 0));
        assertEquals("5.0", sheet.value(0, 1));
        assertEquals("5.0", sheet.value(0, 2));
        assertEquals("10.0", sheet.value(0, 3));
    }

    /**
     * Tests mixed operations with cell references.
     * Verifies correct evaluation of:
     * - Combined cell references and constants
     * - Multi-step calculations
     * - Order of operations with references
     */
    @Test
    void testMixedOperationsWithCellReferences() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "10");
        sheet.set(1, 0, "=A0*2");
        sheet.set(2, 0, "=(B0+A0)/2");
        sheet.eval();

        assertEquals("10.0", sheet.value(0, 0));
        assertEquals("20.0", sheet.value(1, 0));
        assertEquals("15.0", sheet.value(2, 0));
    }

    /**
     * Tests edge cases in mathematical operations.
     * Verifies correct handling of:
     * - Floating point arithmetic
     * - Scientific notation
     * - Precision handling
     */
    @Test
    void testEdgeCaseMathOperations() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=0.1+0.2");
        sheet.set(1, 0, "=1e5");
        sheet.eval();

        assertEquals("0.3", sheet.value(0, 0));
        assertEquals("100000.0", sheet.value(1, 0));
    }

    /**
     * Tests cell content updates and recalculation.
     * Verifies:
     * - Formula updates
     * - Value recalculation
     * - Type conversion handling
     */
    @Test
    void testMultipleUpdates() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=1+1");
        sheet.eval();
        assertEquals("2.0", sheet.value(0, 0));

        sheet.set(0, 0, "=2+2");
        sheet.eval();
        assertEquals("4.0", sheet.value(0, 0));

        sheet.set(0, 0, "Not a formula");
        sheet.eval();
        assertEquals("Not a formula", sheet.value(0, 0));
    }

    /**
     * Tests complex parentheses handling in formulas.
     * Verifies:
     * - Nested parentheses
     * - Multiple groupings
     * - Complex expressions with parentheses
     */
    @Test
    void testComplexParentheses() {
        Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=(((1)))");
        sheet.set(1, 0, "=((2)+(3))");
        sheet.set(2, 0, "=(1+(2+(3)))");
        sheet.eval();

        assertEquals("1.0", sheet.value(0, 0));
        assertEquals("5.0", sheet.value(1, 0));
        assertEquals("6.0", sheet.value(2, 0));
    }

    /**
     * Tests basic circular dependency detection.
     * Verifies error handling for:
     * - Direct circular references between two cells
     */
    @Test
    public void testSimpleCircularDependency() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=B0");
        sheet.set(1, 0, "=A0");

        sheet.eval();
        assertEquals("ERR_CYCLE!", sheet.value(0, 0));
        assertEquals("ERR_CYCLE!", sheet.value(1, 0));
    }

    /**
     * Tests complex circular dependency detection.
     * Verifies error handling for:
     * - Multi-cell circular dependencies
     * - Long dependency chains that form cycles
     */
    @Test
    public void testComplexCircularDependency() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=B0");      // A0 -> B0
        sheet.set(1, 0, "=C0");      // B0 -> C0
        sheet.set(2, 0, "=D0");      // C0 -> D0
        sheet.set(3, 0, "=A0");      // D0 -> A0 (creates cycle)

        sheet.eval();
        assertEquals("ERR_CYCLE!", sheet.value(0, 0));
        assertEquals("ERR_CYCLE!", sheet.value(1, 0));
        assertEquals("ERR_CYCLE!", sheet.value(2, 0));
        assertEquals("ERR_CYCLE!", sheet.value(3, 0));
    }

    /**
     * Tests self-referential circular dependency.
     * Verifies error handling for:
     * - Cell referencing itself
     */
    @Test
    public void testSelfCircularDependency() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=A0");

        sheet.eval();
        assertEquals("ERR_CYCLE!", sheet.value(0, 0));
    }

    /**
     * Tests circular dependencies with calculations.
     * Verifies error handling for:
     * - Circular references with arithmetic operations
     */
    @Test
    public void testCircularDependencyWithCalculations() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=B0+1");
        sheet.set(1, 0, "=A0*2");

        sheet.eval();
        assertEquals("ERR_CYCLE!", sheet.value(0, 0));
        assertEquals("ERR_CYCLE!", sheet.value(1, 0));
    }

    /**
     * Tests circular dependencies in complex formulas.
     * Verifies error handling for:
     * - Circular references embedded in larger formulas
     */
    @Test
    public void testCircularDependencyInLargerFormula() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=5+B0*2");
        sheet.set(1, 0, "=A0/2");

        sheet.eval();
        assertEquals("ERR_CYCLE!", sheet.value(0, 0));
        assertEquals("ERR_CYCLE!", sheet.value(1, 0));
    }

    /**
     * Tests partial circular dependencies.
     * Verifies error handling for:
     * - Cells depending on circular references
     * - Error propagation through dependencies
     */
    @Test
    public void testPartialCircularDependency() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=B0");
        sheet.set(1, 0, "=C0");
        sheet.set(2, 0, "=A0");
        sheet.set(3, 0, "=A0+1");

        sheet.eval();
        assertEquals("ERR_CYCLE!", sheet.value(0, 0));
        assertEquals("ERR_CYCLE!", sheet.value(1, 0));
        assertEquals("ERR_CYCLE!", sheet.value(2, 0));
        assertEquals("ERR_CYCLE!", sheet.value(3, 0));
    }

    /**
     * Tests empty cell dependency handling.
     * Verifies:
     * - Depth calculation for empty cell dependencies
     * - Error handling for references to empty cells
     */
    @Test
    public void testEmptyCellDependency() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=B0+1");

        int[][] result = sheet.depth();

        assertEquals(-1, result[0][0]);
        assertEquals(0, result[0][1]);
    }

    /**
     * Tests long dependency chain handling.
     * Verifies:
     * - Correct depth calculation for long chains
     * - Formula evaluation in dependency chains
     */
    @Test
    public void testLongDependencyChain() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "1");
        sheet.set(1, 0, "=A0+1");
        sheet.set(2, 0, "=B0+2");
        sheet.set(3, 0, "=C0+3");
        sheet.set(4, 0, "=D0+4");

        int[][] result = sheet.depth();

        assertEquals(0, result[0][0]);
        assertEquals(1, result[1][0]);
        assertEquals(2, result[2][0]);
        assertEquals(3, result[3][0]);
        assertEquals(4, result[4][0]);
    }

    /**
     * Tests depth calculation in empty spreadsheet.
     * Verifies:
     * - Depth handling for blank cells
     */
    @Test
    public void testBlankSheetDepth() {
        Ex2Sheet sheet = new Ex2Sheet();
        int[][] result = sheet.depth();

        assertEquals(0, result[0][0]);
        assertEquals(0, result[1][1]);
        assertEquals(0, result[2][2]);
    }

    /**
     * Tests depth calculation with mixed formula types.
     * Verifies depth calculation for:
     * - Direct values
     * - Cell references
     * - Constant formulas
     */
    @Test
    public void testMixedFormulaTypes() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "10");
        sheet.set(0, 1, "=A0+5");
        sheet.set(1, 0, "=10+20");
        sheet.set(1, 1, "15");

        int[][] result = sheet.depth();

        assertEquals(0, result[0][0]);
        assertEquals(1, result[0][1]);
        assertEquals(0, result[1][0]);
        assertEquals(0, result[1][1]);
    }

    /**
     * Tests constant formula depth calculation.
     * Verifies:
     * - Depth calculation for formulas without references
     */
    @Test
    public void testConstantFormula() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "=5*2");

        int[][] result = sheet.depth();

        assertEquals(0, result[0][0]);
        assertEquals(0, result[0][1]);
    }
}