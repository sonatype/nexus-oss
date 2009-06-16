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
        selectLeftSide( id );

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
        selectRightSide( id );

        runScript( ".items.items[1].removeOne()" );

        return this;
    }

    public TwinPanel removeAll()
    {
        runScript( ".items.items[1].removeAll()" );

        return this;
    }

    public TwinPanel selectLeftSide( String id )
    {
        runScript( ".items.items[2].getSelectionModel().select(" + expression + ".items.items[2].nodeHash." + id + ")" );

        return this;
    }

    public TwinPanel selectRightSide( String id )
    {
        runScript( ".items.items[0].getSelectionModel().select(" + expression + ".items.items[0].nodeHash." + id + ")" );

        return this;
    }

    public boolean hasErrorText( String err )
    {
        String text = selenium.getText( getXPath() + "/../..//div[@class='x-form-invalid-msg']" );

        return err.equals( text );
    }

}
