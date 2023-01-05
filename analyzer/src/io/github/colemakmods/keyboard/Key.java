package io.github.colemakmods.keyboard;

/**
 * Created by steve on 18/10/14.
 */
public class Key {

    private int row;
    private int col;
    private String chars;
    private int finger;
    private double effort;

    public Key(int row, int col, String chars) {
        this.row = row;
        this.col = col;
        setChars(chars);
        this.finger = getDefaultFinger();
    }

    public Key duplicate() {
        Key k = new Key(this.row, this.col, this.chars);
        k.setFinger(this.finger);
        k.setEffort(this.effort);
        return k;
    }

    public char getName() {
        return Character.toUpperCase(chars.charAt(0));
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getHand() {
        return (finger <= 4) ? 0 : 1;
    }

    public int getFinger() {
        return finger;
    }

    public void setFinger(int finger) {
        this.finger = finger;
    }

    public double getEffort() {
        return effort;
    }

    public void setEffort(double effort) {
        this.effort = effort;
    }

    public String getChars() {
        return chars;
    }

    public char getPrimaryChar() {
        return chars.charAt(0);
    }

    public boolean hasChar(char ch) {
        for (char c : chars.toCharArray()) {
            if (c == Character.toUpperCase(ch))
                return true;
        }
        return false;
    }

    public void setChars(String chars) {
        if (chars.length() == 1) {
            char shifted = generateDefaultShifted(chars.charAt(0));
            if (shifted != '\0') {
                this.chars = String.valueOf(chars.charAt(0)) + shifted;
            } else {
                this.chars = chars;
            }
        } else {
            this.chars = chars;
        }
    }

    private int getDefaultFinger() {
        // default finger values
        if (col <= 3) {
            return col;
        } else if (col == 4) {
            return 3;
        } else if (col == 5) {
            return 6;
        } else if (col <= 9) {
            return col;
        } else {
            return 9;
        }
    }

    private static char generateDefaultShifted(char ch) {
        if (Character.isAlphabetic(ch)) {
            if (Character.isLowerCase(ch)) {
                return Character.toUpperCase(ch);
            } else {
                return Character.toLowerCase(ch);
            }
        }
        switch (ch) {
            case '1':
                return '!';
            case '2':
                return '@';
            case '3':
                return '#';
            case '4':
                return '$';
            case '5':
                return '%';
            case '6':
                return '^';
            case '7':
                return '&';
            case '8':
                return '*';
            case '9':
                return '(';
            case '0':
                return ')';
            case '\'':
                return '\"';
            case '-':
                return '_';
            case '=':
                return '+';
            case '[':
                return '{';
            case ']':
                return '}';
            case ';':
                return ':';
            case ',':
                return '<';
            case '.':
                return '>';
            case '/':
                return '?';
            default:
                return '\0';
        }
    }

}
