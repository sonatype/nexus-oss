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
package org.sonatype.nexus.web;

import java.io.File;

import javax.servlet.http.HttpServlet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

/**
 * Big fat not: this is semi-finished: maven sets the basedir, hence it was esites to move plexus files to /conf/ folder
 * in root of this module.
 * 
 * @author cstamas
 */
public class PlexusContainerContextListenerTest
{
    protected File webXml;

    protected ServletRunner servletRunner;

    @Before
    public void setUp()
        throws Exception
    {
        webXml = new File( "src/test/resources/httpunit/WEB-INF/web.xml" );

        servletRunner = new ServletRunner( webXml, "/target/httpunit" );
    }

    @Test
    public void testListener()
        throws Exception
    {
        ServletUnitClient client = servletRunner.newClient();

        WebRequest request = new PostMethodWebRequest( "http://localhost/target/httpunit/dummyServlet" );

        InvocationContext context = client.newInvocation( request );

        HttpServlet servlet = (HttpServlet) context.getServlet();

        Assert.assertNotNull( servlet.getServletContext().getAttribute( "plexus" ) );
    }
}
