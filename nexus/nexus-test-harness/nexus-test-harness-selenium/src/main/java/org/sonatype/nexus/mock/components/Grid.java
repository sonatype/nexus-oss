package org.sonatype.nexus.mock.components;

import com.thoughtworks.selenium.Selenium;

public class Grid
    extends Component
{

    public Grid( Component parent, String expression )
    {
        super( parent, expression );
    }

    public Grid( Selenium selenium, String expression )
    {
        super( selenium, expression );
    }

    public Grid select( int index )
    {
        eval( ".getSelectionModel().selectRow(" + index + ")" );

        return this;
    }

    public boolean isSelected( int index )
    {
        return evalTrue( ".getSelectionModel().isSelected(" + index + ")" );
    }

    public int getStoreDataLength()
    {
        String eval = getEval( ".getStore().data.length" );
        if ( eval == null || eval.equals( "null" ))
        {
            return 0;
        }

        return new Integer( eval );
    }

    public int getSelectedIndex()
    {
        int length = getStoreDataLength();
        for ( int i = 0; i < length; i++ )
        {
            if ( isSelected( i ) )
            {
                return i;
            }
        }

        return -1;
    }

}
