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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.artifact.version.VersionComparator;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.SetSnapshotOperation;
import org.apache.maven.mercury.repository.metadata.Snapshot;
import org.apache.maven.mercury.repository.metadata.SnapshotOperand;
import org.apache.maven.mercury.repository.metadata.Versioning;
import org.apache.maven.mercury.util.TimeUtil;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;

public class AbstractMetadataHelper
{

    private static final String VERSION_REGEX = "^[0-9].*$";

    /**
     * current groupId based on the current collection, if no groupId, it's null
     */
    private String currentGroupId;

    private String currentArtifactId;

    private String currentVersion;

    private Map<String, PluginInfoForMetadata> currentPlugins = new HashMap<String, PluginInfoForMetadata>();

    private List<String> currentVersions = new ArrayList<String>();

    private List<String> currentArtifacts = new ArrayList<String>();

    private boolean hasVersion;

    private boolean hasArtifact;

    private boolean hasPlugin;

    public void onDirEnter( String path )
    {
        updateGAV( path );
    }

    public void onDirExit( String path )
        throws MetadataException,
            IOException
    {

        if ( shouldCreateMetadataForSnapshotVersionDir( path ) )
        {
            createMetadataForSnapshotVersionDir( path );

            currentArtifacts.clear();
        }
        else if ( shouldCreateMetadataForArtifactDir( path ) )
        {
            createMetadataForArtifactDir( path );

            currentVersions.clear();
        }
        else if ( shouldCreateMetadataForPluginGroupDir( path ) )
        {
            createMetadataForPluginGroupDir( path );

            currentPlugins.clear();
        }

        cleanGAV( path );
    }

    public void processFile( String path )
    {
        if ( currentVersion != null && path.endsWith( "pom" ) )
        {
            currentArtifacts.add( path );

            hasArtifact = true;
            handleMavenPlugin( path );

            if ( !currentPlugins.isEmpty() )
            {
                hasPlugin = true;
            }
        }

    }

    private String getParentPath( String path )
    {
        int pos = path.lastIndexOf( '/' );

        if ( pos == -1 )
        {
            return null;
        }

        return path.substring( 0, pos );
    }

    private String getName( String path )
    {
        int pos = path.lastIndexOf( '/' );

        if ( pos == -1 )
        {
            return path;
        }

        return path.substring( pos + 1 );
    }

    private boolean inGroupIdPath( String path )
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

    private boolean inVersionPath( String path )
    {
        if ( StringUtils.isEmpty( currentVersion ) )
        {
            return false;
        }
        if ( getName( path ).equals( currentVersion ) )
        {
            return true;
        }
        return false;
    }

    private boolean inArtifactIdPath( String path )
    {
        if ( StringUtils.isEmpty( currentArtifactId ) )
        {
            return false;
        }
        if ( getName( path ).equals( currentArtifactId ) )
        {
            return true;
        }
        return false;
    }

    private void updateGAV( String path )
    {
        if ( getName( path ).matches( VERSION_REGEX ) )
        {
            currentVersions.add( getName( path ) );

            currentVersion = getName( path );

            int spaceOfGAPos = getParentPath( path ).lastIndexOf( '/' );

            if ( currentArtifactId == null )
            {
                currentArtifactId = getParentPath( path ).substring( spaceOfGAPos + 1 );

                currentGroupId = getParentPath( path ).substring( 1, spaceOfGAPos ).replace( '/', '.' );
            }

            hasVersion = true;
        }
    }

    private void cleanGAV( String path )
    {
        if ( currentVersion != null && getName( path ).equals( currentVersion ) )
        {
            currentVersion = null;
        }
        else if ( currentVersion == null && currentArtifactId != null && getName( path ).equals( currentArtifactId ) )
        {
            currentArtifactId = null;
        }
        else if ( currentArtifactId == null && currentGroupId != null && inGroupIdPath( path ) )
        {
            currentGroupId = null;
        }
    }

    private void handleMavenPlugin( String path )
    {
        Model model = null;
        try
        {
            Reader reader = ReaderFactory.newXmlReader( FileUtils.getFile( path ) );
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
            // getLogger().info( "Can't build POM model from " + pomFile.getPath(), e );
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

    private boolean shouldCreateMetadataForArtifactDir( String path )
    {
        if ( hasVersion && inArtifactIdPath( path ) )
        {
            return true;
        }
        return false;
    }

    private boolean shouldCreateMetadataForSnapshotVersionDir( String path )
    {
        if ( hasArtifact && inVersionPath( path ) && currentVersion.endsWith( "SNAPSHOT" ) )
        {
            return true;
        }
        return false;
    }

    private boolean shouldCreateMetadataForPluginGroupDir( String path )
    {
        if ( hasPlugin && inGroupIdPath( path ) )
        {
            return true;
        }

        return false;
    }

    private class PluginInfoForMetadata
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

            xml.append( "      <artifactId>" + getArtifactId() + "</artifactId>" + "\n" );

            xml.append( "    </plugin>\n" );

            return xml.toString();
        }

    }

