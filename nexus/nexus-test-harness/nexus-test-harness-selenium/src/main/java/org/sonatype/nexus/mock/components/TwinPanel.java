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
