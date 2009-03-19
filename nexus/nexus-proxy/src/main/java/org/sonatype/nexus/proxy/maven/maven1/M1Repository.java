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
package org.sonatype.nexus.proxy.maven.maven1;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.M1ArtifactRecognizer;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryConfigurator;

/**
 * The default M1Repository. This class adds snapshot/release sensing and differentiated expiration handling and repo
 * policies for them.
 * 
 * @author cstamas
 */
@Component( role = Repository.class, hint = "maven1", instantiationStrategy = "per-lookup", description = "Maven1 Repository" )
public class M1Repository
    extends AbstractMavenRepository
{
    /**
     * The GAV Calculator.
     */
    @Requirement( hint = "maven1" )
    private GavCalculator gavCalculator;

    @Requirement( hint = "maven1" )
    private ContentClass contentClass;

    @Requirement
    private M1RepositoryConfigurator m1RepositoryConfigurator;

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public GavCalculator getGavCalculator()
    {
        return gavCalculator;
    }

    @Override
    public RepositoryConfigurator getRepositoryConfigurator()
    {
        return m1RepositoryConfigurator;
    }

    /**
     * Should serve by policies.
     * 
     * @param uid the uid
     * @return true, if successful
     */
    public boolean shouldServeByPolicies( ResourceStoreRequest request )
    {
        if ( M1ArtifactRecognizer.isMetadata( request.getRequestPath() ) )
        {
            if ( M1ArtifactRecognizer.isSnapshot( request.getRequestPath() ) )
            {
                return RepositoryPolicy.SNAPSHOT.equals( getRepositoryPolicy() );
            }
            else
            {
                // metadatas goes always
                return true;
            }
        }

        // we are using Gav to test the path
        Gav gav = getGavCalculator().pathToGav( request.getRequestPath() );

        if ( gav == null )
        {
            return true;
        }
        else
        {
            if ( gav.isSnapshot() )
            {
                // snapshots goes if enabled
                return RepositoryPolicy.SNAPSHOT.equals( getRepositoryPolicy() );
            }
            else
            {
                return RepositoryPolicy.RELEASE.equals( getRepositoryPolicy() );
            }
        }
    }

    @Override
    protected boolean isOld( StorageItem item )
    {
        if ( M1ArtifactRecognizer.isMetadata( item.getPath() ) )
        {
            return isOld( getMetadataMaxAge(), item );
        }
        if ( M1ArtifactRecognizer.isSnapshot( item.getPath() ) )
        {
            return isOld( getArtifactMaxAge(), item );
        }

        // we are using Gav to test the path
        Gav gav = getGavCalculator().pathToGav( item.getPath() );

        if ( gav == null )
        {
            // this is not an artifact, it is just any "file"
            return super.isOld( item );
        }
        
        // it is a release
        return isOld( getArtifactMaxAge(), item );
    }

    // not available on maven1 repo
    public boolean recreateMavenMetadata( String path )
    {
        return false;
    }
}
