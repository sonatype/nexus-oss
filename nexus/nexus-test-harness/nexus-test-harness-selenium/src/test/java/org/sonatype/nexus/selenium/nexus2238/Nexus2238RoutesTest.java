package org.sonatype.nexus.selenium.nexus2238;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RotesTab;
import org.sonatype.nexus.mock.pages.RouteForm;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.rest.model.RepositoryRouteResourceResponse;
import org.sonatype.nexus.selenium.nexus1815.LoginTest;
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
        LoginTest.doLogin( main );

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
        LoginTest.doLogin( main );

        RotesTab routes = main.openRoutes();

        // create
        String pattern = ".*";
        String ruleType = "Exclusive";
        String groupId = "*";
        String repo = "central";

        MockListener ml = MockHelper.listen( "/repo_routes", new MockListener()
        {
            @Override
            public void onPayload( Object payload )
            {
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
