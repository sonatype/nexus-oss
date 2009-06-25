/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.maven;

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;

public class ArtifactStoreRequest
    extends ResourceStoreRequest
{
    private final MavenRepository mavenRepository;

    private final Gav gav;

    public ArtifactStoreRequest( MavenRepository repository, String path, boolean localOnly )
        throws IllegalArtifactCoordinateException
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
        super( repository.getGavCalculator().gavToPath( gav ), localOnly );

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
