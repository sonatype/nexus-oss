package org.sonatype.nexus.gwt.ui.client.form;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 *
 * @author barath
 */
public class RadioButtonGroup extends Composite implements ClickListener {
    
    private RadioButton[] buttons;
    
    public RadioButtonGroup(RadioButton[] buttons) {
        this(buttons, new FlowPanel());
    }

    public RadioButtonGroup(RadioButton[] buttons, HasWidgets container) {
        this.buttons = buttons;
        
        for (int i = 0; i < buttons.length; i++) {
            container.add(buttons[i]);
            buttons[i].addClickListener(this);
        }
        buttons[0].setChecked(true);
        initWidget((Widget) container);
    }

    public void onClick(Widget sender) {
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] != sender) {
                buttons[i].setChecked(false);
            }
        }
    }

}
