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

        String formula = data.substring(1); // מתעלמים מה-=
        if (formula.isEmpty()) {
            return false;
        }

        // בדיקת איזון סוגריים
        if (areParenthesesBalanced(formula)) {
            return false;
        }

        // מוודאים שכל חלק בנוסחה הוא או מספר, או הפניה לתא, או אופרטור חוקי
        String[] formParts = formula.split("(?<=[-+*/()])|(?=[-+*/()])");
        for (String part : formParts) {
            part = part.trim();
            if (!part.isEmpty() &&
                    !part.matches("[A-Z][0-9]+") && // שינוי כאן - מאפשר מספרים גדולים מ-9
                    !part.matches("-?\\d+(\\.\\d+)?") &&
                    !part.matches("[-+*/()]")) {
                return false;
            }
        }

        return true;
    }

    public Double computeForm(String form) {
        if (form == null) {
            return null;
        }

        form = form.replaceAll("\\s", ""); // הסרת רווחים
        if (form.isEmpty()) {
            return null;
        }

        if (form.startsWith("=")) {
            form = form.substring(1);
        }

        // בדיקת סוגריים
        if (areParenthesesBalanced(form)) {
            throw new IllegalArgumentException("Unbalanced parentheses in formula: " + form);
        }

        try {
            // ניסיון לפרש כמספר ישיר
            return Double.parseDouble(form);
        } catch (NumberFormatException ignored) {
            // אם זה לא מספר ישיר, נמשיך לחישוב הנוסחה
        }

        // אם זו הפניה ישירה לתא (כמו A1)
        if (form.matches("[A-Z][0-9]")) {
            return 0.0; // ערך ברירת מחדל לתא
        }

        // טיפול בסוגריים
        if (form.startsWith("(") && form.endsWith(")")) {
            return computeForm(form.substring(1, form.length() - 1));
        }

        // חיפוש האופרטור הראשי
        int operatorIndex = findMainOperator(form);
        if (operatorIndex == -1) {
            throw new IllegalArgumentException("No valid operator found in formula: " + form);
        }

        String leftOperand = form.substring(0, operatorIndex).trim();
        String operator = form.substring(operatorIndex, operatorIndex + 1);
        String rightOperand = form.substring(operatorIndex + 1).trim();

        // וידוא שיש אופרנדים
        if (leftOperand.isEmpty() || rightOperand.isEmpty()) {
            throw new IllegalArgumentException("Missing operand in formula: " + form);
        }

        Double leftValue = computeForm(leftOperand);
        Double rightValue = computeForm(rightOperand);

        // בדיקה שהערכים חוקיים
        if (leftValue == null || rightValue == null) {
            return null;
        }

        try {
            switch (operator) {
                case "+":
                    return leftValue + rightValue;
                case "-":
                    return leftValue - rightValue;
                case "*":
                    return leftValue * rightValue;
                case "/":
                    if (rightValue == 0) {
                        throw new ArithmeticException("Division by zero");
                    }
                    return leftValue / rightValue;
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
        } catch (Exception e) {
            System.err.println("Error computing formula: " + form + " - " + e.getMessage());
            return null;
        }
    }

    private boolean areParenthesesBalanced(String str) {
        int balance = 0;
        for (char ch : str.toCharArray()) {
            if (ch == '(') {
                balance++;
            } else if (ch == ')') {
                balance--;
                if (balance < 0) {
                    return true; // סוגר ימין ללא סוגר שמאל תואם
                }
            }
        }
        return balance != 0; // אם נשאר אי-איזון, נוסחה לא חוקית
    }


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

    // חידוש: חיפוש אופרטור ראשי מחוץ לסוגריים בלבד
    private int findMainOperator(String form) {
        int openParentheses = 0;
        int lastAddOrSubtract = -1;

        // עובר על המחרוזת פעמיים - פעם ראשונה מחפש + ו-
        for (int i = 0; i < form.length(); i++) {
            char ch = form.charAt(i);

            if (ch == '(') {
                openParentheses++;
            } else if (ch == ')') {
                openParentheses--;
            }

            if (openParentheses == 0 && (ch == '+' || ch == '-')) {
                lastAddOrSubtract = i;
            }
        }

        // אם מצאנו + או -, נחזיר אותו
        if (lastAddOrSubtract != -1) {
            return lastAddOrSubtract;
        }

        // אם לא מצאנו + או -, נחפש * או /
        openParentheses = 0;
        for (int i = 0; i < form.length(); i++) {
            char ch = form.charAt(i);

            if (ch == '(') {
                openParentheses++;
            } else if (ch == ')') {
                openParentheses--;
            }

            if (openParentheses == 0 && (ch == '*' || ch == '/')) {
                return i;
            }
        }

        return -1;
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