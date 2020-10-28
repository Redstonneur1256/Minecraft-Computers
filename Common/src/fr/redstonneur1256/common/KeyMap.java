package fr.redstonneur1256.common;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class KeyMap {

    private static Map<Character, KeyData> codes;

    static {
        codes = new HashMap<>();

        String letters = "abcdefghijklmnopqrstuvwxyz";
        char[] charArray = letters.toCharArray();
        for(int i = 0; i < charArray.length; i++) {
            char letter = charArray[i];

            codes.put(Character.toLowerCase(letter), new KeyData(KeyEvent.VK_A + i, false));
            codes.put(Character.toUpperCase(letter), new KeyData(KeyEvent.VK_A + i, true));
        }
        for(int i = 0; i <= 9; i++) {
            codes.put(String.valueOf(i).charAt(0), new KeyData(KeyEvent.VK_0 + i, true));
        }

        codes.put('&', new KeyData(KeyEvent.VK_1, false));
        codes.put('~', new KeyData(KeyEvent.VK_2, false));
        codes.put('"', new KeyData(KeyEvent.VK_3, false));
        codes.put('\'', new KeyData(KeyEvent.VK_4, false));
        codes.put('(', new KeyData(KeyEvent.VK_5, false));
        //codes.put('-', new KeyData(KeyEvent.V_6, true));
        codes.put('è', new KeyData(KeyEvent.VK_7, false));
        codes.put('_', new KeyData(KeyEvent.VK_8, false));
        codes.put('ç', new KeyData(KeyEvent.VK_9, false));
        codes.put('à', new KeyData(KeyEvent.VK_0, false));
        codes.put(')', new KeyData(KeyEvent.VK_RIGHT_PARENTHESIS, false));
        codes.put('°', new KeyData(KeyEvent.VK_RIGHT_PARENTHESIS, true));

        codes.put('*', new KeyData(KeyEvent.VK_MULTIPLY, false));
        codes.put('-', new KeyData(KeyEvent.VK_MINUS, false));
        codes.put('+', new KeyData(KeyEvent.VK_PLUS, false));

        codes.put(',', new KeyData(KeyEvent.VK_COMMA, false));
        codes.put('?', new KeyData(KeyEvent.VK_COMMA, true));

        codes.put(';', new KeyData(KeyEvent.VK_PERIOD, false));
        codes.put('.', new KeyData(KeyEvent.VK_PERIOD, true));

        codes.put(':', new KeyData(KeyEvent.VK_SLASH, false));
        codes.put('/', new KeyData(KeyEvent.VK_SLASH, true));

        codes.put('!', new KeyData(KeyEvent.VK_EXCLAMATION_MARK, false));
        codes.put('§', new KeyData(KeyEvent.VK_EXCLAMATION_MARK, true));

        codes.put(' ', new KeyData(KeyEvent.VK_SPACE, false));

    }

    public static KeyData getCode(char c) {
        return codes.getOrDefault(c, null);
    }

    public static class KeyData {

        public final int code;
        public final boolean shift;
        public KeyData(int code, boolean shift) {
            this.code = code;
            this.shift = shift;
        }

    }

}
