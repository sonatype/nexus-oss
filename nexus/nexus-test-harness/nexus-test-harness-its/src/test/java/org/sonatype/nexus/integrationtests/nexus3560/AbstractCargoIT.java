package org.sonatype.nexus.integrationtests.nexus3560;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.plugin.nexus2810.PluginConsoleMessageUtil;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;
import org.sonatype.nexus.rest.model.LogsListResource;
import org.sonatype.nexus.rest.model.LogsListResourceResponse;
import org.sonatype.nexus.test.utils.NexusStatusUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public abstract class AbstractCargoIT
{

    private InstalledLocalContainer container;

    public AbstractCargoIT()
    {
        super();
    }

    public abstract String getContainer();

    public abstract File getContainerLocation();

    public File getWarFile()
    {
        return new File( "target/nexus/war" );
    }

    @BeforeClass
    public void startContainer()
    {
        WAR war = new WAR( getWarFile().getAbsolutePath() );
        war.setContext( "nexus" );

        ConfigurationFactory configurationFactory = new DefaultConfigurationFactory();
        LocalConfiguration configuration =
            (LocalConfiguration) configurationFactory.createConfiguration( getContainer(), ContainerType.INSTALLED,
                ConfigurationType.STANDALONE );
        configuration.addDeployable( war );
        configuration.setProperty( ServletPropertySet.PORT, TestProperties.getString( "nexus.application.port" ) );

        container =
            (InstalledLocalContainer) new DefaultContainerFactory().createContainer( getContainer(),
                ContainerType.INSTALLED, configuration );
        container.setHome( getContainerLocation().getAbsolutePath() );

        container.start();

        TestContainer.getInstance().getTestContext().setSecureTest( true );
        TestContainer.getInstance().getTestContext().useAdminForRequests();
    }

    @AfterClass( alwaysRun = true )
    public void stopContainer()
    {
        if ( container != null )
        {
            try
            {
                container.stop();
            }
            catch ( org.codehaus.cargo.container.ContainerException e )
            {
                // ignore it
            }
        }
    }

    @Test
    public void checkStatus()
        throws Exception
    {
        assertEquals( new NexusStatusUtil().getNexusStatus().getData().getState(), "STARTED" );
    }

    protected PluginConsoleMessageUtil pluginConsoleMsgUtil = new PluginConsoleMessageUtil();

    @Test( dependsOnMethods = { "checkStatus", "checkLogs" } )
    public void checkPlugins()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        List<PluginInfoDTO> pluginInfos = pluginConsoleMsgUtil.listPluginInfos();

        assertNotNull( pluginInfos );
        assertFalse( pluginInfos.isEmpty() );

        for ( PluginInfoDTO info : pluginInfos )
        {
            assertEquals( info.getStatus(), "ACTIVATED" );
        }
    }

    @Test( dependsOnMethods = { "checkStatus" } )
    public void checkLogs()
        throws Exception
    {
        Response response = RequestFacade.sendMessage( "service/local/logs", Method.GET );
        String responseText = response.getEntity().getText();

        Assert.assertEquals( response.getStatus().getCode(), 200, "Status: \n" + responseText );

        LogsListResourceResponse logListResponse =
            (LogsListResourceResponse) XStreamFactory.getXmlXStream().fromXML( responseText );
        List<LogsListResource> logList = logListResponse.getData();
        Assert.assertTrue( logList.size() > 0, "Log List should contain at least 1 log." );

        for ( Iterator<LogsListResource> iter = logList.iterator(); iter.hasNext(); )
        {
            LogsListResource logResource = iter.next();

            this.downloadAndConfirmLog( logResource.getResourceURI(), logResource.getName() );
        }
    }

    private void downloadAndConfirmLog( String logURI, String name )
        throws Exception
    {
        Response response = RequestFacade.sendMessage( new URL( logURI ), Method.GET, null );
        Assert.assertEquals( response.getStatus().getCode(), 200, "Request URI: " + logURI + " Status: " );
        InputStream stream = response.getEntity().getStream();
        if ( stream == null )
        {
            Assert.fail( "Stream was null: " + response.getEntity().getText() );
        }

        // get the first 10000 chars from the downloaded log
        InputStreamReader reader = new InputStreamReader( stream );
        BufferedReader bReader = new BufferedReader( reader );

        StringBuffer downloadedLog = new StringBuffer();

        int lineCount = 10000;
        while ( bReader.ready() && lineCount-- > 0 )
        {
            downloadedLog.append( (char) bReader.read() );
        }

        String log = downloadedLog.toString();

        assertThat( log, not( containsString( "error" ) ) );
        assertThat( log, not( containsString( "exception" ) ) );
    }

}