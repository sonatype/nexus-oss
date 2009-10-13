package org.sonatype.nexus.selenium.nexus2209;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.codehaus.plexus.component.annotations.Component;
import org.hamcrest.text.StringContains;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.NexusMockTestCase;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.PrivilegeConfigurationForm;
import org.sonatype.nexus.mock.pages.PrivilegesTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.selenium.util.NxAssert;
import org.sonatype.security.rest.model.PrivilegeListResourceResponse;
import org.sonatype.security.rest.model.PrivilegeStatusResource;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus2209PrivilegesTest.class )
public class Nexus2209PrivilegesTest
    extends SeleniumTest
{

    private static final String NAME = "privName";

    private static final String DESCRIPTION = "privDescription";

    @Test
    public void errorMessages()
        throws InterruptedException
    {
        doLogin();

        PrivilegeConfigurationForm privs = main.openPrivileges().addPrivilege();

        NxAssert.requiredField( privs.getName(), "selpriv" );
        NxAssert.requiredField( privs.getDescription(), "selpriv" );

        privs.save();
        NxAssert.hasErrorText( privs.getRepoTarget(), "Repository Target is required." );

        privs.getRepoTarget().select( 0 );
        NxAssert.noErrorText( privs.getRepoTarget() );

        privs.cancel();
    }

    @Test
    public void privsCrud()
        throws InterruptedException
    {
        doLogin();

        PrivilegesTab privs = main.openPrivileges();

        MockListener ml = MockHelper.listen( "/privileges_target", new MockListener()
        {
            @Override
            public void onPayload( Object payload )
            {
                System.out.println( payload );
                Assert.assertNotNull( payload );
            }
        } );

        // create
        String name = "privName";
        String description = "privDescription";
        privs.addPrivilege().populate( name, description, 0 ).save();

        PrivilegeListResourceResponse result = (PrivilegeListResourceResponse) ml.getResult();
        assertNotNull( result );

        String[] ids = new String[result.getData().size()];
        for ( int i = 0; i < ids.length; i++ )
        {
            PrivilegeStatusResource p = result.getData().get( i );
            ids[i] = NexusMockTestCase.nexusBaseURL + "service/local/privileges/" + p.getId();
        }

        // read
        for ( int i = 0; i < ids.length; i++ )
        {
            privs.refresh();
            String id = ids[i];

            assertTrue( "id: " + id + " is not present ", privs.getGrid().contains( id ) );

            PrivilegeConfigurationForm priv = privs.select( id );

            PrivilegeStatusResource data = result.getData().get( i );

            assertThat( data.getName(), StringContains.containsString( name ) );
            NxAssert.valueEqualsTo( priv.getName(), data.getName() );
            NxAssert.valueEqualsTo( priv.getDescription(), description );
        }
        privs.refresh();

        // no update

        // delete
        for ( int i = 0; i < ids.length; i++ )
        {
            privs.refresh();
            String id = ids[i];
            privs.select( id );
            privs.delete().clickYes();

            privs.refresh();

            assertFalse( "id: " + id + " is present ", privs.getGrid().contains( id ) );
        }
    }

    @Test
    public void privsRepo()
        throws InterruptedException
    {
        doLogin();

        PrivilegesTab privs = main.openPrivileges();

        MockListener ml = MockHelper.listen( "/privileges_target", new MockListener()
        {
            @Override
            public void onPayload( Object payload )
            {
                Assert.assertNotNull( payload );
            }
        } );

        // create
        String name = "privName";
        String description = "privDescription";
        PrivilegeConfigurationForm priv =
            privs.addPrivilege().populate( name, description, "repo_central", "1" ).save();

        PrivilegeListResourceResponse result = (PrivilegeListResourceResponse) ml.getResult();
        assertNotNull( result );

        for ( PrivilegeStatusResource p : result.getData() )
        {
            String id = getId( p );
            privs.select( id );

            NxAssert.valueEqualsTo( priv.getRepositoryId(), "Maven Central" );
            NxAssert.valueNull( priv.getRepositoryGroupId() );

            privs.delete().clickYes();

            assertFalse( "id: " + id + " still present ", privs.getGrid().contains( id ) );
        }
    }

    @Test
    public void privsGroup()
        throws InterruptedException
    {
        doLogin();

        PrivilegesTab privs = main.openPrivileges();

        MockListener ml = listen();

        PrivilegeConfigurationForm priv =
            privs.addPrivilege().populate( NAME, DESCRIPTION, "group_public", "1" ).save();

        PrivilegeListResourceResponse result = (PrivilegeListResourceResponse) ml.getResult();

        for ( PrivilegeStatusResource p : result.getData() )
        {
            String id = getId( p );
            privs.select( id );

            NxAssert.valueEqualsTo( priv.getRepositoryGroupId(), "Public Repositories" );
            NxAssert.valueNull( priv.getRepositoryId() );

            privs.delete().clickYes();

            assertFalse( "id: " + id + " still present ", privs.getGrid().contains( id ) );
        }

    }

    @Test
    public void targetFiltering()
        throws InterruptedException
    {
        doLogin();

        PrivilegesTab privs = main.openPrivileges();

        PrivilegeConfigurationForm priv = privs.addPrivilege();

        priv.getRepositoryOrGroup().select( 0 );
        Assert.assertEquals( new Integer( 4 ), priv.getRepoTarget().getCount() );
        priv.getRepositoryOrGroup().setValue( "repo_central" );
        Assert.assertEquals( new Integer( 3 ), priv.getRepoTarget().getCount() );
        priv.getRepositoryOrGroup().setValue( "repo_central-m1" );
        Assert.assertEquals( new Integer( 1 ), priv.getRepoTarget().getCount() );

        priv.cancel();
    }

    private String getId( PrivilegeStatusResource p )
    {
        String id = NexusMockTestCase.nexusBaseURL + "service/local/privileges/" + p.getId();
        return id;
    }

    private MockListener listen()
    {
        MockListener ml = MockHelper.listen( "/privileges_target", new MockListener()
        {
            @Override
            public void onPayload( Object payload )
            {
                System.out.println( payload );
                Assert.assertNotNull( payload );
            }
        } );
        return ml;
    }
}
