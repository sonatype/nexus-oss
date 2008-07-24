package org.sonatype.nexus.gwt.ui.client.table;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClickListenerCollection;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SourcesClickEvents;
import com.google.gwt.user.client.ui.Widget;

public class ToolBarButton extends Composite implements ClickListener, SourcesClickEvents {

    private PushButton button = new PushButton();
    private Label label = new Label();

    private ClickListenerCollection clickListeners;

    public ToolBarButton(String normalURL, String disabledURL, String labelText) {
        this(normalURL, disabledURL, labelText, true);
    }

    public ToolBarButton(String normalURL, String disabledURL, String labelText, boolean showLabel) {
        // Set up the button
        Image normalImage = new Image(normalURL, 0, 0, 16, 16);
        Image disabledImage = new Image(disabledURL, 0, 0, 16, 16);
        button.getUpFace().setImage(normalImage);
        button.getDownFace().setImage(normalImage);
        button.getUpDisabledFace().setImage(disabledImage);
        button.getDownDisabledFace().setImage(disabledImage);
        button.setTitle(labelText);

        // Set up the label
        label.setText(labelText);

        // Hook up events.
        button.addClickListener(this);
        label.addClickListener(this);

        // Create the panel and add the widgets to it
        HorizontalPanel panel = new HorizontalPanel();
        panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        panel.add(button);
        if (showLabel) {
            panel.add(label);
        }
        initWidget(panel);

        // Set the style of the panel
        setStyleName("toolbar-button");
    }

    public void setEnabled(boolean enabled) {
        button.setEnabled(enabled);
        if (enabled) {
            label.removeStyleDependentName("disabled");
        } else {
            label.addStyleDependentName("disabled");
        }
    }

    public void addClickListener(ClickListener listener) {
        if (clickListeners == null) {
            clickListeners = new ClickListenerCollection();
        }
        clickListeners.add(listener);
    }

    public void removeClickListener(ClickListener listener) {
        if (clickListeners != null) {
            clickListeners.remove(listener);
        }
    }

    public void onClick(Widget sender) {
        if (clickListeners != null) {
            clickListeners.fireClick(this);
        }
    }

}
