/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.api.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( value = "userList" )
@XmlRootElement( name = "userList" )
public class LdapUserListResponse
{

    private List<LdapUserResponseDTO> data = new ArrayList<LdapUserResponseDTO>();

    /**
     * @return the ldapUserRoleMappings
     */
    @XmlElementWrapper( name = "data" )
    @XmlElement( name = "user" )
    public List<LdapUserResponseDTO> getLdapUserRoleMappings()
    {
        return data;
    }

    /**
     * @param ldapUserRoleMappings the ldapUserRoleMappings to set
     */
    public void setLdapUserRoleMappings( List<LdapUserResponseDTO> ldapUserRoleMappings )
    {
        this.data = ldapUserRoleMappings;
    }

    public void addLdapUserRoleMapping( LdapUserResponseDTO ldapUserRoleMapping )
    {
        data.add( ldapUserRoleMapping );
    }
}
