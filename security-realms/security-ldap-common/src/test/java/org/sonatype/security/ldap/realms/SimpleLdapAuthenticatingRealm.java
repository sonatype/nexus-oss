/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms;

import org.codehaus.plexus.component.annotations.Component;
import org.jsecurity.realm.Realm;

@Component( role = Realm.class, hint = SimpleLdapAuthenticatingRealm.ROLE, description = "Test Authentication LDAP Realm" )
public class SimpleLdapAuthenticatingRealm
    extends AbstractLdapAuthenticatingRealm
{

    public static final String ROLE = "LdapAuthenticatingRealm";
    
    @Override
    public String getName()
    {
        return ROLE;
    }

}
