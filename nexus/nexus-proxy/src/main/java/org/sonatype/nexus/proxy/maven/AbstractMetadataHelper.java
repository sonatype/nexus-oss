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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.mercury.repository.metadata.AddVersionOperation;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.apache.maven.mercury.repository.metadata.MetadataException;
import org.apache.maven.mercury.repository.metadata.MetadataOperation;
import org.apache.maven.mercury.repository.metadata.Plugin;
import org.apache.maven.mercury.repository.metadata.SetSnapshotOperation;
import org.apache.maven.mercury.repository.metadata.StringOperand;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;

/**
 * a Maven metadata helper containing all the logic for creating maven-metadata.xml <br/>
 * and logic for creating md5 and sh1 checksum files
 * 
 * @author Juven Xu
 */
abstract public class AbstractMetadataHelper
{
    private static final String MD5_SUFFIX = ".md5";

    private static final String SHA1_SUFFIX = ".sha1";

    private static final String METADATA_SUFFIX = "/maven-metadata.xml";

    /**
     * current groupId based on the current collection, if no groupId, it's null
     */
    protected String currentGroupId;

    protected String currentArtifactId;

    protected String currentVersion;

    protected Map<String, Plugin> currentPlugins = new HashMap<String, Plugin>();

    protected List<String> currentVersions = new ArrayList<String>();

    protected List<String> currentArtifacts = new ArrayList<String>();

    public void onDirEnter( String path )
    {
        // do nothing
    }

    public void onDirExit( String path )
    {
        try
        {
            if ( shouldCreateMetadataForSnapshotVersionDir( path ) )
            {
                createMetadataForSnapshotVersionDir( path );

                rebuildChecksum( path + METADATA_SUFFIX );

                currentArtifacts.clear();
            }
            else if ( shouldCreateMetadataForArtifactDir( path ) )
            {
                createMetadataForArtifactDir( path );

                rebuildChecksum( path + METADATA_SUFFIX );

                currentVersions.clear();
            }
            else if ( shouldCreateMetadataForPluginGroupDir( path ) )
            {
                createMetadataForPluginGroupDir( path );

                rebuildChecksum( path + METADATA_SUFFIX );

                currentPlugins.clear();
            }

            cleanGAV( path );
        }
        catch ( Exception e )
        {
            // TODO: add error info to system error feeds
        }
    }

    public void processFile( String path )
    {
        try
        {
            // remove old metadata files
            if ( isMavenMetadataFile( path ) )
            {
                remove( path );

                return;
            }
            // remove rotten checksum
            if ( isRottenChecksum( path ) )
            {
                remove( path );

                return;
            }

            if ( path.endsWith( "pom" ) )
            {
                updateMavenInfo( path );
            }

            rebuildChecksum( path );
        }
        catch ( Exception e )
        {
            // TODO: add error info to system error feeds
        }
    }

