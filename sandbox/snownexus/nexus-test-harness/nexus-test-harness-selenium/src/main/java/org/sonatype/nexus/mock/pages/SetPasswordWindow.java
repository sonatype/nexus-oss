package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.components.Window;

import com.thoughtworks.selenium.Selenium;

public class SetPasswordWindow
    extends Window
{
    private Button cancelButton;

    private TextField confirmPassword;

    private TextField newPassword;

    private Button okButton;

    public SetPasswordWindow( Selenium selenium )
    {
        super( selenium, "window.Ext.getCmp('set-password-window')" );

        newPassword = new TextField( this, ".find('name', 'newPassword')[0]" );
        confirmPassword = new TextField( this, ".find('name', 'confirmPassword')[0]" );

        okButton = new Button( selenium, expression + ".items.items[0].buttons[0]" );
        cancelButton = new Button( selenium, expression + ".items.items[0].buttons[1]" );
    }

    public final Button getCancelButton()
    {
        return cancelButton;
    }

    public final TextField getConfirmPassword()
    {
        return confirmPassword;
    }

    public final TextField getNewPassword()
    {
        return newPassword;
    }

    public final Button getOkButton()
    {
        return okButton;
    }

    public MessageBox ok()
    {
        okButton.click();

        return new MessageBox(selenium);
    }

    public SetPasswordWindow populate( String newUserPw )
    {
        newPassword.type( newUserPw );
        confirmPassword.type( newUserPw );

        return this;
    }

    public void cancel()
    {
        cancelButton.click();
    }

}
