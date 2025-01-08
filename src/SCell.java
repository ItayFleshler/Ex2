public class SCell implements Cell {
    private String line;
    private int type;
    private String evaluatedValue;
    private Ex2Sheet sheet;
    private String cellName;  // הוספת שדה לשם התא

    public SCell(String s, Ex2Sheet sheet, String cellName) {
        this.sheet = sheet;
        this.cellName = cellName;  // שמירת שם התא
        setData(s);
        setType(Ex2Utils.TEXT);
    }

    @Override
    public void setData(String s) {
        line = s;
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
    public int getOrder() {
        return 0;
    }

    @Override
    public void setOrder(int t) {
    }

    public void setEvaluatedValue(String value) {
        this.evaluatedValue = value;
    }

    public String getEvaluatedValue() {
        return evaluatedValue;
    }

    public String getCellName() {
        return cellName;
    }

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

    public boolean isForm() {
        String data = getData();
        if (data == null || !data.startsWith("=")) {
            return false;
        }
        return true;
    }

    private boolean isValidOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private boolean isBalancedParentheses(String str) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == '(') count++;
            if (c == ')') count--;
            if (count < 0) return false;
        }
        return count == 0;
    }

    private int findMainOperator(String form) {
        int parenthesesCount = 0;
        int lastOperator = -1;

        for (int i = 0; i < form.length(); i++) {
            char c = form.charAt(i);
            if (c == '(') {
                parenthesesCount++;
            } else if (c == ')') {
                parenthesesCount--;
            } else if (parenthesesCount == 0 && isValidOperator(c)) {
                lastOperator = i;
                if (c == '+' || c == '-') {
                    return i;
                }
            }
        }
        return lastOperator;
    }

    private Double performOperation(Double left, Double right, char operator) {
        switch (operator) {
            case '+': return left + right;
            case '-': return left - right;
            case '*': return left * right;
            case '/': return right != 0 ? left / right : null;
            default: return null;
        }
    }

    public Double computeForm(String form) {
        if (form == null || form.isEmpty()) {
            return null;
        }

        if (form.startsWith("=")) {
            form = form.substring(1).trim();
        }

        form = form.replaceAll("\\s+", "");

        try {
            return Double.parseDouble(form);
        } catch (NumberFormatException ignored) {
        }

        while (form.startsWith("(") && form.endsWith(")")) {
            String inner = form.substring(1, form.length() - 1);
            if (isBalancedParentheses(inner)) {
                form = inner;
                try {
                    return Double.parseDouble(form);
                } catch (NumberFormatException ignored) {
                }
            } else {
                break;
            }
        }

        if (form.matches("[A-Z][0-9]+") && sheet != null) {
            int col = form.charAt(0) - 'A';
            int row = Integer.parseInt(form.substring(1));
            String cellValue = sheet.value(col, row);
            try {
                return Double.parseDouble(cellValue);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        int operatorIndex = -1;
        int parenthesesCount = 0;
        boolean foundOperator = false;

        for (int i = form.length() - 1; i >= 0; i--) {
            char c = form.charAt(i);
            if (c == ')') parenthesesCount++;
            else if (c == '(') parenthesesCount--;
            else if (parenthesesCount == 0 && (c == '+' || c == '-')) {
                if (i > 0 && isValidOperator(form.charAt(i - 1))) {
                    continue;  // מדלג על אופרטורים כפולים
                }
                operatorIndex = i;
                foundOperator = true;
                break;
            } else if (parenthesesCount == 0 && !foundOperator && (c == '*' || c == '/')) {
                operatorIndex = i;
            }
        }

        if (operatorIndex == -1) {
            try {
                return Double.parseDouble(form);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        String leftPart = form.substring(0, operatorIndex).trim();
        char operator = form.charAt(operatorIndex);
        String rightPart = form.substring(operatorIndex + 1).trim();

        if (leftPart.isEmpty()) {
            if (operator == '-') {
                try {
                    return -Double.parseDouble(rightPart);
                } catch (NumberFormatException e) {
                    Double rightValue = computeForm(rightPart);
                    return rightValue != null ? -rightValue : null;
                }
            }
            return computeForm(rightPart);
        }

        Double leftValue = computeForm(leftPart);
        Double rightValue = computeForm(rightPart);

        if (leftValue == null || rightValue == null) {
            return null;
        }

        return performOperation(leftValue, rightValue, operator);
    }

    @Override
    public String toString() {
        if (cellName != null && !cellName.isEmpty()) {
            return cellName;  // מחזיר את שם התא
        }

        if (evaluatedValue != null) {
            try {
                double val = Double.parseDouble(evaluatedValue);
                if (Math.abs(val) >= 1e6 || (Math.abs(val) < 1e-6 && val != 0)) {
                    return String.format("%.1e", val);
                }
                return String.format("%.1f", val);
            } catch (NumberFormatException e) {
                return evaluatedValue;
            }
        }

        String data = getData();
        if (data == null || data.isEmpty()) {
            return "";
        }

        if (isForm()) {
            Double result = computeForm(data);
            if (result != null) {
                if (Math.abs(result) >= 1e6 || (Math.abs(result) < 1e-6 && result != 0)) {
                    return String.format("%.1e", result);
                }
                return String.format("%.1f", result);
            }
            return "Error";
        }

        if (isNumber()) {
            try {
                double val = Double.parseDouble(data);
                if (Math.abs(val) >= 1e6 || (Math.abs(val) < 1e-6 && val != 0)) {
                    return String.format("%.1e", val);
                }
                return String.format("%.1f", val);
            } catch (NumberFormatException e) {
                return data;
            }
        }

        return data;
    }
}