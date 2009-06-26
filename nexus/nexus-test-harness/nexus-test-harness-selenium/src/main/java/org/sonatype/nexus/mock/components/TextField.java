package org.sonatype.nexus.mock.components;

import com.thoughtworks.selenium.Selenium;

public class TextField
    extends Component
{
    public TextField( Selenium selenium, String expression )
    {
        super( selenium, expression );
    }

    public TextField( Component parent, String expression )
    {
        super( parent, expression );
    }

    public void type( String text )
    {
        waitToLoad();

        focus();
        selenium.type( getId(), text );
        blur();
    }

    public boolean hasErrorText( String err )
    {
        String text = getErrorText();

        return err.equals( text );
    }

    public void focus()
    {
        selenium.fireEvent( getId(), "focus" );
    }

    public void resetValue()
    {
        selenium.type( getId(), "" );
    }

    public void blur()
    {
        selenium.fireEvent( getId(), "blur" );
    }

    public String getValue()
    {
        return getEval( ".value" );
    }

    public String getErrorText()
    {
        return selenium.getText( getXPath() + "/../..//div[@class='x-form-invalid-msg']" );
    }
}
