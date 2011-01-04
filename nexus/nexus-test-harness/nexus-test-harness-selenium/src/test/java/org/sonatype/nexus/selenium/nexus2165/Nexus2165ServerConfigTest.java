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
package org.sonatype.nexus.selenium.nexus2165;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.pages.ServerTab;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus2165ServerConfigTest.class )
public class Nexus2165ServerConfigTest
    extends SeleniumTest
{

    @Test
    public void requiredFields()
    {
        doLogin();

        ServerTab serverCfg = main.openServer();
        assertErrorText( serverCfg.getSmtpHost(), "127.0.0.1" );
        assertErrorText( serverCfg.getSmtpPort(), 1125 );
        assertErrorText( serverCfg.getSmtpEmail(), "admin@sonatype.org" );

        assertErrorText( serverCfg.getGlobalTimeout(), 10 );
        assertErrorText( serverCfg.getGlobalRetry(), 3 );

        serverCfg.getSecurityRealms().removeAll();
        Assert.assertTrue( serverCfg.getSecurityRealms().hasErrorText( "Select one or more items" ) );
        serverCfg.getSecurityRealms().addAll();
        Assert.assertFalse( serverCfg.getSecurityRealms().hasErrorText( "Select one or more items" ) );

        assertErrorText( serverCfg.getSecurityAnonymousUsername(), "anonymous" );
        assertErrorText( serverCfg.getSecurityAnonymousPassword(), "anonymous" );

        assertErrorText( serverCfg.getApplicationBaseUrl(), "http://localhost:8081/nexus" );
    }

    private void assertErrorText( TextField tf, String validText )
    {
        tf.type( "" );
        assertTrue( "Expected validation", tf.hasErrorText( "This field is required" ) );
        tf.type( validText );
        assertFalse( "Should pass validation", tf.hasErrorText( "This field is required" ) );
    }

    private void assertErrorText( TextField tf, int validValue )
    {
        assertErrorText( tf, String.valueOf( validValue ) );
    }

}
