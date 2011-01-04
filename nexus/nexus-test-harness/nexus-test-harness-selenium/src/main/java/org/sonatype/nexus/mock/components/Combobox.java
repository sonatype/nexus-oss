/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
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
        /*
         * var cb =
         * window.Ext.getCmp('security-privileges').cardPanel.getLayout().activeItem.getLayout().activeItem.find('name',
         * 'repositoryOrGroup')[0]; var value = 'repo_central'; cb.setValue( value ); cb.fireEvent('select', cb,
         * cb.store.getById(value), cb.store.indexOfId(value));
         * window.Ext.getCmp('security-privileges').cardPanel.getLayout().activeItem.getLayout().activeItem.find('name',
         * 'repositoryTargetId')[0].store.getCount();
         */
        focus();
        evalTrue( ".setValue( '" + value + "' )" );
        runScript( ".fireEvent( 'select', " + expression + ", " + expression + ".store.getById('" + value + "'), "
            + expression + ".store.indexOfId('" + value + "') )" );
        blur();
    }

    public void select( int i )
    {
        // workaround to select an item on combobox
        /*
         * var cb =
         * window.Ext.getCmp('security-privileges').cardPanel.getLayout().activeItem.getLayout().activeItem.find('name',
         * 'repositoryOrGroup')[0]; var i = 0; cb.setValue( cb.store.getAt(i).id ); cb.fireEvent('select', cb,
         * cb.store.getAt(i), i);
         * window.Ext.getCmp('security-privileges').cardPanel.getLayout().activeItem.getLayout().activeItem.find('name',
         * 'repositoryTargetId')[0].store.getCount();
         */
        focus();
        runScript( ".setValue(" + expression + ".store.getAt(" + i + ").id )" );
        runScript( ".fireEvent( 'select', " + expression + ", " + expression + ".store.getAt(" + i + "), " + i + " )" );
        blur();
    }

    public Integer getCount()
    {
        String eval = getEval( ".store.getCount()" );
        if ( eval == null || "null".equals( eval ) )
        {
            return null;
        }

        return new Integer( eval );
    }

}
