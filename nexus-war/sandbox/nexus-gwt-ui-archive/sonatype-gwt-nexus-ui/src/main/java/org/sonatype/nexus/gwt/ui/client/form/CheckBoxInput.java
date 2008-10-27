package org.sonatype.nexus.gwt.ui.client.form;

import com.google.gwt.user.client.ui.CheckBox;

/**
 * 
 *
 * @author barath
 */
public class CheckBoxInput implements FormInput {

    private CheckBox widget;
    
    public CheckBoxInput(CheckBox widget) {
        this.widget = widget;
    }
    
    public Object getValue() {
        return Boolean.valueOf(widget.isChecked());
    }

    public void setValue(Object value) {
        if (value != null) {
            boolean b = false;
            if (value instanceof String ) {
                b = Boolean.valueOf((String) value).booleanValue();
            } else if (value instanceof Boolean) {
                b = ((Boolean) value).booleanValue();
            }
            widget.setChecked(b);
        } else {
            widget.setChecked(false);
        }
    }

    public void reset() {
        widget.setChecked(false);
    }

}
