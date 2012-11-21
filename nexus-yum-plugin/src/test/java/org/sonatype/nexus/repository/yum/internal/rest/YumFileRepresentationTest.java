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
package org.sonatype.nexus.repository.yum.internal.rest;

import static org.junit.Assert.assertEquals;
import static org.sonatype.nexus.repository.yum.internal.support.AbstractYumNexusTestCase.UTIL;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.repository.yum.internal.YumRepositoryImpl;

public class YumFileRepresentationTest
{

    @Test
    public void shouldReturnXmlFile()
        throws Exception
    {
        YumFileRepresentation representation = createRepresentation( "repomd.xml" );
        assertEquals( MediaType.APPLICATION_XML, representation.getMediaType() );
    }

    @Test
    public void shouldReturnGzFile()
        throws Exception
    {
        YumFileRepresentation representation = createRepresentation( "primary.xml.gz" );
        assertEquals( MediaType.APPLICATION_GNU_ZIP, representation.getMediaType() );
    }

    @Test
    public void shouldReturnAllFile()
        throws Exception
    {
        YumFileRepresentation representation = createRepresentation( "primary.txt" );
        assertEquals( MediaType.APPLICATION_ALL, representation.getMediaType() );
    }

    private YumFileRepresentation createRepresentation( String filename )
    {
        return new YumFileRepresentation(
            new UrlPathInterpretation( null, filename, false ),
            new YumRepositoryImpl( UTIL.getBaseDir(), null, null )
        );
    }
}
