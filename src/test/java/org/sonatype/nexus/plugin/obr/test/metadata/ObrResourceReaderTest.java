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
package org.sonatype.nexus.plugin.obr.test.metadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;
import org.osgi.service.obr.Resource;
import org.sonatype.nexus.obr.metadata.ObrResourceReader;
import org.sonatype.nexus.obr.metadata.ObrSite;

public class ObrResourceReaderTest
    extends AbstractObrMetadataTest
{

    @Test
    public void testObrParsing()
        throws Exception
    {
        final ObrSite testSite = openObrSite( testRepository, "/obr/samples/osgi_alliance_obr.zip" );
        final ObrResourceReader reader = obrMetadataSource.getReader( testSite );

        final BufferedReader br =
            new BufferedReader( new InputStreamReader( getResourceAsStream( "/obr/samples/osgi_alliance_obr.lst" ) ) );

        int numBundles = 0;

        Resource r;
        while ( ( r = reader.readResource() ) != null )
        {
            assertEquals( br.readLine(), r.toString() );
            numBundles++;
        }

        assertNull( br.readLine() );

        assertEquals( 2710, numBundles );

        reader.close();
        br.close();
    }

    @Test
    public void testObrReferral()
        throws Exception
    {
        final ObrSite testSite = openObrSite( testRepository, "/obr/samples/referrals.xml" );
        final ObrResourceReader reader = obrMetadataSource.getReader( testSite );

        int numBundles = 0;
        int numExceptions = 0;

        while ( true )
        {
            try
            {
                final Resource r = reader.readResource();
                if ( r != null )
                {
                    numBundles++;
                }
                else
                {
                    break;
                }
            }
            catch ( final IOException e )
            {
                if ( ++numExceptions > 2 )
                {
                    throw e;
                }
            }
        }

        assertEquals( 2713, numBundles );
        assertEquals( 2, numExceptions );

        reader.close();
    }
}
