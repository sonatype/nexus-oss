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
package org.sonatype.nexus.rest;

import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;

public class Nexus2302Test
    extends org.sonatype.nexus.AbstractNexusTestCase
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

    public void testSimplePath()
    {
        // FIXME: skipped
        // doTestPathEncoding( "content/repositories/central/org/log4j/log4j/1.2.13/log4j-1.2.13.jar" );
    }

    public void testProblematicPath()
    {
        // doTestPathEncoding(
        // "content/repositories/central/nexus-2302/utilities/0.4.0-SNAPSHOT/utilities-0.4.0-SNAPSHOT-i386-Linux-g++-static.nar"
        // );
    }

}
