/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.api.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( value = "userRequest" )
public class LdapUserRequest
{

    private LdapUserRequestDTO data;

    /**
     * @return the ldapUserRoleMapping
     */
    public LdapUserRequestDTO getLdapUserRequestDto()
    {
        return data;
    }

    /**
     * @param ldapUserRoleMapping the ldapUserRoleMapping to set
     */
    public void setLdapUserRequestDto( LdapUserRequestDTO ldapUserRoleMapping )
    {
        this.data = ldapUserRoleMapping;
    }
    
    
}
