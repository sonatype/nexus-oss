package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;

public class NexusConfigUtil
{

    public static Configuration getNexusConfig()
        throws IOException
    {

        URL configURL = new URL( TestProperties.getString( "nexus.base.url" ) + "service/local/configs/current" );

        Reader fr = null;
        Configuration configuration = null;

        try
        {
            NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();

            fr = new InputStreamReader( configURL.openStream() );

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

}
