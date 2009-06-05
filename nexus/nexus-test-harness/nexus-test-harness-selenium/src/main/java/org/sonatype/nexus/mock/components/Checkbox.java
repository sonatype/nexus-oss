package org.sonatype.nexus.mock.components;

import com.thoughtworks.selenium.Selenium;

public class Checkbox
    extends TextField
{

    public Checkbox( Component parent, String expression )
    {
        super( parent, expression );
    }

    public Checkbox( Selenium selenium, String expression )
    {
        super( selenium, expression );
    }

    public void check( boolean enable )
    {
        if ( enable != evalTrue( ".checked" ) )
        {
            click();
        }
    }

    public void click()
    {
        waitForEvalTrue( ".disabled == false" );
        selenium.click( getXPath() );
    }

}
