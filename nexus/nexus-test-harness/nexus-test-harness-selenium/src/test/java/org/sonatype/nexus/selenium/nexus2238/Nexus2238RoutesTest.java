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
package org.sonatype.nexus.selenium.nexus2238;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Method;
import org.sonatype.nexus.mock.MockEvent;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RotesTab;
import org.sonatype.nexus.mock.pages.RouteForm;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.model.RepositoryRouteResourceResponse;
import org.sonatype.nexus.selenium.util.NxAssert;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus2238RoutesTest.class )
public class Nexus2238RoutesTest
    extends SeleniumTest
{

    @Test
    public void errorMessages()
        throws InterruptedException
    {
        doLogin();

        RouteForm route = main.openRoutes().addRoute();

        NxAssert.requiredField( route.getPattern(), ".*" );
        NxAssert.requiredField( route.getRuleType(), "Exclusive" );
        NxAssert.requiredField( route.getRepositoriesGroup(), 0 );

        route.save();
        NxAssert.hasErrorText( route.getRepositoriesOrder(), "Select one or more items" );

        route.getRepositoriesOrder().add( "central" );
        NxAssert.noErrorText( route.getRepositoriesOrder() );

        route.cancel();
    }

    @Test
    public void routesCRUD()
        throws InterruptedException
    {
        doLogin();

        RotesTab routes = main.openRoutes();

        // create
        String pattern = ".*";
        String ruleType = "Exclusive";
        String groupId = "*";
        String repo = "central";

        MockListener ml = MockHelper.listen( "/repo_routes", new MockListener()
        {
            @Override
            public void onPayload( Object payload, MockEvent evt )
            {
                if ( !Method.POST.equals( evt.getMethod() ) )
                {
                    evt.block();
                }
                assertThat( payload, not( nullValue() ) );
            }
        } );

        routes.addRoute().populate( pattern, ruleType, groupId, repo ).save();

        RepositoryRouteResourceResponse result = (RepositoryRouteResourceResponse) ml.getResult();
        String routeId = nexusBaseURL + "service/local/repo_routes/" + result.getData().getId();

        MockHelper.checkAssertions();
        MockHelper.clearMocks();

        routes.refresh();

        Assert.assertTrue( routes.getGrid().contains( routeId ) );
        routes.refresh();

        // read
        RouteForm target = routes.select( routeId );
        NxAssert.valueEqualsTo( target.getPattern(), pattern );
        NxAssert.valueEqualsTo( target.getRuleType(), ruleType );
        NxAssert.valueEqualsTo( target.getRepositoriesGroup(), groupId );
        NxAssert.contains( target.getRepositoriesOrder(), repo );

        routes.refresh();

        // update
        String newPattern = ".*/selenium/.*";

        target = routes.select( routeId );
        target.getPattern().type( newPattern );
        target.save();

        routes.refresh();
        target = routes.select( routeId );
        NxAssert.valueEqualsTo( target.getPattern(), newPattern );

        routes.refresh();

        // delete
        routes.select( routeId );
        routes.delete().clickYes();
        routes.refresh();

        Assert.assertFalse( routes.getGrid().contains( routeId ) );
    }
}
