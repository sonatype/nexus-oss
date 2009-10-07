package org.sonatype.nexus.mock.components;

import com.thoughtworks.selenium.Selenium;

public class Tree
    extends Component
{

    public Tree( Component parent, String expression )
    {
        super( parent, expression );
    }

    public Tree( Selenium selenium, String expression )
    {
        super( selenium, expression );
    }

    public Tree( Selenium selenium )
    {
        super( selenium );
    }

    public Tree select( String id )
    {
        runScript( ".getSelectionModel().select(" //
            + expression + ".nodeHash['" + id + "']" + //
            ")" );

        return this;
    }

    public boolean hasErrorText( String err )
    {
        String text = getErrorText();

        return err.equals( text );
    }

    public String getErrorText()
    {
        String text = selenium.getText( getXPath() + "//div[@class='x-form-invalid-msg']" );
        return text;
    }

    public boolean contains( String id )
    {
        String eval = getEval( ".nodeHash['" + id + "'] != null" );
        return Boolean.parseBoolean( eval );
    }

}
