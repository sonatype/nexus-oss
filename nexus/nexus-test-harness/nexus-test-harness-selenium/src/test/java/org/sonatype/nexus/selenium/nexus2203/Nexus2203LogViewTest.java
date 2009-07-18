package org.sonatype.nexus.selenium.nexus2203;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.LogConfigTab;
import org.sonatype.nexus.mock.pages.LogsViewTab;
import org.sonatype.nexus.selenium.nexus1815.LoginTest;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus2203LogViewTest.class )
public class Nexus2203LogViewTest
    extends SeleniumTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void contentLoading()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        LogsViewTab logs = main.openViewLogs();
        String content = logs.getContent();
        assertThat( content, anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) );

        logs.selectFile( "nexus.log" );
        content = logs.getContent();
        assertThat( content, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) ) );
        logs.getReload().click();
        content = logs.getContent();
        assertThat( content, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) ) );

        logs.selectFile( "nexus.xml" );
        String nexusXmlContent = logs.getContent();
        assertThat( nexusXmlContent, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ),
                                                        equalTo( content ) ) ) );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void tail()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        LogsViewTab logs = main.openViewLogs();
        String content = logs.getContent();
        assertThat( content, anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) );

        logs.selectFile( "nexus.log" );
        content = logs.getContent();
        assertThat( content, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) ) );

        Thread.sleep( 1000 );

        logs.getTailUpdate().click();

        String contentReloaded = logs.getContent();
        assertThat( contentReloaded, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) ) );
        Assert.assertTrue( contentReloaded.startsWith( content ) );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void checkConfiguration()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        LogConfigTab logs = main.openLogsConfig();
        assertThat( logs.getRootLoggerLevel().getValue(), equalTo( "DEBUG" ) );
        assertThat( logs.getRootLoggerAppenders().getValue(), equalTo( "eelogfile" ) );
        assertThat( logs.getFileAppenderPattern().getValue(), anyOf( nullValue(), equalTo( "" ),
                                                                            equalTo( "null" ) ) );
        assertThat( logs.getFileAppenderLocation().getValue(), anyOf( nullValue(), equalTo( "" ),
                                                                             equalTo( "null" ) ) );

        // TODO missing mock

        logs.save();

        logs.cancel();
    }

}
