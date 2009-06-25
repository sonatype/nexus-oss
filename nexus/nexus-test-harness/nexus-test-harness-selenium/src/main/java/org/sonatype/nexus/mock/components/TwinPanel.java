package org.sonatype.nexus.mock.components;

import com.thoughtworks.selenium.Selenium;

public class TwinPanel
    extends Component
{

    public TwinPanel( Selenium selenium, String expression )
    {
        super( selenium, expression );
    }

    public TwinPanel add( String id )
    {
        selectRightSide( id );

        runScript( ".items.items[1].addOne()" );

        return this;
    }

    public TwinPanel addAll()
    {
        runScript( ".items.items[1].addAll()" );

        return this;
    }

    public TwinPanel remove( String id )
    {
        selectLeftSide( id );

        runScript( ".items.items[1].removeOne()" );

        return this;
    }

    public TwinPanel removeAll()
    {
        runScript( ".items.items[1].removeAll()" );

        return this;
    }

    public TwinPanel selectRightSide( String id )
    {
        return select( 2, id );
    }

    private TwinPanel select( int i, String id )
    {
        runScript( ".items.items[" + i + "].getSelectionModel().select(" //
            + expression + ".items.items[" + i + "].nodeHash['" + id + "']" + //
            ")" );

        return this;
    }

    public TwinPanel selectLeftSide( String id )
    {
        return select( 0, id );
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

    public boolean containsLeftSide( String id )
    {
        String eval = getEval( ".items.items[0].nodeHash['" + id + "'] != null" );
        return Boolean.parseBoolean( eval );
    }

}
