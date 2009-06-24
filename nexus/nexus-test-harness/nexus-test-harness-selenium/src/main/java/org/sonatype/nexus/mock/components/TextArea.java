package org.sonatype.nexus.mock.components;

import com.thoughtworks.selenium.Selenium;

public class TextArea
    extends TextField
{
    public TextArea( Selenium selenium, String expression )
    {
        super( selenium, expression );
    }

    public TextArea( Component parent, String expression )
    {
        super( parent, expression );
    }

    @Override
    public String getValue()
    {
        return getEval( ".getValue()" );
    }
}
