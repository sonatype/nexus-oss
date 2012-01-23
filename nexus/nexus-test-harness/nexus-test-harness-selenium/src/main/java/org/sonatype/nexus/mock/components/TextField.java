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
        evalTrue( ".setValue( '' )" );
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
