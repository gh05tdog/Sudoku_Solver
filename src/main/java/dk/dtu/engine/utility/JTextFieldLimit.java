package dk.dtu.engine.utility;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class JTextFieldLimit extends PlainDocument {
    private final int limit;
    private final boolean onlyNumbers;

    public JTextFieldLimit(int limit, boolean onlyNumbers) {
        super();
        this.limit = limit;
        this.onlyNumbers = onlyNumbers;
    }

    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null) {
            return;
        }

        if ((getLength() + str.length()) <= limit) {
            if (onlyNumbers) {
                for (int i = 0; i < str.length(); i++) {
                    if (!Character.isDigit(str.charAt(i)) || str.charAt(i) == '0') {
                        return; // Reject the entire insertion if it's not a digit or it's zero
                    }
                }
            }
            super.insertString(offset, str, attr);
        }
    }
}