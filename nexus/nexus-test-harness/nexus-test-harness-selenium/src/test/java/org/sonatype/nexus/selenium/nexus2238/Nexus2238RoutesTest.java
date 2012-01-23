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
package org.sonatype.nexus.selenium.nexus2238;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
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
