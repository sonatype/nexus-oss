/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
        runScript( ".getSelectionModel().selectRow(" + index + ")" );

        waitToLoad();

        return this;
    }

    public Grid select( String key )
    {
        // Didn't worked!
        // String eval = getEval( ".getStore().data.indexOfKey('" + key + "')" );

        String[] keys = getKeys();
        for ( int i = 0; i < keys.length; i++ )
        {
            if ( key.equals( keys[i] ) )
            {
                select( i );

                return this;
            }

        }

        throw new IllegalArgumentException( "Unable to select" + key );

    }

    public boolean contains( String key )
    {
        String[] keys = getKeys();
        for ( int i = 0; i < keys.length; i++ )
        {
            if ( key.equals( keys[i] ) )
            {
                return true;
            }

        }

        return false;

    }

    public boolean isSelected( int index )
    {
        return evalTrue( ".getSelectionModel().isSelected(" + index + ")" );
    }

    public int getStoreDataLength()
    {
        String eval = getEval( ".getStore().data.length" );
        if ( eval == null || eval.equals( "null" ) )
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

    public Menu openContextMenu( String selectedKey, String menuId )
    {
        return openContextMenu( selectedKey, 1, menuId );
    }

    public Menu openContextMenu( String selectedKey, int colNumber, String menuId )
    {
        selenium.contextMenu( getId() + "_" + selectedKey + "_col" + colNumber );

        return new Menu( selenium, "window.Ext.menu.MenuMgr.get('" + menuId + "')" );
    }

    public Menu openContextMenu( int rowNumber, int colNumber, String menuId )
    {
        String[] keys = getKeys();
        return openContextMenu( keys[rowNumber], colNumber, menuId );
    }

    public String[] getKeys()
    {
        return getEval( ".getStore().data.keys" ).split( "," );
    }

}
