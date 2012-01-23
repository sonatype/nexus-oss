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
package org.sonatype.nexus.integrationtests.webproxy.nexus1155;

import org.sonatype.nexus.integrationtests.webproxy.nexus1146.Nexus1146RepositoryOverProxyIT;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class Nexus1155RepositoryOverProxyWithAuthenticationIT
    extends Nexus1146RepositoryOverProxyIT
{

    @Override
    @BeforeMethod
    public void startWebProxy()
        throws Exception
    {
        super.startWebProxy();
        server.getProxyServlet().setUseAuthentication( true );
        server.getProxyServlet().getAuthentications().put( "admin", "123" );
    }

    @Override
    @AfterMethod
    public void stopWebProxy()
        throws Exception
    {
        server.getProxyServlet().setUseAuthentication( false );
        server.getProxyServlet().setAuthentications( null );
        super.stopWebProxy();
    }

}
