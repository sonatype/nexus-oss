package org.sonatype.nexus.integrationtests;

import java.util.HashMap;

public class TestContext
{

    private boolean secureTest = false;

    private String username = "admin";

    private String password = "admin123";

    private HashMap<String, Object> map = new HashMap<String, Object>();

    public Object getObject( String key )
    {
        return map.get( key );
    }

    public boolean getBoolean( String key )
    {
        if ( map.containsKey( key ) )
        {
            return (Boolean) map.get( key );
        }

        return false;
    }

    public void put( String key, Object value )
    {
        this.map.put( key, value );
    }

    public boolean isSecureTest()
    {
        return secureTest;
    }

    public void setSecureTest( boolean secureTest )
    {
        this.secureTest = secureTest;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

}
