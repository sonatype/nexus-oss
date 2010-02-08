/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.test.api.dto;

import javax.xml.bind.annotation.XmlRootElement;

import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( value = "connectionInfoTest" )
@XmlRootElement( name = "connectionInfoTest" )
public class LdapAuthenticationTestRequest
{
    private LdapConnectionInfoDTO data;

    /**
     * @return the ldapConnectionInfoDTO
     */
    public LdapConnectionInfoDTO getData()
    {
        return data;
    }

    /**
     * @param ldapConnectionInfoDTO the ldapConnectionInfoDTO to set
     */
    public void setData( LdapConnectionInfoDTO ldapConnectionInfoDTO )
    {
        this.data = ldapConnectionInfoDTO;
    }

}
