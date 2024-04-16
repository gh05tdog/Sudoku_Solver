package dk.dtu.engine.utility;

import javax.swing.text.*;

public class NumberDocumentFilter extends DocumentFilter {
    @Override
    public void insertString(DocumentFilter.FilterBypass fb, int offset, String string,
                             AttributeSet attr) throws BadLocationException {
        if (string.isEmpty() || string.matches("[1-9]")) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text,
                        AttributeSet attrs) throws BadLocationException {
        if (text.isEmpty() || text.matches("[1-9]")) {
            // Ensure only one digit can be entered.
            if (fb.getDocument().getLength() - length + text.length() <= 1) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
}
