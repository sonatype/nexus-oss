package org.sonatype.nexus.selenium.nexus2209;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.NexusTestCase;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.PrivilegeConfigurationForm;
import org.sonatype.nexus.mock.pages.PrivilegesTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.selenium.nexus1815.LoginTest;
import org.sonatype.nexus.selenium.util.NxAssert;
import org.sonatype.security.rest.model.PrivilegeListResourceResponse;
import org.sonatype.security.rest.model.PrivilegeStatusResource;

public class Nexus2209PrivilegesTest
    extends SeleniumTest
{

    @Test
    public void errorMessages()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

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
        LoginTest.doLogin( main );

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

        String[] ids = new String[result.getData().size()];
        for ( int i = 0; i < ids.length; i++ )
        {
            PrivilegeStatusResource p = result.getData().get( i );
            ids[i] = NexusTestCase.nexusBaseURL + "service/local/privileges/" + p.getId();
        }

        privs.refresh();

        for ( String id : ids )
        {
            Assert.assertTrue( "id: " + id + " is not present ", privs.getGrid().contains( id ) );
        }
        privs.refresh();

        // read
        PrivilegeConfigurationForm priv = privs.select( name ).selectConfiguration();
        NxAssert.valueEqualsTo( priv.getName(), name );
        NxAssert.valueEqualsTo( priv.getDescription(), description );
        NxAssert.valueEqualsTo( priv.getRepoTarget(), String.valueOf( 0 ) );

        privs.refresh();

    }
}
