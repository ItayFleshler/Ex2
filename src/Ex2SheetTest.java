import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Ex2SheetTest {

    @Test
    public void testSetAndGet() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "123");
        assertEquals("123", sheet.get(0, 0).getData());

        sheet.set(1, 1, "=1+2");
        assertEquals("=1+2", sheet.get(1, 1).getData());
    }

    @Test
    public void testEval() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=1+2");
        sheet.eval();
        assertEquals("3.0", sheet.get(0, 0).getData());

        sheet.set(1, 1, "=3*3");
        sheet.eval();
        assertEquals("9.0", sheet.get(1, 1).getData());
    }

    @Test
    public void testInvalidFormula() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=1++2");
        sheet.eval();
        assertEquals(Ex2Utils.ERR_FORM, sheet.get(0, 0).getData());
    }

    @Test
    public void testCellReference() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "5");
        sheet.set(1, 1, "=A1+2");
        sheet.eval();
        assertEquals("7.0", sheet.get(1, 1).getData());
    }

    @Test
    public void testDivisionByZero() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=1/0");
        sheet.eval();
        assertEquals("Infinity", sheet.get(0, 0).getData());
    }
}