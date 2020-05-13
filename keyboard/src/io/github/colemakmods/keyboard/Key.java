package io.github.colemakmods.keyboard;

/**
 * Created by steve on 18/10/14.
 */
public class Key {

    private char[] chars;
    private int row;
    private int col;
    private int finger;
    private double effort;

    public Key(String chars, int row, int col) {
        if (chars.length() == 1) {
            char shifted = generateDefaultShifted(chars.charAt(0));
            if (shifted != '\0') {
                this.chars = new char[]{chars.charAt(0), shifted};
            } else {
                this.chars = new char[]{chars.charAt(0)};
            }
        } else {
            this.chars = chars.toCharArray();
        }
        this.row = row;
        this.col = col;
        this.finger = getDefaultFinger();
    }

    public char getName() {
        return Character.toUpperCase(chars[0]);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
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

    public char[] getChars() {
        return chars;
    }

    public boolean hasChar(char ch) {
        for (char c : chars) {
            if (c == Character.toUpperCase(ch)) return true;
        }
        return false;
    }

    private int getDefaultFinger() {
        //default finger values
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