    private boolean isRottenChecksum( String path )
        throws Exception
    {
        if ( !isChecksumFile( path ) )
        {
            return false;
        }

        String originalPath = path.substring( 0, path.lastIndexOf( '.' ) );

        if ( exists( originalPath ) )
        {
            return false;
        }

        return true;
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

    private void cleanGAV( String path )
    {
        if ( currentVersion != null )
        {
            currentArtifacts.clear();
        }

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

    protected void updateMavenInfo( String path )
    {
        Model model = null;
        try
        {
            Reader reader = ReaderFactory.newXmlReader( retrieveContent( path ) );

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
            // skip
            return;
        }

        if ( model == null )
        {
            return;
        }

        currentArtifactId = model.getArtifactId();

        if ( !StringUtils.isEmpty( model.getGroupId() ) )
        {
            currentGroupId = model.getGroupId();
        }
        else
        {
            currentGroupId = model.getParent().getGroupId();
        }

        if ( !StringUtils.isEmpty( model.getVersion() ) )
        {
            currentVersion = model.getVersion();

            currentVersions.add( model.getVersion() );
        }
        else
        {
            currentVersion = model.getParent().getVersion();

            currentVersions.add( model.getParent().getVersion() );
        }

        currentArtifacts.add( path );

        if ( model.getPackaging().equals( "maven-plugin" ) )
        {
            Plugin plugin = new Plugin();

            plugin.setArtifactId( model.getArtifactId() );

            plugin.setPrefix( getPluginPrefix( model.getArtifactId() ) );

            if ( !StringUtils.isEmpty( model.getName() ) )
            {
                plugin.setName( model.getName() );
            }

            currentPlugins.put( model.getArtifactId(), plugin );
        }
    }

    private boolean shouldCreateMetadataForArtifactDir( String path )
    {
        if ( !currentVersions.isEmpty() && inArtifactIdPath( path ) )
        {
            return true;
        }
        return false;
    }

    private boolean shouldCreateMetadataForSnapshotVersionDir( String path )
    {
        if ( !currentArtifacts.isEmpty() && inVersionPath( path ) && currentVersion.endsWith( "SNAPSHOT" ) )
        {
            return true;
        }
        return false;
    }

    private boolean shouldCreateMetadataForPluginGroupDir( String path )
    {
        if ( !currentPlugins.isEmpty() && inGroupIdPath( path ) )
        {
            return true;
        }

        return false;
    }

    private String getPluginPrefix( String artifactId )
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

    public void createMetadataForPluginGroupDir( String path )
        throws Exception
    {
        Metadata md = new Metadata();

        for ( Plugin plugin : currentPlugins.values() )
        {
            md.addPlugin( plugin );
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MetadataBuilder.write( md, outputStream );

        String mdString = outputStream.toString();

        outputStream.close();

        store( mdString, path + METADATA_SUFFIX );
    }

    public void createMetadataForArtifactDir( String path )
        throws Exception
    {
        Metadata md = new Metadata();

        md.setGroupId( currentGroupId );

        md.setArtifactId( currentArtifactId );

        versioningForArtifactDir( md, currentVersions );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MetadataBuilder.write( md, outputStream );

        String mdString = outputStream.toString();

        outputStream.close();

        store( mdString, path + METADATA_SUFFIX );
    }

    protected void versioningForArtifactDir( Metadata metadata, List<String> versions )
        throws MetadataException
    {
        List<MetadataOperation> ops = new ArrayList<MetadataOperation>();

        for ( String version : versions )
        {
            ops.add( new AddVersionOperation( new StringOperand( version ) ) );
        }

        MetadataBuilder.changeMetadata( metadata, ops );
    }

    public void createMetadataForSnapshotVersionDir( String path )
        throws Exception
    {
        Metadata md = new Metadata();

        md.setGroupId( currentGroupId );

        md.setArtifactId( currentArtifactId );

        md.setVersion( currentVersion );

        versioningForSnapshotVersionDir( md, currentArtifacts );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MetadataBuilder.write( md, outputStream );

        String mdString = outputStream.toString();

        outputStream.close();

        store( mdString, path + METADATA_SUFFIX );
    }

    protected void versioningForSnapshotVersionDir( Metadata metadata, List<String> artifacts )
        throws MetadataException
    {
        List<MetadataOperation> ops = new ArrayList<MetadataOperation>();

        for ( String artifact : artifacts )
        {
            ops.add( new SetSnapshotOperation( new StringOperand( getName( artifact ) ) ) );
        }

        MetadataBuilder.changeMetadata( metadata, ops );
    }

    private void rebuildChecksum( String path )
        throws Exception
    {
        if ( !shouldBuildChecksum( path ) )
        {
            return;
        }

        store( buildMd5( path ), path + MD5_SUFFIX );

        store( buildSh1( path ), path + SHA1_SUFFIX );
    }

    protected boolean shouldBuildChecksum( String path )
    {
        if ( isChecksumFile( path ) )
        {
            return false;
        }

        return true;
    }

    protected boolean isChecksumFile( String path )
    {
        if ( getName( path ).endsWith( MD5_SUFFIX ) || getName( path ).endsWith( SHA1_SUFFIX ) )
        {
            return true;
        }
        return false;
    }

    protected boolean isMavenMetadataFile( String path )
    {
        if ( getName( path ).endsWith( METADATA_SUFFIX.substring( 1 ) ) )
        {
            return true;
        }
        return false;
    }

    abstract public String buildMd5( String path )
        throws Exception;

    abstract public String buildSh1( String path )
        throws Exception;

    /**
     * Store the content to the file of the path
     * 
     * @param metadata
     * @param path
     */
    abstract public void store( String content, String path )
        throws Exception;

    /**
     * Remove the file of the path
     * 
     * @param path
     * @throws Exception
     */
    abstract public void remove( String path )
        throws Exception;

    /**
     * Retrieve the content according to the path
     * 
     * @param path
     * @return
     */
    abstract public InputStream retrieveContent( String path )
        throws Exception;

    /**
     * Check if the file or item of this path exists
     * 
     * @param path
     * @return
     * @throws Exception
     */
    abstract public boolean exists( String path )
        throws Exception;

}
