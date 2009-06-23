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

    public void select( int i )
    {
        // workaround to select an item on combobox
        focus();

        runScript( ".expand()" );

        String id;
        if ( i == 0 )
        {
            id = getEval( ".innerList.first().id" );
        }
        else
        {
            runScript( ".innerList.first()" );
            for ( int j = 1; j < i; j++ )
            {
                runScript( ".innerList.next()" );
            }
            id = getEval( ".innerList.next().id" );
        }

        selenium.click( "//*[@id='" + id + "']" );

        blur();
    }

}
