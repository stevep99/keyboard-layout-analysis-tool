package io.github.colemakmods.web;

import io.github.colemakmods.keyboard.Key;
import io.github.colemakmods.keyboard.KeyboardLayout;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Document;

/**
 * Created by steve on 27/04/15.
 */
public class HTMLKeyboardRenderer {

    private KeyboardLayout keyboardLayout;

    private final static int STD_KEY_WIDTH = 24; //standard key width in pixels

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

    public HTMLKeyboardRenderer(KeyboardLayout keyboardLayout) {
        this.keyboardLayout = keyboardLayout;
    }

    public HTMLElement generate(Document document) {
        HTMLElement divElt = (HTMLElement) document.createElement("div");
        for (int row = 0; row < keyboardLayout.getRows(); ++row) {
            for (int col = 0; col < keyboardLayout.getCols(); ++col) {
                int gap = determineGap(row, col);
                if (gap > 0) {
                    HTMLElement keyGapSpanElt = generateKeyGapElt(document, gap);
                    divElt.appendChild(keyGapSpanElt);
                }
                Key key = keyboardLayout.lookupKey(row, col);
                if (key != null) {
                    HTMLElement keySpanElt = generateKeyElt(document, key);
                    divElt.appendChild(keySpanElt);
                } else {
                    HTMLElement keyGapSpanElt = generateKeyGapElt(document, STD_KEY_WIDTH);
                    divElt.appendChild(keyGapSpanElt);
                }
            }
            HTMLElement brElt = (HTMLElement) document.createElement("br");
            divElt.appendChild(brElt);
        }
        return divElt;
    }

    private HTMLElement generateKeyElt(Document document, Key key) {
        HTMLElement spanElt = (HTMLElement) document.createElement("span");
        spanElt.setAttribute("class", "key");
        String backgroundColor = KEY_COLOR_FINGERS[key.getFinger()];
        if (backgroundColor != null) {
            spanElt.setAttribute("style", "background-color:" + backgroundColor);
        } else {
            spanElt.setHidden(true);
        }
        spanElt.appendChild(document.createTextNode(String.valueOf(key.getName())));
        return spanElt;
    }

    private HTMLElement generateKeyGapElt(Document document, int gap) {
        HTMLElement spanElt = (HTMLElement) document.createElement("span");
        spanElt.setAttribute("class", "keygap");
        spanElt.setAttribute("style", "width:" + gap + "px");
        return spanElt;
    }

    private int determineGap(int row, int col) {
        KeyboardLayout.KeyboardType type = keyboardLayout.getKeyboardType();
        if (type == KeyboardLayout.KeyboardType.STD) {
            if (row == 0 && col == 0) {
                return STD_KEY_WIDTH / 2;
            } else if (row == 1 && col == 0) {
                return STD_KEY_WIDTH * 3/4;
            } else if (row == 2 && col == 0) {
                return STD_KEY_WIDTH * 5/4;
            }
        } else if (type == KeyboardLayout.KeyboardType.ANGLE) {
            if (row == 0 && col == 0) {
                return STD_KEY_WIDTH / 2;
            } else if (row == 1 && col == 0) {
                return STD_KEY_WIDTH * 3/4;
            } else if (row == 2 && col == 0) {
                return STD_KEY_WIDTH * 1/4;
            } else if (row == 2 && col == 5) {
                return STD_KEY_WIDTH;
            }
        } else if (type == KeyboardLayout.KeyboardType.MATRIX) {
            if (col == 0 || col == 5) {
                return STD_KEY_WIDTH/2;
            }
        }
        return 0;
    }


}
