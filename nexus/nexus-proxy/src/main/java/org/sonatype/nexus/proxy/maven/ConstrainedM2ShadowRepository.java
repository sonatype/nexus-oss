/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.maven;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.M2ArtifactRecognizer;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.util.AlphanumComparator;

/**
 * A M2 shadow repository that makes constraints against artifact versions, simply blocking maven plugin discovery using
 * metadata. This shadow will (if set) report specific plugin versions only.
 * 
 * @author cstamas
 * @plexus.component instantiation-strategy="per-lookup" role="org.sonatype.nexus.proxy.repository.Repository" role-hint="m2-constrained"
 */
public class ConstrainedM2ShadowRepository
    extends ShadowRepository
    implements MavenRepository
{
    /**
     * The GAV Calculator.
     * 
     * @plexus.requirement role-hint="m2"
     */
    private GavCalculator gavCalculator;

    private ContentClass contentClass = new Maven2ContentClass();

    private Map<String, String> versionMap;

    public Map<String, String> getVersionMap()
    {
        return versionMap;
    }

    public void setVersionMap( Map<String, String> versionMap )
    {
        this.versionMap = versionMap;
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public ContentClass getMasterRepositoryContentClass()
    {
        return contentClass;
    }

    public RepositoryPolicy getRepositoryPolicy()
    {
        return ( (MavenRepository) getMasterRepository() ).getRepositoryPolicy();
    }

    @Override
    protected String transformMaster2Shadow( String path )
    {
        return path;
    }

    @Override
    protected String transformShadow2Master( String path )
    {
        return path;
    }

    public StorageFileItem retrieveArtifactPom( String groupId, String artifactId, String version )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        ArtifactStoreHelper ash = new ArtifactStoreHelper( this, gavCalculator );

        return ash.retrieveArtifactPom( groupId, artifactId, version );
    }

    public StorageFileItem retrieveArtifact( String groupId, String artifactId, String version, String classifier )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        ArtifactStoreHelper ash = new ArtifactStoreHelper( this, gavCalculator );

        return ash.retrieveArtifact( groupId, artifactId, version, classifier );
    }

    protected StorageItem doRetrieveItem( boolean localOnly, RepositoryItemUid uid, Map<String, Object> context )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        if ( M2ArtifactRecognizer.isMetadata( uid.getPath() ) && !M2ArtifactRecognizer.isChecksum( uid.getPath() ) )
        {
            // this is metadata file
            StorageItem item = super.doRetrieveItem( localOnly, uid, context );

            if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
            {
                StorageFileItem file = (StorageFileItem) item;

                // remote item is not reusable, and we usually cache remote stuff locally
                ByteArrayInputStream backup = null;
                try
                {
                    // prepare for the worst, backup it to be able to reemit it
                    ByteArrayOutputStream backup1 = new ByteArrayOutputStream();

                    IOUtil.copy( file.getInputStream(), backup1 );

                    backup = new ByteArrayInputStream( backup1.toByteArray() );

                    // Metadata is small, let's do it in memory
                    MetadataXpp3Reader metadataReader = new MetadataXpp3Reader();

                    InputStreamReader isr = new InputStreamReader( backup );

                    Metadata imd = metadataReader.read( isr );

                    // sanity check, the metadata should have gid and aid as we have from path
                    GA ga = getGaForMetadata( uid.getPath() );

                    if ( shouldProcess( ga ) && ga.getGroupId().equals( imd.getGroupId() )
                        && ga.getArtifactId().equals( imd.getArtifactId() ) && imd.getVersioning() != null )
                    {
                        VersionRange versionRange = VersionRange.createFromVersionSpec( getVersionForGA( ga ) );

                        String versionStr;

                        DefaultArtifactVersion version;

                        List<String> versions = imd.getVersioning().getVersions();

                        for ( Iterator<String> i = versions.iterator(); i.hasNext(); )
                        {
                            versionStr = i.next();

                            version = new DefaultArtifactVersion( versionStr );

                            if ( !versionRange.containsVersion( version ) )
                            {
                                i.remove();
                            }
                        }
                        if ( versions.size() > 0 )
                        {
                            Collections.sort( versions, new AlphanumComparator() );

                            imd.setVersion( versions.get( versions.size() - 1 ) );

                            imd.getVersioning().setLatest( imd.getVersion() );

                            imd.getVersioning().setRelease( imd.getVersion() );
                        }
                        else
                        {
                            imd.setVersion( null );

                            imd.getVersioning().setLatest( null );

                            imd.getVersioning().setRelease( null );
                        }

                        // serialize and swap the new metadata
                        MetadataXpp3Writer metadataWriter = new MetadataXpp3Writer();

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();

                        OutputStreamWriter osw = new OutputStreamWriter( bos );

                        metadataWriter.write( osw, imd );

                        ByteArrayInputStream bis = new ByteArrayInputStream( bos.toByteArray() );

                        file.setContentLocator( new PreparedContentLocator( bis ) );
                    }
                    else
                    {
                        // get backup and continue operation
                        backup.reset();

                        file.setContentLocator( new PreparedContentLocator( backup ) );
                    }

                    return file;
                }
                catch ( Exception e )
                {
                    getLogger().error( "Exception during repository metadata constraining.", e );

                    if ( backup != null )
                    {
                        // get backup and continue operation
                        backup.reset();

                        file.setContentLocator( new PreparedContentLocator( backup ) );
                    }

                    return file;
                }

            }
            else
            {
                // what the hell is this???
                return item;
            }
        }
        else
        {
            // the "normal" way, serving the file from repo (cache or remote, whatever)
            return super.doRetrieveItem( localOnly, uid, context );
        }
    }

    protected GA getGaForMetadata( String path )
    {
        String s = path.startsWith( "/" ) ? path.substring( 1 ) : path;

        int aEndPos = s.lastIndexOf( '/' );

        if ( aEndPos == -1 )
        {
            return null;
        }

        int gEndPos = s.lastIndexOf( '/', aEndPos - 1 );

        if ( gEndPos == -1 )
        {
            return null;
        }

        String g = s.substring( 0, gEndPos ).replace( '/', '.' );
        String a = s.substring( gEndPos + 1, aEndPos );

        return new GA( g, a );
    }

    protected boolean shouldProcess( GA ga )
    {
        if ( ga == null )
        {
            return false;
        }

        return getVersionForGA( ga ) != null;
    }

    protected String getVersionForGA( GA ga )
    {
        return getVersionMap().get( ga.getGroupId() + ":" + ga.getArtifactId() );
    }

    private class GA
    {
        private String groupId;

        private String artifactId;

        public GA( String gid, String aid )
        {
            this.groupId = gid;
            this.artifactId = aid;
        }

        public String getGroupId()
        {
            return groupId;
        }

        public String getArtifactId()
        {
            return artifactId;
        }
    }

}
