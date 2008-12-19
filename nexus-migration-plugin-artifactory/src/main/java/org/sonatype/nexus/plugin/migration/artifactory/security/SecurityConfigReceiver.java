package org.sonatype.nexus.plugin.migration.artifactory.security;

import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;

public interface SecurityConfigReceiver
{
    void receiveRepositoryTarget( CRepositoryTarget repoTarget );

    void receiveSecurityPrivilege( SecurityPrivilege privilege );

    void receiveSecurityRole( SecurityRole role );

    void receiveSecurityUser( SecurityUser user );
}
