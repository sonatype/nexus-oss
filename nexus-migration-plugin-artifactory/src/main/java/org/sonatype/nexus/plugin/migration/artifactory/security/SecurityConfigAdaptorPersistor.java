package org.sonatype.nexus.plugin.migration.artifactory.security;

import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;

public interface SecurityConfigAdaptorPersistor
{

    void persistRepositoryTarget( CRepositoryTarget repoTarget );

    void persistSecurityPrivilege( SecurityPrivilege privilege );

    void persistSecurityRole( SecurityRole role );

    void persistSecurityUser( SecurityUser user );

}
