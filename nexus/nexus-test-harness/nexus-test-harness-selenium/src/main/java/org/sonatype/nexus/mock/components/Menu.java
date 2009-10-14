package org.sonatype.nexus.mock.components;

import com.thoughtworks.selenium.Selenium;

public class Menu
    extends Component
{

    public Menu( Component parent, String expression )
    {
        super( parent, expression );
    }

    public Menu( Selenium selenium, String expression )
    {
        super( selenium, expression );
    }

    public Menu( Selenium selenium )
    {
        super( selenium );
    }

    public void click( String itemKey )
    {
        String id = getEval( ".items.items[" + expression + ".items.indexOfKey('" + itemKey + "')].id" );
        selenium.click( "//*[@id='" + id + "']" );
    }

    public void click( String propName, String propValue )
    {
        getEval( ".items.items[" + expression + ".items.findIndex('" + propName + "', '" + propValue + "')].id" );
        String id =
            getEval( ".items.items[" + expression + ".items.findIndex('" + propName + "', '" + propValue + "')].el.id" );
        selenium.click( "//*[@id='" + id + "']" );
    }
}
