package org.sonatype.nexus.selenium.nexus421;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Method;
import org.sonatype.nexus.mock.MockEvent;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.ServerTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.model.GlobalConfigurationResourceResponse;
import org.testng.annotations.Test;

@Component( role = Nexus421NotificationConfigTest.class )
public class Nexus421NotificationConfigTest
    extends SeleniumTest
{

    @Test
    public void systemNotificationFields()
    {
        doLogin();

        ServerTab serverCfg = main.openServer();
        
        serverCfg.getNotificationsEnabled().check( true );
        serverCfg.getNotificationEmails().type( "someemail@someemail.com,otheremail@otheremail.com" );
        serverCfg.getNotificationRoles().add( "anonymous" );
        serverCfg.getNotificationRoles().add( "admin" );
        
        MockListener ml = MockHelper.listen( "/global_settings/current", new MockListener()
        {
            @Override
            public void onPayload( Object payload, MockEvent evt )
            {
                if ( !Method.PUT.equals( evt.getMethod() ) )
                {
                    evt.block();
                }
                assertThat( payload, not( nullValue() ) );
                
                GlobalConfigurationResourceResponse resource = ( GlobalConfigurationResourceResponse ) payload;
                
                assertThat( resource.getData().getSystemNotificationSettings(), not( nullValue() ) );
                assertThat( resource.getData().getSystemNotificationSettings().isEnabled(), equalTo( true ) );
                assertThat( resource.getData().getSystemNotificationSettings().getEmailAddresses(), equalTo( "someemail@someemail.com,otheremail@otheremail.com" ) );
                assertThat( resource.getData().getSystemNotificationSettings().getRoles().size(), equalTo( 2 ) );
                assertThat( resource.getData().getSystemNotificationSettings().getRoles().get( 0 ), equalTo( "anonymous" ) );
                assertThat( resource.getData().getSystemNotificationSettings().getRoles().get( 1 ), equalTo( "admin" ) );
            }
        } );
        
        serverCfg.save();
        
        ml.getResult();
        
        MockHelper.checkAssertions();
        MockHelper.clearMocks();
        
    }
}
