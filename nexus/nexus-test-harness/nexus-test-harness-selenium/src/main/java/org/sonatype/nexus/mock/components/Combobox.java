package org.sonatype.nexus.mock.components;

import com.thoughtworks.selenium.Selenium;

public class Combobox
    extends TextField
{

    public Combobox( Component parent, String expression )
    {
        super( parent, expression );
    }

    public Combobox( Selenium selenium, String expression )
    {
        super( selenium, expression );
    }

    public void click()
    {
        selenium.click( getXPath() );
    }

    public void setValue( String value )
    {
        evalTrue( ".setValue( '" + value + "' )" );
    }

}
