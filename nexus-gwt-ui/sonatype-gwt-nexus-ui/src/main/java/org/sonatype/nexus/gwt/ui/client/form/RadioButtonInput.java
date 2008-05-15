package org.sonatype.nexus.gwt.ui.client.form;

import com.google.gwt.user.client.ui.RadioButton;

/**
 * 
 *
 * @author barath
 */
public class RadioButtonInput implements FormInput {
    
    private RadioButton[] widgets;
    
    public RadioButtonInput(RadioButton[] widgets) {
        this.widgets = widgets;
    }

    public Object getValue() {
        for (int i = 0; i < widgets.length; i++) {
            if (widgets[i].isChecked()) {
                return widgets[i].getName();
            }
        }
        return null;
    }

    public void setValue(Object value) {
        String name = null;
        if (value != null) {
            name = value.toString();
        }
        for (int i = 0; i < widgets.length; i++) {
            widgets[i].setChecked(widgets[i].getName().equals(name));
        }
    }

    public void reset() {
        widgets[0].setChecked(true);
        for (int i = 1; i < widgets.length; i++) {
            widgets[i].setChecked(false);
        }
    }

}
