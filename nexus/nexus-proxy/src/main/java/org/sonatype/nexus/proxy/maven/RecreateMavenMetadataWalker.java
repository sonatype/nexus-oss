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

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.RepositoryNotListableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryType;
import org.sonatype.nexus.proxy.utils.StoreWalker;

/**
 * @author Juven Xu
 */
public class RecreateMavenMetadataWalker
    extends StoreWalker
{

    private Repository repository;

    private boolean isHostedRepo;

    public static final String VERSION_REGEX = "^[0-9].*$";

    /**
     * current groupId based on the current collection, if no groupId, it's null
     */
    private String currentGroupId;

    private String currentArtifactId;

    private String currentVersion;

    private Map<String, PluginInfoForMetadata> currentPlugins = new HashMap<String, PluginInfoForMetadata>();

    private MavenMetadataHelper mdHelper = new MavenMetadataHelper( this );

    public RecreateMavenMetadataWalker( Repository repository, Logger logger )
    {
        super( repository, logger );

        this.repository = repository;

        isHostedRepo = RepositoryType.HOSTED.equals( getRepository().getRepositoryType() );
    }

    protected void processItem( StorageItem item )
    {
        
        // we only handle pom file
        if ( !item.getName().endsWith( "pom" ) )
        {
            return;
        }

        if ( !StorageFileItem.class.isAssignableFrom( item.getClass() ) )
        {
            return;
        }

        handleMavenPlugin( (StorageFileItem) item );

    }

    protected void beforeWalk()
    {
        if ( !isHostedRepo )
        {
            stop( new Exception( "Not allowed to create metadata files for non-hosted repositoty" ) );
        }

    }

    private void handleMavenPlugin( StorageFileItem pomFile )
    {
        Model model = null;
        try
        {
            Reader reader = ReaderFactory.newXmlReader( pomFile.getInputStream() );
            MavenXpp3Reader xpp3 = new MavenXpp3Reader();

            try
            {
                model = xpp3.read( reader );
            }
            finally
            {
                reader.close();
                reader = null;
            }
        }
        catch ( Exception e )
        {
            getLogger().info( "Can't build POM model from " + pomFile.getPath(), e );
        }

        if ( model != null && model.getPackaging().equals( "maven-plugin" ) )
        {

            if ( !StringUtils.isEmpty( model.getName() ) )
            {
                currentPlugins.put( model.getArtifactId(), new PluginInfoForMetadata( model.getArtifactId(), model
                    .getName() ) );
            }
            else
            {
                currentPlugins.put( model.getArtifactId(), new PluginInfoForMetadata( model.getArtifactId() ) );
            }

        }
    }

    private void buildGAV( StorageCollectionItem coll )
    {
        if ( coll.getName().matches( VERSION_REGEX ) )
        {
            currentVersion = coll.getName();

            int spaceOfGAPos = coll.getParentPath().lastIndexOf( '/' );

            if ( currentArtifactId == null )
            {
                currentArtifactId = coll.getParentPath().substring( spaceOfGAPos + 1 );
            }

            if ( currentGroupId == null )
            {
                currentGroupId = coll.getParentPath().substring( 1, spaceOfGAPos ).replace( '/', '.' );
            }
        }

    }

    private void cleanGAV( StorageCollectionItem coll )
    {
        if ( currentVersion != null && coll.getName().equals( currentVersion ) )
        {
            currentVersion = null;
        }
        else if ( currentVersion == null && currentArtifactId != null && coll.getName().equals( currentArtifactId ) )
        {
            currentArtifactId = null;
        }
        else if ( currentArtifactId == null && currentGroupId != null && isGroupPath( coll.getPath() ) )
        {
            currentGroupId = null;
        }

    }

    protected void onCollectionEnter( StorageCollectionItem coll )
    {

        buildGAV( coll );

    }

    protected void onCollectionExit( StorageCollectionItem coll )
    {
        try
        {
            if ( shouldCreateMetadataForSnapshotVersionDir( coll ) )
            {
                mdHelper.createMetadataForSnapshotVersionDir( coll );
            }
            else if ( shouldCreateMetadataForArtifactDir( coll ) )
            {
                mdHelper.createMetadataForArtifactDir( coll );
            }
            else if ( shouldCreateMetadataForPluginGroupDir( coll ) )
            {
                mdHelper.createMetadataForPluginGroupDir( coll );
                
                currentPlugins.clear();
            }

        }
        catch ( Exception e )
        {
            getLogger().info( "Can't build maven metadata in " + coll.getPath(), e );
        }

        cleanGAV( coll );
    }

    public Repository getRepository()
    {
        return repository;
    }

    private boolean shouldCreateMetadataForArtifactDir( StorageCollectionItem coll )
        throws StorageException,
            AccessDeniedException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            RepositoryNotListableException,
            ItemNotFoundException
    {
        for ( StorageItem item : coll.list() )
        {
            if ( StorageCollectionItem.class.isAssignableFrom( item.getClass() )
                && item.getName().matches( VERSION_REGEX ) )
            {
                return true;
            }
        }

        return false;
    }

    private boolean shouldCreateMetadataForSnapshotVersionDir( StorageCollectionItem coll )
        throws StorageException,
            AccessDeniedException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            RepositoryNotListableException,
            ItemNotFoundException
    {
        if ( !coll.getName().matches( VERSION_REGEX ) )
        {
            return false;
        }
        if ( !coll.getName().endsWith( "SNAPSHOT" ) )
        {
            return false;
        }

        for ( StorageItem item : coll.list() )
        {
            if ( StorageFileItem.class.isAssignableFrom( item.getClass() ) )
            {
                return true;
            }
        }

        return false;
    }

    private boolean shouldCreateMetadataForPluginGroupDir( StorageCollectionItem coll )
    {
        if ( isGroupPath( coll.getPath() ) && !currentPlugins.isEmpty() )
        {
            return true;
        }

        return false;
    }

    public String getCurrentGroupId()
    {
        return currentGroupId;
    }

    public String getCurrentArtifactId()
    {
        return currentArtifactId;
    }

    public String getCurrentVersion()
    {
        return currentVersion;
    }

    public Map<String, PluginInfoForMetadata> getCurrentPlugins()
    {
        return currentPlugins;
    }

    private boolean isGroupPath( String path )
    {
        if ( StringUtils.isEmpty( currentGroupId ) )
        {
            return false;
        }
        if ( path.substring( 1 ).replace( '/', '.' ).equals( currentGroupId ) )
        {
            return true;
        }
        return false;
    }
    
    class PluginInfoForMetadata
    {
        private String name;

        private String artifactId;

        public PluginInfoForMetadata( String artifactId )
        {
            this.artifactId = artifactId;
        }

        public PluginInfoForMetadata( String artifactId, String name )
        {
            this.artifactId = artifactId;

            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public String getArtifactId()
        {
            return artifactId;
        }

        public String getPrefix()
        {
            if ( "maven-plugin-plugin".equals( artifactId ) )
            {
                return "plugin";
            }
            else
            {
                return artifactId.replaceAll( "-?maven-?", "" ).replaceAll( "-?plugin-?", "" );
            }
        }

        public String toXml()
        {
            StringBuffer xml = new StringBuffer();

            xml.append( "    <plugin>" + "\n" );

            xml.append( "      <prefix>" + getPrefix() + "</prefix>" + "\n" );

            if ( !StringUtils.isEmpty( getName() ) )
            {
                xml.append( "      <name>" + getName() + "</name>" + "\n" );
            }

            xml.append( "      <artifactid>" + getArtifactId() + "</artifactid>" + "\n" );

            xml.append( "    </plugin>\n" );

            return xml.toString();
        }

    }
}
