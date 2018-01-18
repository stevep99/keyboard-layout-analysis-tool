package io.github.colemakmods.chars;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 18/04/15.
 *
 * This class is needed when used by TeaVM (conversion to JavaScript), as String.split(...) is not supported.
 */
public class StringSplitter {

    public static List<String> split(String str, char sep) {
        List<String> tokens = new ArrayList<String>();
        StringBuffer currentToken = new StringBuffer();
        for (int i=0; i<str.length(); ++i) {
            if (str.charAt(i) != sep) {
                currentToken.append(str.charAt(i));
            }
            if (str.charAt(i) == sep || i == str.length()-1) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            }
        }
        return tokens;
    }
}
