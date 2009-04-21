package org.sonatype.jsecurity.realms.url;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserManager;
import org.sonatype.jsecurity.locators.users.PlexusUserSearchCriteria;


public class URLUserLocatorTest
    extends PlexusTestCase
{

    public void testBasics() throws Exception
    {
        
        PlexusUserManager userManager = this.lookup( PlexusUserManager.class );
        
        PlexusUser user = userManager.getUser( "ANYBODY" );
        Assert.assertNotNull( user );
        Assert.assertEquals( "url", user.getSource() );
        
        Assert.assertNotNull( userManager.getUser( "RANDOM", "url" ) );
        
        Assert.assertEquals( 1, userManager.searchUsers( new PlexusUserSearchCriteria("abcd"), "url" ).size() );
        
        
    }
    
}
