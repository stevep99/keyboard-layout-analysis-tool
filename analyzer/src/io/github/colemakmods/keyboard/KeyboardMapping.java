package io.github.colemakmods.keyboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import io.github.colemakmods.chars.StringSplitter;

public class KeyboardMapping {

    public static boolean parse(KeyboardLayout keyboardLayout, File file) {
        try {
            parse(keyboardLayout, new FileReader(file));
            return true;
        } catch (Exception ex) {
            System.err.println("Unable to load layout file");
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean parse(KeyboardLayout keyboardLayout, String input) {
        try {
            parse(keyboardLayout, new StringReader(input));
            return true;
        } catch (Exception ex) {
            System.err.println("Unable to read layout data");
            ex.printStackTrace();
            return false;
        }
    }

    private static void parse(KeyboardLayout keyboardLayout, Reader in) throws IOException {
        BufferedReader br = new BufferedReader(in);
        try {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                int commentpos = line.indexOf('#');
                if (commentpos >= 0) {
                    line = line.substring(0, commentpos);
                }
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                List<String> charList = StringSplitter.split(line, ' ');
                for (int col = 0; col < charList.size(); col++) {
                    String chars = charList.get(col);
                    keyboardLayout.addKey(row, col, chars);
                }
                ++row;
            }
        } finally {
            br.close();
        }
    }


}
