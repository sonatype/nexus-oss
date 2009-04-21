package org.sonatype.jsecurity.realms.url;

import java.io.File;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;
import org.sonatype.jsecurity.locators.users.PlexusUserManager;
import org.sonatype.jsecurity.locators.users.PlexusUserSearchCriteria;

public class URLRealmConfiguredUserTest
    extends PlexusTestCase
{

    private String securityXmlPath = "./target/plexus-home/" + this.getClass().getSimpleName() + "/security.xml";

    public void testURLRealmConfiguredUser()
        throws Exception
    {
        PlexusUserManager userManager = this.lookup( PlexusUserManager.class );
        PlexusUserLocator urlLocator = this.lookup( PlexusUserLocator.class, "url" );
        PlexusUserLocator configuredUsersLocator = this.lookup( PlexusUserLocator.class, "allConfigured" );

        // try to get a normal user to make sure the search is working
        Assert.assertEquals( 1, configuredUsersLocator.searchUsers( new PlexusUserSearchCriteria( "user1" ) ).size() );

        // make sure we get the URL realm user from this search
        Assert.assertEquals( 1, configuredUsersLocator.searchUsers( new PlexusUserSearchCriteria( "url-user" ) ).size() );

        // do the search from the URL realm
        Assert.assertEquals( 1, urlLocator.searchUsers( new PlexusUserSearchCriteria( "url-user" ) ).size() );

        // do the search using the user manager.
        Assert.assertEquals( 1, userManager.searchUsers( new PlexusUserSearchCriteria( "url-user" ), "all" ).size() );

        // the list should contain a single user
        Assert.assertTrue( urlLocator.listUserIds().contains( "url-user" ) );
        Assert.assertEquals( 1, urlLocator.listUserIds().size() );
    }

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );
        context.put( "security-xml-file", this.securityXmlPath );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // copy security.xml file into place
        FileUtils.copyFile( new File( "./target/test-classes/configuredUser-security.xml" ), new File(
            this.securityXmlPath ) );
    }

}
