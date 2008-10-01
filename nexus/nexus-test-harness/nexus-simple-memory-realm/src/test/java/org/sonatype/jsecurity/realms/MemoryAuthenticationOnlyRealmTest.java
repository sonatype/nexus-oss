package org.sonatype.jsecurity.realms;

import org.codehaus.plexus.PlexusTestCase;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.realm.Realm;

public class MemoryAuthenticationOnlyRealmTest
    extends PlexusTestCase
{
    private MemoryAuthenticationOnlyRealm realm;
         
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        realm = ( MemoryAuthenticationOnlyRealm ) lookup( Realm.class, "MemoryAuthenticationOnlyRealm" );
    }
    
    public void testSuccessfulAuthentication()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordToken( "admin", "admin321" );
        
        AuthenticationInfo ai = realm.getAuthenticationInfo( upToken );
        
        String password = ( String ) ai.getCredentials();
        
        assertEquals( "admin321", password );        
    }
    
    public void testFailedAuthentication()
        throws Exception
    {
        UsernamePasswordToken upToken = new UsernamePasswordToken( "admin", "admin123" );
        
        try
        {
            realm.getAuthenticationInfo( upToken );
            
            fail( "Authentication should have failed" );
        }
        catch( AuthenticationException e )
        {
            // good
        }   
    }
}
