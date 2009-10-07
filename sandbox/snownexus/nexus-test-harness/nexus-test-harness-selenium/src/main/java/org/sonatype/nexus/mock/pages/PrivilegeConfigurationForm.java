package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Combobox;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.components.Window;

import com.thoughtworks.selenium.Selenium;

public class PrivilegeConfigurationForm
    extends Component
{

    private Button cancelButton;

    private TextField description;

    private TextField method;

    private TextField name;

    private TextField repositoryGroupId;

    private TextField repositoryId;

    private Combobox repositoryOrGroup;

    private Combobox repoTarget;

    private Button saveButton;

    private Combobox type;

    public PrivilegeConfigurationForm( Selenium selenium, String expression )
    {
        super( selenium, expression );

        name = new TextField( selenium, expression + ".find('name', 'name')[0]" );
        description = new TextField( selenium, expression + ".find('name', 'description')[0]" );
        type = new Combobox( selenium, expression + ".find('name', 'type')[0]" );
        repositoryOrGroup = new Combobox( selenium, expression + ".find('name', 'repositoryOrGroup')[0]" );
        repoTarget = new Combobox( selenium, expression + ".find('name', 'repositoryTargetId')[0]" );

        saveButton = new Button( selenium, expression + ".buttons[0]" );
        cancelButton = new Button( selenium, expression + ".buttons[1]" );

        repositoryId = new TextField( selenium, expression + ".find('name', 'repositoryId')[0]" );
        repositoryGroupId = new TextField( selenium, expression + ".find('name', 'repositoryGroupId')[0]" );
        method = new TextField( selenium, expression + ".find('name', 'method')[0]" );
    }

    public void cancel()
    {
        cancelButton.click();
    }

    public final Button getCancelButton()
    {
        return cancelButton;
    }

    public final TextField getDescription()
    {
        return description;
    }

    public final TextField getMethod()
    {
        return method;
    }

    public final TextField getName()
    {
        return name;
    }

    public final TextField getRepositoryGroupId()
    {
        return repositoryGroupId;
    }

    public final TextField getRepositoryId()
    {
        return repositoryId;
    }

    public final Combobox getRepositoryOrGroup()
    {
        return repositoryOrGroup;
    }

    public final Combobox getRepoTarget()
    {
        return repoTarget;
    }

    public final Button getSaveButton()
    {
        return saveButton;
    }

    public final Combobox getType()
    {
        return type;
    }

    public PrivilegeConfigurationForm populate( String name, String description, int target )
    {
        this.name.type( name );
        this.description.type( description );
        if ( target != -1 )
        {
            this.repoTarget.select( target );
        }

        return this;
    }

    public PrivilegeConfigurationForm populate( String name, String description, String repoId, String targetId )
    {
        populate( name, description, -1 );
        this.repoTarget.setValue( targetId );
        this.repositoryOrGroup.setValue( String.valueOf( repoId ) );

        return this;
    }

    public PrivilegeConfigurationForm save()
    {
        saveButton.click();

        new Window( selenium ).waitFor();

        return this;
    }

}
