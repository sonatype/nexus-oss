/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.api;

import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoResponse;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationResponse;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserListResponse;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserResponseDTO;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapAuthenticationTestRequest;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapUserAndGroupConfigTestRequest;
import org.sonatype.plexus.rest.xstream.AliasingListConverter;

import com.thoughtworks.xstream.XStream;

public class XStreamInitalizer
{

    public XStream initXStream( XStream xstream )
    {
        
        xstream.processAnnotations( LdapConnectionInfoResponse.class );
        xstream.processAnnotations( LdapUserAndGroupConfigurationResponse.class );
        xstream.processAnnotations( LdapUserListResponse.class );
        xstream.processAnnotations( LdapAuthenticationTestRequest.class );
        xstream.processAnnotations( LdapUserAndGroupConfigTestRequest.class );

      xstream.registerLocalConverter( LdapUserListResponse.class, "data", new AliasingListConverter(
          LdapUserResponseDTO.class, "user" ) );

        return xstream;
    }
    
}
