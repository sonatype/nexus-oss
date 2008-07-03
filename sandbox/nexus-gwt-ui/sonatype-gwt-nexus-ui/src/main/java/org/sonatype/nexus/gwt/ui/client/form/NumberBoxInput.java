package org.sonatype.nexus.gwt.ui.client.form;

import com.google.gwt.user.client.ui.TextBox;

/**
 * 
 *
 * @author barath
 */
public class NumberBoxInput implements FormInput {

    private TextBox widget;
    
    private boolean integer;
    
    public NumberBoxInput(TextBox widget) {
        this(widget, true);
    }

    public NumberBoxInput(TextBox widget, boolean isInteger) {
        this.widget = widget;
        this.integer = isInteger;
    }

    public Object getValue() {
        Double n = null;
        if (widget.getText() != null && !"".equals(widget.getText().trim())) {
            n = Double.valueOf(widget.getText());
        }
        return n;
    }
    
    public void setValue(Object value) {
        if (value != null) {
            String s = String.valueOf(value);
            if (integer) {
                int pos = s.indexOf('.');
                if (pos > -1) {
                    s = s.substring(0, pos);
                }
            }
            widget.setText(s);
        } else {
            widget.setText("");
        }
    }

    public void reset() {
        widget.setText("");
    }

}
