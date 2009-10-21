package org.sonatype.nexus.selenium.nexus2203;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.nexus.selenium.util.NxAssert.disabled;
import static org.sonatype.nexus.selenium.util.NxAssert.requiredField;
import static org.sonatype.nexus.selenium.util.NxAssert.valueEqualsTo;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Status;
import org.sonatype.nexus.mock.MockEvent;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.LogConfigTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.model.LogConfigResource;
import org.sonatype.nexus.rest.model.LogConfigResourceResponse;
import org.testng.annotations.Test;

@Component( role = Nexus2203LogConfigureTest.class )
public class Nexus2203LogConfigureTest
    extends SeleniumTest
{

    @Test
    public void erroValidation()
        throws InterruptedException
    {
        doLogin();

        LogConfigTab logs = main.openLogsConfig();

        // disabled( logs.getRootLoggerLevel() );
        disabled( logs.getRootLoggerAppenders() );
        // disabled( logs.getFileAppenderPattern() );
        disabled( logs.getFileAppenderLocation() );

        requiredField( logs.getFileAppenderPattern(), "%4d{yyyy-MM-dd HH\\:mm\\:ss} %-5p [%-15.15t] - %c - %m%n" );

        logs.cancel();
    }

    @Test
    public void checkConfiguration()
        throws InterruptedException
    {
        doLogin();

        String fileAppenderLocation = "${plexus.nexus-work}/logs/nexus.log";
        String fileAppenderPattern = "%-5p [%-15.15t] - %c - %m%n";
        String rootLoggerAppenders = "console, logfile";
        String rootLoggerLevel = "DEBUG";

        LogConfigResourceResponse result = new LogConfigResourceResponse();
        LogConfigResource data = new LogConfigResource();
        data.setFileAppenderLocation( fileAppenderLocation );
        data.setFileAppenderPattern( fileAppenderPattern );
        data.setRootLoggerAppenders( rootLoggerAppenders );
        data.setRootLoggerLevel( rootLoggerLevel );
        result.setData( data );

        MockHelper.expect( "/log/config", new MockResponse( Status.SUCCESS_OK, result ) );

        LogConfigTab logs = main.openLogsConfig();
        valueEqualsTo( logs.getRootLoggerLevel(), rootLoggerLevel );
        valueEqualsTo( logs.getRootLoggerAppenders(), rootLoggerAppenders );
        valueEqualsTo( logs.getFileAppenderPattern(), fileAppenderPattern );
        valueEqualsTo( logs.getFileAppenderLocation(), fileAppenderLocation );

        MockHelper.checkAndClean();
    }

    @Test
    public void saveConfiguration()
    {
        doLogin();

        LogConfigTab logs = main.openLogsConfig();

        // save
        final String newLevel = "INFO";
        final String newPattern = "%4d{yyyy-MM-dd HH\\:mm\\:ss} %-5p [%-15.15t] - %c - %m%n";
        logs.getRootLoggerLevel().setValue( newLevel );
        logs.getFileAppenderPattern().type( newPattern );

        MockHelper.listen( "/log/config", new MockListener()
        {
            @Override
            protected void onPayload( Object payload, MockEvent evt )
            {
                assertThat( payload, is( LogConfigResourceResponse.class ) );
                LogConfigResource data = ( (LogConfigResourceResponse) payload ).getData();
                assertThat( data.getRootLoggerLevel(), equalTo( newLevel ) );
                assertThat( data.getFileAppenderPattern(), equalTo( newPattern ) );
            }

            @Override
            protected void onResult( Object result, MockEvent evt )
            {
                assertThat( result, is( LogConfigResourceResponse.class ) );
                LogConfigResource data = ( (LogConfigResourceResponse) result ).getData();
                assertThat( data.getRootLoggerLevel(), equalTo( newLevel ) );
                assertThat( data.getFileAppenderPattern(), equalTo( newPattern ) );
            }
        } );

        logs.save();

        MockHelper.checkAndClean();
        valueEqualsTo( logs.getRootLoggerLevel(), newLevel.replace( "\\", "" ) );
        valueEqualsTo( logs.getFileAppenderPattern(), newPattern.replace( "\\", "" ) );

        logs.cancel();

    }

}
