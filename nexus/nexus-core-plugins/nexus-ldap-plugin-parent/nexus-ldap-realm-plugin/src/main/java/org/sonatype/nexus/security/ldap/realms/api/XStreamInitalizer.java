/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.security.ldap.realms.api;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import org.apache.commons.lang.StringEscapeUtils;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoResponse;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationResponse;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserListResponse;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserResponseDTO;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapAuthenticationTestRequest;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapUserAndGroupConfigTestRequest;
import org.sonatype.plexus.rest.xstream.AliasingListConverter;

public class XStreamInitalizer
{

    public XStream initXStream( XStream xstream )
    {
        
        xstream.processAnnotations( LdapConnectionInfoResponse.class );
        xstream.processAnnotations( LdapUserAndGroupConfigurationResponse.class );
        xstream.processAnnotations( LdapUserListResponse.class );
        xstream.processAnnotations( LdapAuthenticationTestRequest.class );
        xstream.processAnnotations( LdapUserAndGroupConfigTestRequest.class );

        // NXCM-2974 unescape html entities like "o=org&amp;org", they get escaped by nexus-rest-api json->DTO
        // conversion
        final StringConverter converter = new StringConverter()
        {
            @Override
            public Object fromString( String str )
            {
                return StringEscapeUtils.unescapeHtml( str );
            }
        };

        xstream.registerLocalConverter( LdapConnectionInfoDTO.class, "systemUsername", converter );
        xstream.registerLocalConverter( LdapConnectionInfoDTO.class, "systemPassword", converter );
        xstream.registerLocalConverter( LdapConnectionInfoDTO.class, "searchBase", converter );
        xstream.registerLocalConverter( LdapUserAndGroupConfigurationDTO.class, "groupBaseDn", converter );
        xstream.registerLocalConverter( LdapUserAndGroupConfigurationDTO.class, "userBaseDn", converter );

        xstream.registerLocalConverter( LdapUserListResponse.class, "data", new AliasingListConverter(
          LdapUserResponseDTO.class, "user" ) );

        return xstream;
    }

}
