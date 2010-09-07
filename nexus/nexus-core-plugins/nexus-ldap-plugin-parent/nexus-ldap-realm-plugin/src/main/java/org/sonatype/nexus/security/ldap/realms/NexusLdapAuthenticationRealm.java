/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms;

import org.codehaus.plexus.component.annotations.Component;
import org.jsecurity.realm.Realm;
import org.sonatype.security.ldap.realms.AbstractLdapAuthenticatingRealm;

@Component( role = Realm.class, hint = NexusLdapAuthenticationRealm.ROLE, description = "OSS LDAP Authentication Realm")
public class NexusLdapAuthenticationRealm
    extends AbstractLdapAuthenticatingRealm
{

    public static final String ROLE = "NexusLdapAuthenticationRealm";
    
    @Override
    public String getName()
    {
        return ROLE;
    }

}
