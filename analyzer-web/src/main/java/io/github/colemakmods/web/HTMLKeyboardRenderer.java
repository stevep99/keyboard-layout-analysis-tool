package io.github.colemakmods.web;

import io.github.colemakmods.keyboard.Hand;
import io.github.colemakmods.keyboard.Key;
import io.github.colemakmods.keyboard.KeyboardLayout;
import io.github.colemakmods.web.teavm.JSFormatter;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Document;

import java.util.HashMap;
/**
 * Created by steve on 27/04/15.
 */
public class HTMLKeyboardRenderer {

    private KeyboardLayout keyboardLayout;
    private HashMap<Key, Double> keyFreq;

    //standard key width and height in pixels
    private final static int STD_KEY_WIDTH = 36;
    private final static int STD_KEY_HEIGHT = 28;

    private static final String[] KEY_COLOR_FINGERS = {
            "#80c4c4",
            "#ca80ca",
            "#80b780",
            "#7a93c0",
            null,
            null,
            "#8080ca",
            "#80b780",
            "#ca80ca",
            "#80c4c4",
    };

    public HTMLKeyboardRenderer(KeyboardLayout keyboardLayout, HashMap<Key, Double> keyFreq) {
        this.keyboardLayout = keyboardLayout;
        this.keyFreq = keyFreq;
    }

    public HTMLElement generate(Document document, boolean isHeatmap) {
        HTMLElement divElt = (HTMLElement) document.createElement("div");
        divElt.setAttribute("style", "position:relative;");
        for (int row = 0; row < keyboardLayout.getRows(); ++row) {
            for (int col = 0; col < keyboardLayout.getCols(); ++col) {
                Key key = keyboardLayout.lookupKey(row, col);
                if (key != null) {
                    HTMLElement keySpanElt = generateKeyElt(document, key, isHeatmap);
                    divElt.appendChild(keySpanElt);
                }
            }
            HTMLElement brElt = (HTMLElement) document.createElement("br");
            divElt.appendChild(brElt);
        }
        return divElt;
    }

    private HTMLElement generateKeyElt(Document document, Key key, boolean isHeatmap) {
        HTMLElement spanElt = (HTMLElement) document.createElement("span");
        spanElt.setAttribute("class", "key");
        String backgroundColor = null;
        if (isHeatmap) {
            Double keyFreqValue = keyFreq.get(key);
            if (keyFreqValue != null) {
                int redComponent = Math.min(255, 160 + (int) (keyFreqValue*1000));
                int blueGreenComponent = Math.max(0, 160 - (int) (keyFreqValue*1500));
                backgroundColor =  "rgb(" + redComponent + "," + blueGreenComponent + "," + blueGreenComponent + ")";
            }

        } else {
            backgroundColor = KEY_COLOR_FINGERS[key.getFinger()];
        }

        //calculate key position
        int rowid = 5 - keyboardLayout.getRows() + key.getRow();
        Position pos = determinePosition(rowid, key.getCol(), keyboardLayout.getRows(), key.getHand());
        StringBuffer styleAttr = new StringBuffer("position:absolute;");
        styleAttr.append("left:" + pos.x + "px;top:" + pos.y + "px;");
        if (backgroundColor != null) {
            styleAttr.append("background-color:" + backgroundColor + ";");
        } else {
            spanElt.setHidden(true);
        }
        spanElt.setAttribute("style", styleAttr.toString());

        if (isHeatmap) {
            Double keyFreqValue = keyFreq.get(key);
            if (keyFreqValue != null) {
                spanElt.setAttribute("title", "Key " + key.getChars() + " Usage: "
                        + JSFormatter.toFixed(keyFreqValue * 100, 2) + "%");
            }
        } else {
            spanElt.setAttribute("title", "Key " + key.getChars() + "  Effort: "
                + JSFormatter.toFixed(key.getEffort(), 1));

        }
        spanElt.appendChild(document.createTextNode(String.valueOf(key.getName())));
        return spanElt;
    }

    private Position determinePosition(int rowid, int col, int rowCount, Hand hand) {
        KeyboardLayout.KeyboardType type = keyboardLayout.getKeyboardType();
        int x = col * STD_KEY_WIDTH + 10;
        int y = (rowCount > 3) ? rowid * STD_KEY_HEIGHT - 24 : rowid * STD_KEY_HEIGHT - 36;
        if (type == KeyboardLayout.KeyboardType.STD) {
            if (rowid == 1) {
                //
            } else if (rowid == 2) {
                x += STD_KEY_WIDTH / 2;
            } else if (rowid == 3) {
                x += STD_KEY_WIDTH * 3/4;
            } else if (rowid == 4) {
                x += STD_KEY_WIDTH * 5/4;
            }
        } else if (type == KeyboardLayout.KeyboardType.ANGLE) {
            if (rowid == 1) {
                //
            } else if (rowid == 2) {
                x += STD_KEY_WIDTH / 2;
            } else if (rowid == 3) {
                x += STD_KEY_WIDTH * 3/4;
            } else if (rowid == 4) {
                x += (hand == Hand.LEFT) ? STD_KEY_WIDTH / 4 : STD_KEY_WIDTH * 5/4;;
            }
        } else if (type == KeyboardLayout.KeyboardType.MATRIX_SIMPLE) {
            if (hand == Hand.RIGHT) {
                x += STD_KEY_WIDTH / 2;
            }
        } else if (type == KeyboardLayout.KeyboardType.MATRIX_ERGODOX) {
            if (hand == Hand.RIGHT) {
                x += STD_KEY_WIDTH * 5 / 2;
            }
            if (rowid == 1 || rowid == 2) {
                if (col == 10) {
                    x -= STD_KEY_WIDTH * 5;
                } else if (col == 11) {
                    x -= STD_KEY_WIDTH * 7;
                } else if (col > 11) {
                    x -= STD_KEY_WIDTH * 2;
                }
            }
        }
        return new Position(x, y);
    }

    class Position {
        int x;
        int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

}
