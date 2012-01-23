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
package org.sonatype.nexus.integrationtests.nexus1329;

import org.restlet.data.MediaType;
import org.sonatype.jettytestsuite.ControlledServer;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.MirrorMessageUtils;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractMirrorIT
    extends AbstractNexusIntegrationTest
{

    public static final String REPO = "nexus1329-repo";

    protected static final int webProxyPort;

    static
    {
        webProxyPort = TestProperties.getInteger( "webproxy.server.port" );
    }

    protected ControlledServer server;

    protected MirrorMessageUtils messageUtil;

    public AbstractMirrorIT()
    {
        super();
        this.messageUtil = new MirrorMessageUtils( this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }

    @BeforeMethod
    public void start()
        throws Exception
    {
        server = lookup( ControlledServer.class );
    }

    @AfterMethod
    public void stop()
        throws Exception
    {
        // @After will be called even if there is a failure in @Before, and server is null!
        if ( server != null )
        {
            server.stop();
        }
    }

}