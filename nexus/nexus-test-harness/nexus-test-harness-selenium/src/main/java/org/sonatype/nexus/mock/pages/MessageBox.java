package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Window;

import com.thoughtworks.selenium.Selenium;

public class MessageBox
    extends Window
{

    public MessageBox( Selenium selenium )
    {
        super( selenium, "Sonatype.MessageBox.getDialog()" );
    }

    public MessageBox clickYes()
    {
        selenium.click( "Yes" );

        return this;
    }

    public MessageBox clickNo()
    {
        selenium.click( "No" );

        return this;
    }

}
