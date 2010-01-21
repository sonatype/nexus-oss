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
package org.sonatype.nexus.proxy.maven.metadata;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.mercury.repository.metadata.Plugin;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;

/**
 * a Maven metadata helper containing all the logic for creating maven-metadata.xml <br/>
 * and logic for creating md5 and sh1 checksum files
 * 
 * @author Juven Xu
 */
abstract public class AbstractMetadataHelper
{
    static final String MD5_SUFFIX = ".md5";

    static final String SHA1_SUFFIX = ".sha1";

    static final String METADATA_SUFFIX = "/maven-metadata.xml";

    static final String APPROPRIATE_GAV_PATTERN = "^[\\d\\w\\.-]*$";

    protected Logger logger;

    // key is the path where need to create g md, value is a collection of plugins
    Map<String, Collection<Plugin>> gData = new HashMap<String, Collection<Plugin>>();

    // key is the path where need to create ga md, value is a collection of versions
    Map<String, Collection<String>> gaData = new HashMap<String, Collection<String>>();

    // key is the path where need to create gav md, value is a collection of path names
    Map<String, Collection<String>> gavData = new HashMap<String, Collection<String>>();

    private Collection<AbstractMetadataProcessor> metadataProcessors;

    public AbstractMetadataHelper( Logger logger )
    {
        this.logger = logger;

        // here the order matters
        metadataProcessors = new ArrayList<AbstractMetadataProcessor>( 3 );

        metadataProcessors.add( new VersionDirMetadataProcessor( this ) );

        metadataProcessors.add( new ArtifactDirMetadataProcessor( this ) );

        metadataProcessors.add( new GroupDirMetadataProcessor( this ) );

        metadataProcessors.add( new ObsoleteMetadataProcessor( this ) );
    }

    public void onDirEnter( String path )
        throws Exception
    {
        // do nothing
    }

    public void onDirExit( String path )
        throws Exception
    {

        for ( AbstractMetadataProcessor metadataProcessor : metadataProcessors )
        {
            if ( metadataProcessor.process( path ) )
            {
                break;
            }
        }

    }

    public void processFile( String path )
        throws Exception
    {
        // remove rotten checksum
        if ( isObsoleteChecksum( path ) )
        {
            remove( path );

            return;
        }

        rebuildChecksum( path );

        if ( path.endsWith( "pom" ) )
        {
            updateMavenInfo( path );
        }
    }

    private boolean isObsoleteChecksum( String path )
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

    protected void updateMavenInfo( String path )
        throws Exception
    {
        Reader reader = ReaderFactory.newXmlReader( retrieveContent( path ) );

        MavenXpp3Reader xpp3 = new MavenXpp3Reader();

        Model model = null;

        try
        {
            model = xpp3.read( reader );
        }
        catch ( Exception e )
        {
            throw new Exception( "Unable to parse POM model from '" + path + "'.", e );
        }
        finally
        {
            reader.close();

            reader = null;
        }

        // groupId, artifactId, version, artifactName
        String g, a, v, n;

        g = model.getGroupId() == null ? model.getParent().getGroupId() : model.getGroupId();
        a = model.getArtifactId();
        v = model.getVersion() == null ? model.getParent().getVersion() : model.getVersion();
        n = path.substring( path.lastIndexOf( '/' ) + 1 );

        // if the pom could not provide good values, parse GAV from path
        if ( isInpropriateValue( g ) || isInpropriateValue( a ) || isInpropriateValue( v ) )
        {
            try
            {
                Gav gav = getGavCalculator().pathToGav( path );

                if ( isInpropriateValue( g ) )
                {
                    g = gav.getGroupId();
                }

                if ( isInpropriateValue( a ) )
                {
                    a = gav.getArtifactId();
                }

                if ( isInpropriateValue( v ) )
                {
                    v = gav.getBaseVersion();
                }
            }
            catch ( IllegalArtifactCoordinateException e )
            {
                logger.warn( "Unable to parse good GAV values. Path: '" + path + "'. GAV: '" + g + ":" + a + ":" + v
                    + "'" );
            }
        }
        

        // GA
        String gaPath = "/" + g.replace( '.', '/' ) + "/" + a;

        if ( gaData.get( gaPath ) == null )
        {
            gaData.put( gaPath, new ArrayList<String>() );
        }

        gaData.get( gaPath ).add( v );

        // GAV
        if ( v.endsWith( "SNAPSHOT" ) )
        {
            String gavPath = "/" + g.replace( '.', '/' ) + "/" + a + "/" + v;

            if ( gavData.get( gavPath ) == null )
            {
                gavData.put( gavPath, new ArrayList<String>() );
            }

            gavData.get( gavPath ).add( n );
        }

        // G
        if ( model.getPackaging().equals( "maven-plugin" ) )
        {
            Plugin plugin = new Plugin();

            plugin.setArtifactId( a );

            plugin.setPrefix( getPluginPrefix( a ) );

            if ( !StringUtils.isEmpty( model.getName() ) )
            {
                plugin.setName( model.getName() );
            }

            String gPath = "/" + g.replace( '.', '/' );

            if ( gData.get( gPath ) == null )
            {
                gData.put( gPath, new ArrayList<Plugin>() );
            }

            gData.get( gPath ).add( plugin );
        }
    }

    private boolean isInpropriateValue( String value )
    {
        if ( StringUtils.isEmpty( value ) )
        {
            return true;
        }
        if ( !value.matches( APPROPRIATE_GAV_PATTERN ) )
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

    void rebuildChecksum( String path )
        throws Exception
    {
        if ( !exists( path ) )
        {
            if ( exists( path + MD5_SUFFIX ) )
            {
                remove( path + MD5_SUFFIX );
            }
            if ( exists( path + SHA1_SUFFIX ) )
            {
                remove( path + SHA1_SUFFIX );
            }

            return;
        }

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
    
    abstract protected GavCalculator getGavCalculator();

}
