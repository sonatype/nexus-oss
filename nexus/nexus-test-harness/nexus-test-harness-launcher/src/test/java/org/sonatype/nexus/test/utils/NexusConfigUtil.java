package org.sonatype.nexus.test.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

public class NexusConfigUtil
{

    public static Configuration getNexusConfig()
        throws IOException
    {

//        URL configURL = new URL( TestProperties.getString( "nexus.base.url" ) + "service/local/configs/current" );

        Reader fr = null;
        Configuration configuration = null;

        try
        {
            NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();

            fr = new FileReader( getNexusFile() );

            // read again with interpolation
            configuration = reader.read( fr );

        }
        catch ( XmlPullParserException e )
        {
            Assert.fail( "could not parse nexus.xml: " + e.getMessage() );
        }
        finally
        {
            if ( fr != null )
            {
                fr.close();
            }
        }
        return configuration;
    }

    private static void saveConfig( Configuration config )
        throws IOException
    {
        Writer fw = null;
        try
        {
            FileOutputStream fos =
                new FileOutputStream( getNexusFile() );
            fw = new OutputStreamWriter( fos );

            NexusConfigurationXpp3Writer writer = new NexusConfigurationXpp3Writer();

            writer.write( fw, config );
        }
        finally
        {
            if ( fw != null )
            {
                fw.flush();
                fw.close();
            }
        }
    }
    
    private static File getNexusFile()
    {
        return new File(TestProperties.getString( "nexus.base.dir" ) + "/"
                        + AbstractNexusIntegrationTest.RELATIVE_CONF_DIR, "nexus.xml");
    }

    @SuppressWarnings( "unchecked" )
    public static CGroupsSettingPathMappingItem getRoute( String id )
        throws IOException
    {
        List<CGroupsSettingPathMappingItem> routes = getNexusConfig().getRepositoryGrouping().getPathMappings();

        for ( Iterator<CGroupsSettingPathMappingItem> iter = routes.iterator(); iter.hasNext(); )
        {
            CGroupsSettingPathMappingItem groupsSettingPathMappingItem = iter.next();

            if ( groupsSettingPathMappingItem.getId().equals( id ) )
            {
                return groupsSettingPathMappingItem;
            }

        }
        return null;
    }

    public static void enableSecurity( boolean enabled )
        throws IOException
    {
        Configuration config = getNexusConfig();

        if ( config.getSecurity().isEnabled() != enabled )
        {
            config.getSecurity().setEnabled( enabled );
            
            // save it
            saveConfig( config );
        }

    }

}
