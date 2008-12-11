/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.maven.maven2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.M2ArtifactRecognizer;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepository;
import org.sonatype.nexus.proxy.maven.ArtifactPackagingMapper;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.util.AlphanumComparator;

/**
 * The default M2Repository.
 * 
 * @author cstamas
 */
@Component( role = Repository.class, hint = "maven2", instantiationStrategy = "per-lookup" )
public class M2Repository
    extends AbstractMavenRepository
{
    /**
     * The ContentClass.
     */
	@Requirement( hint = "maven2" )
    private ContentClass contentClass;

    /**
     * The GAV Calculator.
     */
	@Requirement( hint = "maven2" )
    private GavCalculator gavCalculator;

    /**
     * The artifact packaging mapper.
     */
	@Requirement
    private ArtifactPackagingMapper artifactPackagingMapper;

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public GavCalculator getGavCalculator()
    {
        return gavCalculator;
    }

    public ArtifactPackagingMapper getArtifactPackagingMapper()
    {
        return artifactPackagingMapper;
    }

    /**
     * Should serve by policies.
     * 
     * @param uid the uid
     * @return true, if successful
     */
    public boolean shouldServeByPolicies( RepositoryItemUid uid )
    {
        if ( M2ArtifactRecognizer.isMetadata( uid.getPath() ) )
        {
            if ( M2ArtifactRecognizer.isSnapshot( uid.getPath() ) )
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
        Gav gav = gavCalculator.pathToGav( uid.getPath() );
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

    protected AbstractStorageItem doCacheItem( AbstractStorageItem item )
        throws StorageException
    {
        // if the item is file, is M2 repository metadata and this repo is release-only or snapshot-only
        if ( isCleanseRepositoryMetadata() && StorageFileItem.class.isAssignableFrom( item.getClass() )
            && M2ArtifactRecognizer.isMetadata( item.getPath() ) )
        {
            InputStream orig = null;
            StorageFileItem mdFile = (StorageFileItem) item;
            ByteArrayInputStream backup = null;
            try
            {
                // remote item is not reusable, and we usually cache remote stuff locally
                ByteArrayOutputStream backup1 = new ByteArrayOutputStream();
                orig = mdFile.getInputStream();
                IOUtil.copy( orig, backup1 );
                IOUtil.close( orig );
                backup = new ByteArrayInputStream( backup1.toByteArray() );

                // Metadata is small, let's do it in memory
                MetadataXpp3Reader metadataReader = new MetadataXpp3Reader();
                InputStreamReader isr = new InputStreamReader( backup );
                Metadata imd = metadataReader.read( isr );

                // and fix it
                imd = cleanseMetadataForRepository( RepositoryPolicy.SNAPSHOT.equals( getRepositoryPolicy() ), imd );

                // serialize and swap the new metadata
                MetadataXpp3Writer metadataWriter = new MetadataXpp3Writer();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter( bos );
                metadataWriter.write( osw, imd );
                ByteArrayInputStream bis = new ByteArrayInputStream( bos.toByteArray() );
                mdFile.setContentLocator( new PreparedContentLocator( bis ) );
            }
            catch ( Exception e )
            {
                getLogger().error( "Exception during repository metadata cleansing.", e );

                if ( backup != null )
                {
                    // get backup and continue operation
                    backup.reset();
                    mdFile.setContentLocator( new PreparedContentLocator( backup ) );
                }
            }
        }
        return super.doCacheItem( item );
    }

    protected boolean isOld( StorageItem item )
    {
        if ( M2ArtifactRecognizer.isMetadata( item.getPath() ) )
        {
            return isOld( getMetadataMaxAge(), item );
        }
        if ( M2ArtifactRecognizer.isSnapshot( item.getPath() ) )
        {
            return isOld( getSnapshotMaxAge(), item );
        }

        // we are using Gav to test the path
        Gav gav = gavCalculator.pathToGav( item.getPath() );

        if ( gav == null )
        {
            // this is not an artifact, it is just any "file"
            return super.isOld( item );
        }
        // it is a release
        return isOld( getReleaseMaxAge(), item );
    }

    @SuppressWarnings( "unchecked" )
    protected Metadata cleanseMetadataForRepository( boolean snapshot, Metadata metadata )
    {
        // remove base versions not belonging here
        List<String> versions = metadata.getVersioning().getVersions();
        for ( Iterator<String> iversion = versions.iterator(); iversion.hasNext(); )
        {
            // if we need snapshots and the version is not snapshot, or
            // if we need releases and the version is snapshot
            if ( ( snapshot && !VersionUtils.isSnapshot( iversion.next() ) ) || ( !snapshot && VersionUtils.isSnapshot( iversion.next() ) ) )
            {
                iversion.remove();
            }
        }

        metadata.getVersioning().setLatest( getLatestVersion( metadata.getVersioning().getVersions() ) );
        if ( snapshot )
        {
            metadata.getVersioning().setRelease( null );
        }
        else
        {
            metadata.getVersioning().setRelease( metadata.getVersioning().getLatest() );
        }
        return metadata;
    }

    public String getLatestVersion( List<String> versions )
    {
        Collections.sort( versions, new AlphanumComparator() );

        return versions.get( versions.size() - 1 );
    }

}
