/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao;

import java.util.HashSet;
import java.util.Set;

public class LdapUser
implements Comparable<LdapUser>
{
    
    private String username;
    
    private String realName;
    
    private String email;
    
    private String website;
    
    private String password;
    
    private Set<String> membership = new HashSet<String>();
    
    private String dn;

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public String getRealName()
    {
        return realName;
    }

    public void setRealName( String realName )
    {
        this.realName = realName;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public Set<String> getMembership()
    {
        return membership;
    }

    public void setMembership( Set<String> membership )
    {
        this.membership = membership;
    }

    public String getWebsite()
    {
        return website;
    }

    public void setWebsite( String website )
    {
        this.website = website;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }
    
    public String getDn()
    {
        return dn;
    }

    public void setDn( String dn )
    {
        this.dn = dn;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append( "User:" );
        buffer.append( "\n\tUsername: " ).append( username );
        buffer.append( "\n\tDN: " ).append( dn );
        buffer.append( "\n\tReal Name: " ).append( realName );
        buffer.append( "\n\tEmail: " ).append( email );
        buffer.append( "\n\tWebsite: " ).append( website );
        
        if(getMembership() != null && !getMembership().isEmpty())
        {
            buffer.append( "\n\tMembership: " ).append( getMembership() );
        }
        
        return buffer.toString();
    }

    public int compareTo( LdapUser o )
    {
        if ( o == null )
            return 1;
        
        return this.getUsername().compareTo( o.getUsername() );
    }

}
