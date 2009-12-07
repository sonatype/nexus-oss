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
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.artifact.M1ArtifactRecognizer;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.IllegalRequestException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;

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

    @Override
    public M1RepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (M1RepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<M1RepositoryConfiguration> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<M1RepositoryConfiguration>()
        {
            public M1RepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new M1RepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public GavCalculator getGavCalculator()
    {
        return gavCalculator;
    }

    @Override
    protected Configurator getConfigurator()
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
        Gav gav = null;

        try
        {
            gav = getGavCalculator().pathToGav( request.getRequestPath() );
        }
        catch ( IllegalArtifactCoordinateException e )
        {
            getLogger().info( "Illegal artifact path: '" + request.getRequestPath() + "'" + e.getMessage() );

            return false;
        }

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
        Gav gav = null;

        try
        {
            gav = getGavCalculator().pathToGav( item.getPath() );
        }
        catch ( IllegalArtifactCoordinateException e )
        {
            getLogger().info( "Illegal artifact path: '" + item.getPath() + "'" + e.getMessage() );
        }

        if ( gav == null )
        {
            // this is not an artifact, it is just any "file"
            return super.isOld( item );
        }

        return super.isOld( getArtifactMaxAge(), item )
            && ( !RepositoryPolicy.RELEASE.equals( getRepositoryPolicy() ) || !isEnforceReleaseRedownloadPolicy() );
    }

    // not available on maven1 repo
    public boolean recreateMavenMetadata( String path )
    {
        return false;
    }

    @Override
    protected void enforceWritePolicy( ResourceStoreRequest request, Action action )
        throws IllegalRequestException
    {
        // allow updating of metadata
        // we also need to allow updating snapshots
        if ( !M1ArtifactRecognizer.isMetadata( request.getRequestPath() )
            && !M1ArtifactRecognizer.isSnapshot( request.getRequestPath() ) )
        {
            super.enforceWritePolicy( request, action );
        }
    }

}
