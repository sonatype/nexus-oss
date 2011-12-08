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
package org.sonatype.nexus.proxy.maven;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.maven.gav.Gav;

public class ArtifactStoreRequest
    extends ResourceStoreRequest
{
    private final MavenRepository mavenRepository;

    private final Gav gav;

    public ArtifactStoreRequest( MavenRepository repository, String path, boolean localOnly )
    {
        super( path, localOnly );

        this.mavenRepository = repository;

        this.gav = mavenRepository.getGavCalculator().pathToGav( path );

        if ( gav == null )
        {
            throw new IllegalArgumentException( "The path does not represent an artifact!" );
        }
    }

    public ArtifactStoreRequest( MavenRepository repository, Gav gav, boolean localOnly )
    {
        this( repository, gav, localOnly, false );
    }

    public ArtifactStoreRequest( MavenRepository repository, Gav gav, boolean localOnly, boolean remoteOnly )
    {
        super( repository.getGavCalculator().gavToPath( gav ), localOnly, remoteOnly );

        this.mavenRepository = repository;

        this.gav = gav;
    }

    public MavenRepository getMavenRepository()
    {
        return mavenRepository;
    }

    public Gav getGav()
    {
        return gav;
    }

    public String getGroupId()
    {
        return gav.getGroupId();
    }

    public String getArtifactId()
    {
        return gav.getArtifactId();
    }

    public String getVersion()
    {
        return gav.getVersion();
    }

    public String getClassifier()
    {
        return gav.getClassifier();
    }

    public String getExtension()
    {
        return gav.getExtension();
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer( getGroupId() );
        sb.append( ":" );
        sb.append( getArtifactId() );
        sb.append( ":" );
        sb.append( getVersion() );
        sb.append( ":c=" );
        sb.append( getClassifier() );
        sb.append( ":e=" );
        sb.append( getExtension() );

        return sb.toString();
    }

}