    public void createMetadataForPluginGroupDir( String path )
    {
        // RepositoryItemUid mdUid = walker.getRepository().createUid( coll.getPath() + "/maven-metadata.xml" );

        StringBuffer md = new StringBuffer();

        md.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );

        md.append( "<metadata>\n" );

        md.append( "  <groupId>" + currentGroupId + "</groupId>\n" );

        md.append( "  <plugins>\n" );

        for ( PluginInfoForMetadata plugin : currentPlugins.values() )
        {
            md.append( plugin.toXml() );
        }

        md.append( "  </plugins>\n" );

        md.append( "</metadata>\n" );

        store( md.toString(), path );

    }

    public void createMetadataForArtifactDir( String path )
        throws MetadataException,
            IOException
    {

        // UIDs are like URIs! The separator is _always_ "/"!!!
        // RepositoryItemUid mdUid = walker.getRepository().createUid( coll.getPath() + "/maven-metadata.xml" );

        Metadata md = new Metadata();

        md.setGroupId( currentGroupId );

        md.setArtifactId( currentArtifactId );

        versioningForArtifactDir( md );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MetadataBuilder.write( md, outputStream );

        String mdString = outputStream.toString();

        outputStream.close();

        store( mdString, path );
    }

    private void versioningForArtifactDir( Metadata md )
    {
        Versioning versioning = new Versioning();

        String release = null;

        String latest = null;

        VersionComparator versionComparator = new VersionComparator();

        for ( String version : currentVersions )
        {
            versioning.addVersion( version );

            if ( latest != null && versionComparator.compare( latest, version ) < 0 )
            {
                latest = version;
            }

            if ( release != null && !version.endsWith( "SNAPSHOT" )
                && versionComparator.compare( release, version ) < 0 )
            {
                release = version;
            }

            if ( latest == null )
            {
                latest = version;

            }

            if ( release == null && !version.endsWith( "SNAPSHOT" ) )
            {
                release = version;

            }
        }

        if ( release != null )
        {
            versioning.setRelease( release );
        }

        if ( latest != null )
        {
            versioning.setLatest( latest );
        }

        versioning.setLastUpdated( TimeUtil.getUTCTimestamp() );

        md.setVersioning( versioning );
    }

    public void createMetadataForSnapshotVersionDir( String path )
        throws MetadataException,
            IOException
    {
        // RepositoryItemUid mdUid = walker.getRepository().createUid( coll.getPath() + "/maven-metadata.xml" );

        Metadata md = new Metadata();

        md.setGroupId( currentGroupId );

        md.setArtifactId( currentArtifactId );

        md.setVersion( currentVersion );

        versioningForSnapshotVersionDir( md );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MetadataBuilder.write( md, outputStream );

        String mdString = outputStream.toString();

        outputStream.close();

        store( mdString, path );
    }

    private void versioningForSnapshotVersionDir( Metadata md )
        throws MetadataException
    {
        Versioning versioning = new Versioning();

        versioning.setLastUpdated( TimeUtil.getUTCTimestamp() );

        md.setVersioning( versioning );

        Snapshot snapshot = new Snapshot();

        snapshot.setLocalCopy( false );

        snapshot.setBuildNumber( 1 );

        for ( String artifact : currentArtifacts )
        {
            int lastHyphenPos = artifact.lastIndexOf( '-' );

            int buildNumber = Integer.parseInt( artifact.substring( lastHyphenPos + 1, artifact.length() - 4 ) );

            if ( buildNumber > snapshot.getBuildNumber() )
            {
                snapshot.setBuildNumber( buildNumber );

                String timeStamp = artifact.substring( ( md.getArtifactId() + '-' + md.getVersion() + '-' ).length()
                    - "-SNAPSHOT".length(), lastHyphenPos );

                snapshot.setTimestamp( timeStamp );

            }
        }

        MetadataBuilder.changeMetadata( md, new SetSnapshotOperation( new SnapshotOperand( snapshot ) ) );

    }

    public void store( String metadata, String path )
    {

    }
}
