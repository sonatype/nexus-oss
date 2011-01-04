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
