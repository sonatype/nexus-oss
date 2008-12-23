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
package org.sonatype.nexus.integrationtests.nexus606;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
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

/**
 * Tests downloading of log and config files.
 */
public class Nexus606DownloadLogsAndConfigFilesTest
    extends AbstractNexusIntegrationTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void getLogsTest()
        throws Exception
    {

        Response response = RequestFacade.sendMessage( "service/local/logs", Method.GET );
        String responseText = response.getEntity().getText();

        Assert.assertEquals( "Status: \n" + responseText, 200, response.getStatus().getCode() );

        LogsListResourceResponse logListResponse = (LogsListResourceResponse) this.getXMLXStream().fromXML( responseText );
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
            (ConfigurationsListResourceResponse) this.getXMLXStream().fromXML( responseText );
        List<ConfigurationsListResource> configList = logListResponse.getData();
        Assert.assertTrue( "Config List should contain at least 2 config file: "+ configList, configList.size() >= 2 );

        ConfigurationsListResource nexusXmlConfigResource = getConfigFromList(configList, "nexus.xml");
        Assert.assertNotNull( "nexus.xml", nexusXmlConfigResource );
        
        ConfigurationsListResource securityXmlConfigResource = this.getConfigFromList(configList, "security.xml");
        Assert.assertNotNull( "security.xml", securityXmlConfigResource );

        // check the config now...
        response = RequestFacade.sendMessage( new URL( nexusXmlConfigResource.getResourceURI() ), Method.GET, null );
        Assert.assertEquals( "Status: ", 200, response.getStatus().getCode() );

        
        String sha1Expected = FileTestingUtils.createSHA1FromStream( response.getEntity().getStream() );
        String sha1Actual = FileTestingUtils.createSHA1FromFile( NexusConfigUtil.getNexusFile() );

        Assert.assertEquals( "SHA1 of config files do not match: ", sha1Expected, sha1Actual );
    }

    private void downloadAndConfirmLog( String logURI, String name )
        throws Exception
    {
        Response response = RequestFacade.sendMessage( new URL( logURI ), Method.GET, null );
        Assert.assertEquals( "Request URI: "+ logURI +" Status: ", 200, response.getStatus().getCode() );
        
        File logFile = new File( nexusLogDir, name );
        
        // get the first 10000 chars from the downloaded log
        InputStreamReader reader = new InputStreamReader(response.getEntity().getStream());
        BufferedReader bReader = new BufferedReader(reader);
        
        StringBuffer downloadedLog = new StringBuffer();
        
        int lineCount = 10000;
        while(bReader.ready() && lineCount-- > 0)
        {
            downloadedLog.append( (char) bReader.read() );
        }
        String logOnDisk = FileUtils.fileRead( logFile );
        Assert.assertTrue( "Downloaded log should be similar to log file from disk.\nNOTE: its possible the file could have rolled over.\nTrying to match:\n"+ downloadedLog, logOnDisk.contains( downloadedLog ) );
    }
    
    private ConfigurationsListResource getConfigFromList( List<ConfigurationsListResource> configList, String name )
    {
        for ( ConfigurationsListResource configurationsListResource : configList )
        {
            if ( configurationsListResource.getName().equals( name ) )
            {
                return configurationsListResource;
            }
        }
        return null;
    }
    

}
