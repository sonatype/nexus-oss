package org.sonatype.nexus.selenium.nexus2203;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.LogConfigTab;
import org.sonatype.nexus.mock.pages.LogsViewTab;
import org.sonatype.nexus.selenium.nexus1815.LoginTest;

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
        Assert.assertThat( content, anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) );

        logs.selectFile( "nexus.log" );
        content = logs.getContent();
        Assert.assertThat( content, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) ) );
        logs.getReload().click();
        content = logs.getContent();
        Assert.assertThat( content, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) ) );

        logs.selectFile( "nexus.xml" );
        String nexusXmlContent = logs.getContent();
        Assert.assertThat( nexusXmlContent, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ),
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
        Assert.assertThat( content, anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) );

        logs.selectFile( "nexus.log" );
        content = logs.getContent();
        Assert.assertThat( content, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) ) );

        Thread.sleep( 1000 );

        logs.getTailUpdate().click();

        String contentReloaded = logs.getContent();
        Assert.assertThat( contentReloaded, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) ) );
        Assert.assertTrue( contentReloaded.startsWith( content ) );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void checkConfiguration()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        LogConfigTab logs = main.openLogsConfig();
        Assert.assertThat( logs.getRootLoggerLevel().getValue(), equalTo( "DEBUG" ) );
        Assert.assertThat( logs.getRootLoggerAppenders().getValue(), equalTo( "eelogfile" ) );
        Assert.assertThat( logs.getFileAppenderPattern().getValue(), anyOf( nullValue(), equalTo( "" ),
                                                                            equalTo( "null" ) ) );
        Assert.assertThat( logs.getFileAppenderLocation().getValue(), anyOf( nullValue(), equalTo( "" ),
                                                                             equalTo( "null" ) ) );

        // TODO missing mock

        logs.save();

        logs.cancel();
    }

}
