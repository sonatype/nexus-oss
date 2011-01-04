/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.selenium.nexus421;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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
                            assertThat( payload, is( GlobalConfigurationResourceResponse.class ) );

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
