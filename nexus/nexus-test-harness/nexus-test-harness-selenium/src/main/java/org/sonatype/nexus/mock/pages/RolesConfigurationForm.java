package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.components.TwinPanel;

import com.thoughtworks.selenium.Selenium;

public class RolesConfigurationForm
    extends Component
{

    private TextField roleId;

    private TextField name;

    private TextField description;

    private TwinPanel privileges;

    private Button saveButton;

    private Button cancelButton;

    public RolesConfigurationForm( Selenium selenium, String expression )
    {
        super( selenium, expression );

        roleId = new TextField( selenium, expression + ".find('name', 'id')[0]" );
        name = new TextField( selenium, expression + ".find('name', 'name')[0]" );
        description = new TextField( selenium, expression + ".find('name', 'description')[0]" );
        privileges = new TwinPanel( selenium, expression + ".find('name', 'privileges')[0]" );

        saveButton = new Button( selenium, expression + ".buttons[0]" );
        cancelButton = new Button( selenium, expression + ".buttons[1]" );
    }

    public final TextField getRoleId()
    {
        return roleId;
    }

    public final TextField getName()
    {
        return name;
    }

    public final TextField getDescription()
    {
        return description;
    }

    public final TwinPanel getPrivileges()
    {
        return privileges;
    }

    public RolesConfigurationForm save()
    {
        saveButton.click();

        return this;
    }

    public RolesConfigurationForm cancel()
    {
        cancelButton.click();

        return this;
    }

    public RolesConfigurationForm populate( String roleId, String name, String... privs )
    {
        this.roleId.type( roleId );
        this.name.type( name );
        for ( String priv : privs )
        {
            this.privileges.add( priv );
        }

        return this;
    }

}
