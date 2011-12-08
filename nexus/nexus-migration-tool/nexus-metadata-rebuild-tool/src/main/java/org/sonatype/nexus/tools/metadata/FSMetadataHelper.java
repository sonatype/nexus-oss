/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.tools.metadata;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.sonatype.nexus.proxy.maven.metadata.AbstractMetadataHelper;
import org.sonatype.nexus.util.DigesterUtils;

@Component( role = FSMetadataHelper.class )
public class FSMetadataHelper
    extends AbstractMetadataHelper
{
    @Requirement( role = GavCalculator.class, hint = "maven2" )
    private GavCalculator gavCalculator;

    public FSMetadataHelper( Logger logger )
    {
        super( logger );
    }

    private String repo;

    @Override
    public InputStream retrieveContent( String path )
        throws IOException
    {
        return new FileInputStream( repo + path );
    }

    @Override
    public void store( String content, String path )
        throws IOException
    {
        String file = repo + path;

        FileUtils.fileWrite( file, content );
    }

    @Override
    public boolean exists( String path )
    {
        return FileUtils.fileExists( repo + path );
    }

    public String getRepo()
    {
        return repo;
    }

    public void setRepo( String repo )
    {
        this.repo = repo;
    }

    // copy from org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector
    @Override
    public String buildMd5( String path )
        throws IOException
    {
        return DigesterUtils.getMd5Digest( retrieveContent( path ) );
    }

    @Override
    public String buildSh1( String path )
        throws IOException
    {
        return DigesterUtils.getSha1Digest( retrieveContent( path ) );
    }

    @Override
    public void remove( String path )
        throws IOException
    {
        FileUtils.forceDelete( repo + path );
    }

    @Override
    protected GavCalculator getGavCalculator()
    {
        return gavCalculator;
    }

}
