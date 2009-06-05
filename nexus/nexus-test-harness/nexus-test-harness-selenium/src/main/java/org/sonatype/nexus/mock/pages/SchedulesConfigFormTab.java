package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Checkbox;
import org.sonatype.nexus.mock.components.Combobox;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.TextField;

import com.thoughtworks.selenium.Selenium;

public class SchedulesConfigFormTab
    extends Component
{

    private Button saveButton;

    private TextField name;

    private Combobox taskType;

    private Checkbox enabled;

    private Combobox recurrence;

    private Button cancelButton;

    public SchedulesConfigFormTab( Selenium selenium )
    {
        super( selenium, "window.Ext.getCmp('schedule-config-forms')" );

        enabled = new Checkbox( selenium, getExpression() + ".find('name', 'enabled')[0]" );
        name = new TextField( selenium, getExpression() + ".find('name', 'name')[0]" );
        taskType = new Combobox( selenium, getExpression() + ".find('name', 'typeId')[0]" );
        recurrence = new Combobox( selenium, getExpression() + ".find('name', 'schedule')[0]" );

        saveButton = new Button( selenium, "window.Ext.getCmp('savebutton')" );
        cancelButton = new Button( selenium, "window.Ext.getCmp('cancelbutton')" );
    }

    public SchedulesConfigFormTab populate( boolean enable, String name, String taskType, String recurrence )
    {
        this.enabled.check( enable );
        this.name.type( name );
        this.taskType.setValue( taskType );
        this.recurrence.setValue( recurrence );

        return this;
    }

    public SchedulesConfigFormTab save()
    {
        saveButton.click();

        return this;
    }

    public Button getSaveButton()
    {
        return saveButton;
    }

    public TextField getName()
    {
        return name;
    }

    public Combobox getTaskType()
    {
        return taskType;
    }

    public Checkbox getEnabled()
    {
        return enabled;
    }

    public Combobox getRecurrence()
    {
        return recurrence;
    }

    public Button getCancelButton()
    {
        return cancelButton;
    }

}
