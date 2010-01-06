/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.api.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( value = "connectionInfo" )
public class LdapConnectionInfoResponse
{

    LdapConnectionInfoDTO data;

    /**
     * @return the connectionInfo
     */
    public LdapConnectionInfoDTO getData()
    {
        return data;
    }

    /**
     * @param connectionInfo the connectionInfo to set
     */
    public void setData( LdapConnectionInfoDTO connectionInfo )
    {
        this.data = connectionInfo;
    }
    
    
    
}
