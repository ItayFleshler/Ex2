// SCell Class Implementation
public class SCell implements Cell {
    private String line;
    private int type;

    // Constructor
    public SCell(String s) {
        setData(s);
        setType(Ex2Utils.TEXT); // ערך ברירת מחדל
    }

    // בדיקה האם הנתון מייצג מספר
    public boolean isNumber() {
        String data = getData();
        if (data == null || data.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(data.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // בדיקה האם הנתון מייצג טקסט
    public boolean isText() {
        String data = getData();
        if (data == null || data.trim().isEmpty()) {
            return false;
        }
        // טקסט אינו מתחיל ב-=
        if (data.charAt(0) == '=') {
            return false;
        }
        return !isNumber() && !isForm();
    }

    // בדיקה האם הנתון מייצג נוסחה חוקית
    public boolean isForm() {
        String data = getData();
        if (data == null || !data.startsWith("=")) {
            return false;
        }

        String formula = data.substring(1); // Ignore the "="
        if (formula.startsWith("-")) {
            formula = formula.substring(1); // Allow formulas like =-3+2
        }
        if (formula.isEmpty()) { // Empty formula is not valid
            return false;
        }

        String[] formParts = formula.split("[+\\-*/()]");
        for (String part : formParts) {
            if (!part.trim().isEmpty() && !isValidPart(part.trim())) {
                return false;
            }
        }

        return true;
    }


    // בדיקת האם חלק מסוים בנוסחה חוקי
    private boolean isValidPart(String part) {
        if (part.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(part); // אם החלק מספר חוקי
            return true;
        } catch (NumberFormatException e) {
            // אם החלק לכאורה מתייחס לתא אחר בטבלה
            return part.matches("[A-Za-z]+[0-9]+");
        }
    }

    // חישוב ערך הנוסחה
    public Double computeForm(String form) {
        form = form.replaceAll("\\s", ""); // הסרת רווחים מיותרים
        if (form.startsWith("=")) {
            form = form.substring(1); // התעלמות מה־"="
        }

        try {
            Double d = Double.parseDouble(form);
            return d;
        } catch (NumberFormatException e) {}


        // אם המחרוזת מוקפת בסוגריים, מבטלים את הסוגריים החיצוניים
        if (form.startsWith("(") && form.endsWith(")")) {
            return computeForm(form.substring(1, form.length() - 1));
        }

        int operatorIndex = findMainOperator(form); // חיפוש אופרטור מרכזי
        if (operatorIndex != -1) {
            String leftOperand = form.substring(0, operatorIndex).trim(); // חלק שמאלי של הפורמולה
            String operator = form.substring(operatorIndex, operatorIndex + 1); // האופרטור
            String rightOperand = form.substring(operatorIndex + 1).trim(); // חלק ימני של הפורמולה

            // חישוב ערכים של כל אחד מהאופרנדים
            Double leftValue = computeForm(leftOperand);
            Double rightValue = computeForm(rightOperand);

            // ביצוע חישוב לפי סוג האופרטור
            switch (operator) {
                case "+":
                    return leftValue + rightValue;
                case "-":
                    return leftValue - rightValue;
                case "*":
                    return leftValue * rightValue;
                case "/":
                    if (rightValue == 0) {
                        throw new ArithmeticException("Infinity"); // מניעת חילוק באפס
                    }
                    return leftValue / rightValue;
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
        }

        throw new IllegalArgumentException("Invalid formula: " + form); // במקרה שלא נמצא אופרטור ובמחרוזת לא נשאר מספר
    }



    // חידוש: חיפוש אופרטור ראשי מחוץ לסוגריים בלבד
    private int findMainOperator(String form) {
        int openParentheses = 0;

        for (int i = 0; i < form.length(); i++) {
            char ch = form.charAt(i);

            if (ch == '(') {
                openParentheses++; // נתקלנו בסוגר שמאלי
            } else if (ch == ')') {
                openParentheses--; // סוגר ימני
            }

            // מחפש את האופרטור המרכזי מחוץ לסוגריים
            if (openParentheses == 0 && (ch == '+' || ch == '-' || ch == '*' || ch == '/')) {
                return i; // מחזיר את האינדקס של האופרטור
            }
        }

        return -1; // אם לא נמצא אופרטור
    }

    // Field to store the evaluated value of the formula
    private String evaluatedValue;

    // Getter for evaluatedValue
    public String getEvaluatedValue() {
        return evaluatedValue;
    }

    // Setter for evaluatedValue
    public void setEvaluatedValue(String evaluatedValue) {
        this.evaluatedValue = evaluatedValue;
    }

    // Return evaluated value if set, otherwise return original data
    @Override
    public String toString() {
        return evaluatedValue != null ? evaluatedValue : getData();
    }


    @Override
    public int getOrder() {
        return 0; // ערך ברירת מחדל
    }

    @Override
    public void setData(String s) {
        line = s != null ? s.trim() : null; // הסרת רווחים מיותרים
    }

    @Override
    public String getData() {
        return line;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    @Override
    public void setOrder(int t) {
        // ניתן לממש במידת הצורך
    }
}