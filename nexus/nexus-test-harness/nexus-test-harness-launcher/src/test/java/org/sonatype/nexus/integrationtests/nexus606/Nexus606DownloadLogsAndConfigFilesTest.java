package org.sonatype.nexus.integrationtests.nexus606;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.ConfigurationsListResource;
import org.sonatype.nexus.rest.model.ConfigurationsListResourceResponse;
import org.sonatype.nexus.rest.model.LogsListResource;
import org.sonatype.nexus.rest.model.LogsListResourceResponse;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.NexusConfigUtil;

import com.thoughtworks.xstream.XStream;

public class Nexus606DownloadLogsAndConfigFilesTest
    extends AbstractNexusIntegrationTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void getLogsTest()
        throws IOException
    {

        Response response = RequestFacade.sendMessage( "service/local/logs", Method.GET );
        String responseText = response.getEntity().getText();

        Assert.assertEquals( "Status: \n" + responseText, 200, response.getStatus().getCode() );

        LogsListResourceResponse logListResponse = (LogsListResourceResponse) new XStream().fromXML( responseText );
        List<LogsListResource> logList = logListResponse.getData();
        Assert.assertTrue( "Log List should contain at least 1 log.", logList.size() > 0 );

        for ( Iterator<LogsListResource> iter = logList.iterator(); iter.hasNext(); )
        {
            LogsListResource logResource = iter.next();

            // check the contents of each log now...
            this.downloadAndConfirmLog( logResource.getResourceURI(), logResource.getName() );
        }
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void getConfigsTest()
        throws IOException
    {

        Response response = RequestFacade.sendMessage( "service/local/configs", Method.GET );
        String responseText = response.getEntity().getText();

        Assert.assertEquals( "Status: \n" + responseText, 200, response.getStatus().getCode() );

        ConfigurationsListResourceResponse logListResponse =
            (ConfigurationsListResourceResponse) new XStream().fromXML( responseText );
        List<ConfigurationsListResource> configList = logListResponse.getData();
        Assert.assertTrue( "Config List should contain  2 config file: "+ configList, configList.size() == 2 );

        Assert.assertNotNull( "Default Config", this.getConfigFromList(configList, "default") );
        
        ConfigurationsListResource configResource = this.getConfigFromList(configList, "current");
        Assert.assertNotNull( "Current Config", configResource );

        // check the config now...
        response = RequestFacade.sendMessage( new URL( configResource.getResourceURI() ), Method.GET, null );
        Assert.assertEquals( "Status: ", 200, response.getStatus().getCode() );

        
        String sha1Expected = FileTestingUtils.createSHA1FromStream( response.getEntity().getStream() );
        String sha1Actual = FileTestingUtils.createSHA1FromFile( NexusConfigUtil.getNexusFile() );

        Assert.assertEquals( "SHA1 of log files do not match: ", sha1Expected, sha1Actual );
    }

    private void downloadAndConfirmLog( String logURI, String name )
        throws MalformedURLException, IOException
    {
        Response response = RequestFacade.sendMessage( new URL( logURI ), Method.GET, null );
        Assert.assertEquals( "Status: ", 200, response.getStatus().getCode() );

        // now get the real log file and compare
        String logDir = NexusConfigUtil.getNexusConfig().getApplicationLogDirectory();
        File logFile = new File( logDir, name );

        String sha1Expected = FileTestingUtils.createSHA1FromStream( response.getEntity().getStream()  );
        String sha1Actual = FileTestingUtils.createSHA1FromFile( logFile );

        Assert.assertEquals( "SHA1 of log files do not match: ", sha1Expected, sha1Actual );
    }
    
    private ConfigurationsListResource getConfigFromList( List<ConfigurationsListResource> configList, String name )
    {
        
        for ( Iterator<ConfigurationsListResource> iter = configList.iterator(); iter.hasNext(); )
        {
            ConfigurationsListResource configurationsListResource = iter.next();
            
            if( configurationsListResource.getName().equals( name ))
            {
                return configurationsListResource;
            }
        }
        return null;
    }
    

}
