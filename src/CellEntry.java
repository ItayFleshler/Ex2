// Add your documentation below:

public class CellEntry  implements Index2D {
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
    public int XCell (String c){
        if (c.length() <= 1)
            return -1;
        if (c.charAt(0) >= 'A' && c.charAt(0) <= 'Z' && c.charAt(1) >= '0' && c.charAt(1) <= '9')
            return c.charAt(0) - 'A';
        return -1;
    }

    public int YCell (String c){
        if (c.length() <= 1)
            return -1;
        while (!c.isEmpty() && c.charAt(0) >= 'A' && c.charAt(0) <= 'Z'){
            c = c.substring(1);
        }
        try {
            int i = Integer.parseInt(c);
            if (i >= 0 && i < 100)
                return i;
            return -1;
        } catch (NumberFormatException _) {}
        return -1;
    }


    @Override
    public int getX() {return x;}

    @Override
    public int getY() {return y;}
}