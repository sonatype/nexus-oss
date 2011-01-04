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
package org.sonatype.nexus.security.upgrade;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.events.EventInspector;

public class SecurityConfigurationUpgradeTest
    extends AbstractNexusTestCase
{

    protected void copySecurityConfigToPlace()
        throws IOException
    {
        this.copyResource( "/org/sonatype/nexus/security/upgrade/security.xml", getNexusSecurityConfiguration() );
    }

    public void testLoadComponent() throws Exception
    {
        Assert.assertNotNull( this.lookup( EventInspector.class, "SecurityUpgradeEventInspector" ) );
    }

    public void testSecurityUpgradeAndEvent()
        throws Exception
    {
        testLoadComponent();

        this.copySecurityConfigToPlace();

        this.lookup( Nexus.class );

        // verify
        this.verifyUpgrade( "/org/sonatype/nexus/security/upgrade/security.result.xml" );

    }

    private void verifyUpgrade( String resource )
        throws IOException
    {
        InputStream stream = null;
        StringWriter writer = new StringWriter();
        try
        {
            stream = getClass().getResourceAsStream( resource );
            IOUtil.copy( stream, writer );
        }
        finally
        {
            IOUtil.close( stream );
        }

        String expected = writer.toString();

        // security should be upgraded now. lets look at the security.xml
        String securityXML = FileUtils.fileRead( getNexusSecurityConfiguration() );

        Assert.assertEquals( expected.replace( "\r", "" ), securityXML.replace( "\r", "" ) );

    }

}
