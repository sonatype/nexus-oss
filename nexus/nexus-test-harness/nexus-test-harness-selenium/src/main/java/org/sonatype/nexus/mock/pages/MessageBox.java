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
package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Window;

import com.thoughtworks.selenium.Selenium;

public class MessageBox
    extends Window
{

    public MessageBox( Selenium selenium )
    {
        super( selenium, "window.Sonatype.MessageBox.getDialog()" );
    }

    public MessageBox clickYes()
    {
        selenium.click( "Yes" );

        return this;
    }

    public MessageBox clickNo()
    {
        selenium.click( "No" );

        return this;
    }

    public MessageBox clickOk()
    {
        selenium.click( "OK" );

        return this;
    }

    public String getTitle()
    {
        waitForVisible();

        return getEval( ".title" );
    }

}
