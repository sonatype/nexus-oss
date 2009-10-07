package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Combobox;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.components.TwinPanel;

import com.thoughtworks.selenium.Selenium;

public class UsersConfigurationForm
    extends Component
{

    private TextField userId;

    private TextField name;

    private TextField email;

    private Combobox status;

    private TextField password;

    private TextField passwordConfirm;

    private TwinPanel roles;

    private Button saveButton;

    private Button cancelButton;

    public UsersConfigurationForm( Selenium selenium, String expression )
    {
        super( selenium, expression );

        userId = new TextField( selenium, expression + ".find('name', 'userId')[0]" );
        name = new TextField( selenium, expression + ".find('name', 'name')[0]" );
        email = new TextField( selenium, expression + ".find('name', 'email')[0]" );
        status = new Combobox( selenium, expression + ".find('name', 'status')[0]" );
        password = new TextField( selenium, expression + ".find('name', 'password')[0]" );
        passwordConfirm = new TextField( selenium, expression + ".find('name', 'confirmPassword')[0]" );
        roles = new TwinPanel( selenium, expression + ".find('name', 'roles')[0]" );

        saveButton = new Button( selenium, expression + ".buttons[0]" );
        cancelButton = new Button( selenium, expression + ".buttons[1]" );
    }

    public UsersConfigurationForm populate( String userId, String name, String email, String status, String password,
                                            String... roles )
    {
        this.password.type( password );
        this.passwordConfirm.type( password );

        return populate( userId, name, email, status, roles );
    }

    public UsersConfigurationForm populate( String userId, String name, String email, String status, String[] roles )
    {
        this.userId.type( userId );
        this.name.type( name );
        this.email.type( email );
        this.status.setValue( status );
        for ( String role : roles )
        {
            this.roles.add( role );
        }

        return this;
    }

    public UsersConfigurationForm save()
    {
        this.saveButton.click();

        return this;
    }

    public final TextField getUserId()
    {
        return userId;
    }

    public final TextField getName()
    {
        return name;
    }

    public final TextField getEmail()
    {
        return email;
    }

    public final Combobox getStatus()
    {
        return status;
    }

    public final TextField getPassword()
    {
        return password;
    }

    public final TextField getPasswordConfirm()
    {
        return passwordConfirm;
    }

    public final TwinPanel getRoles()
    {
        return roles;
    }

    public final Button getSaveButton()
    {
        return saveButton;
    }

    public final Button getCancelButton()
    {
        return cancelButton;
    }

    public UsersConfigurationForm cancel()
    {
        cancelButton.click();

        return this;
    }

}
