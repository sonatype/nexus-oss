package org.sonatype.security.ldap.realms.persist;

import org.sonatype.plexus.appevents.AbstractEvent;

public class LdapClearCacheEvent extends AbstractEvent<Object>
{

    public LdapClearCacheEvent( Object component )
    {
        super( component );
    }

}
