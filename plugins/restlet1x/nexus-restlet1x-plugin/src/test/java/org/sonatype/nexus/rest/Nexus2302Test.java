/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest;

import org.junit.Ignore;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.sonatype.nexus.NexusAppTestSupport;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;

public class Nexus2302Test
    extends NexusAppTestSupport
{
    private ContentPlexusResource contentResource;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        contentResource = (ContentPlexusResource) lookup( ManagedPlexusResource.class, "content" );
    }

    public void doTestPathEncoding( final String uri )
    {
        // this is how it would come from some servlet container (encoded)
        final String encodedUri = Reference.encode( uri );

        Reference baseRef = new Reference( "http://localhost:8081/nexus/" );

        Request request = new Request( Method.GET, new Reference( baseRef, baseRef.toString() + encodedUri ) );

        // check is the method getResourceStorePath() handles encoded paths
        final String resourceStorePath = contentResource.getResourceStorePath( request );

        assertEquals( uri, resourceStorePath );
    }

    @Ignore
    @Test
    public void testSimplePath()
    {
        // FIXME: skipped
        // doTestPathEncoding( "content/repositories/central/org/log4j/log4j/1.2.13/log4j-1.2.13.jar" );
    }

    @Ignore
    @Test
    public void testProblematicPath()
    {
        // doTestPathEncoding(
        // "content/repositories/central/nexus-2302/utilities/0.4.0-SNAPSHOT/utilities-0.4.0-SNAPSHOT-i386-Linux-g++-static.nar"
        // );
    }

}
