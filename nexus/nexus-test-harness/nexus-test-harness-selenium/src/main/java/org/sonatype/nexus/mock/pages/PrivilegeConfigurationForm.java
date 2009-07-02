package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Combobox;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.TextField;

import com.thoughtworks.selenium.Selenium;

public class PrivilegeConfigurationForm
    extends Component
{

    private TextField name;

    private TextField description;

    private Combobox type;

    private Combobox repository;

    private Combobox repoTarget;

    private Button saveButton;

    private Button cancelButton;

    public PrivilegeConfigurationForm( Selenium selenium, String expression )
    {
        super( selenium, expression );

        name = new TextField( selenium, expression + ".find('name', 'name')[0]" );
        description = new TextField( selenium, expression + ".find('name', 'description')[0]" );
        type = new Combobox( selenium, expression + ".find('name', 'type')[0]" );
        repository = new Combobox( selenium, expression + ".find('name', 'repositoryOrGroup')[0]" );
        repoTarget = new Combobox( selenium, expression + ".find('name', 'repositoryTargetId')[0]" );

        saveButton = new Button( selenium, expression + ".buttons[0]" );
        cancelButton = new Button( selenium, expression + ".buttons[1]" );
    }

    public final TextField getName()
    {
        return name;
    }

    public final TextField getDescription()
    {
        return description;
    }

    public final Combobox getType()
    {
        return type;
    }

    public final Combobox getRepository()
    {
        return repository;
    }

    public final Combobox getRepoTarget()
    {
        return repoTarget;
    }

    public final Button getSaveButton()
    {
        return saveButton;
    }

    public final Button getCancelButton()
    {
        return cancelButton;
    }

    public void cancel()
    {
        cancelButton.click();
    }

    public void save()
    {
        saveButton.click();

        waitEvalTrue( "window.Ext.Msg.isVisible() == false" );
    }

    public PrivilegeConfigurationForm populate( String name, String description, int target )
    {
        this.name.type( name );
        this.description.type( description );
        this.repoTarget.select( target );

        return this;
    }

}
