package org.sonatype.nexus.gwt.ui.client.form;

import com.google.gwt.user.client.ui.TextBoxBase;

/**
 * 
 *
 * @author barath
 */
public class TextBoxInput implements FormInput {

    private TextBoxBase widget;
    
    public TextBoxInput(TextBoxBase widget) {
        this.widget = widget;
    }

    public Object getValue() {
        return widget.getText();
    }
    
    public void setValue(Object value) {
        if (value != null) {
            widget.setText(value.toString());
        } else {
            widget.setText("");
        }
    }

    public void reset() {
        widget.setText("");
    }

}
