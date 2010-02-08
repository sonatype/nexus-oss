/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.test.api.dto;

import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( value = "userAndGroupConfigTest" )
@XmlRootElement( name = "userAndGroupConfigTest" )
public class LdapUserAndGroupConfigTestRequest
{
    private LdapUserAndGroupConfigTestRequestDTO data;

    /**
     * @return the data
     */
    public LdapUserAndGroupConfigTestRequestDTO getData()
    {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData( LdapUserAndGroupConfigTestRequestDTO data )
    {
        this.data = data;
    }
}
