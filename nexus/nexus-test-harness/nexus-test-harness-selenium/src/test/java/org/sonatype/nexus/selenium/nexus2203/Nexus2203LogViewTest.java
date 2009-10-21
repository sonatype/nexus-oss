package org.sonatype.nexus.selenium.nexus2203;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.LogsViewTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.plexus.rest.representation.InputStreamRepresentation;
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
        doLogin();

        LogsViewTab logs = main.openViewLogs();
        String content = logs.getContent();
        assertThat( content, anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) );

        MockListener<InputStreamRepresentation> ml =
            MockHelper.listen( "/logs/{fileName}", new MockListener<InputStreamRepresentation>() );
        logs.selectFile( "nexus.log" );
        ml.waitForResult( InputStreamRepresentation.class );
        MockHelper.checkAndClean();
        content = logs.getContent();
        assertThat( content, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) ) );
        ml = MockHelper.listen( "/logs/{fileName}", new MockListener<InputStreamRepresentation>() );
        logs.getReload().click();
        ml.waitForResult( InputStreamRepresentation.class );
        MockHelper.checkAndClean();
        content = logs.getContent();
        assertThat( content, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ) ) ) );

        ml = MockHelper.listen( "/configs/{configName}", new MockListener<InputStreamRepresentation>() );
        logs.selectFile( "nexus.xml" );
        ml.waitForResult( InputStreamRepresentation.class );
        MockHelper.checkAndClean();
        String nexusXmlContent = logs.getContent();
        assertThat( nexusXmlContent, not( anyOf( nullValue(), equalTo( "" ), equalTo( "null" ), equalTo( content ) ) ) );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void tail()
        throws InterruptedException
    {
        doLogin();

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

}
