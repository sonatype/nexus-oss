/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.selenium.nexus421;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.testng.Assert.assertNotNull;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Method;
import org.sonatype.nexus.mock.MockEvent;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.ServerTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.global.GlobalConfigurationPlexusResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationResourceResponse;
import org.testng.annotations.Test;

@Component( role = Nexus421NotificationConfigTest.class )
public class Nexus421NotificationConfigTest
    extends SeleniumTest
{

    @Test
    public void systemNotificationFields()
        throws Exception
    {
        doLogin();

        ServerTab serverCfg = main.openServer();

        serverCfg.getNotificationsEnabled().check( true );
        serverCfg.getNotificationEmails().type( "someemail@someemail.com,otheremail@otheremail.com" );
        serverCfg.getNotificationRoles().add( "anonymous" );
        serverCfg.getNotificationRoles().add( "admin" );

        MockListener<GlobalConfigurationResourceResponse> ml =
            MockHelper.listen( GlobalConfigurationPlexusResource.RESOURCE_URI,
                new MockListener<GlobalConfigurationResourceResponse>()
                {
                    @Override
                    public void onPayload( Object payload, MockEvent evt )
                    {
                        if ( !Method.PUT.equals( evt.getMethod() ) )
                        {
                            evt.block();
                        }
                        else
                        {
                            assertThat( payload, not( nullValue() ) );
                            assertThat( payload, is( instanceOf( GlobalConfigurationResourceResponse.class ) ) );

                            GlobalConfigurationResourceResponse resource =
                                (GlobalConfigurationResourceResponse) payload;

                            assertThat( resource.getData().getSystemNotificationSettings(), not( nullValue() ) );
                            assertThat( resource.getData().getSystemNotificationSettings().getEmailAddresses(),
                                equalTo( "someemail@someemail.com,otheremail@otheremail.com" ) );
                            assertThat( resource.getData().getSystemNotificationSettings().getRoles().size(),
                                equalTo( 2 ) );
                            assertThat( resource.getData().getSystemNotificationSettings().getRoles().get( 0 ),
                                equalTo( "anonymous" ) );
                            assertThat( resource.getData().getSystemNotificationSettings().getRoles().get( 1 ),
                                equalTo( "admin" ) );
                            assertThat( resource.getData().getSystemNotificationSettings().isEnabled(), equalTo( true ) );
                        }
                    }
                } );

        serverCfg.save();
        Thread.sleep( 1000 );

        assertNotNull( ml.waitForPayload( GlobalConfigurationResourceResponse.class ) );

        MockHelper.checkAndClean();

    }
}
