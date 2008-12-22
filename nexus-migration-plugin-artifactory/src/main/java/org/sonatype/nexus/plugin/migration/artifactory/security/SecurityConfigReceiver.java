package org.sonatype.nexus.plugin.migration.artifactory.security;

import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.plugin.migration.artifactory.ArtifactoryMigrationException;

public interface SecurityConfigReceiver
{
    void receiveRepositoryTarget( CRepositoryTarget repoTarget )
        throws ArtifactoryMigrationException;

    void receiveSecurityPrivilege( SecurityPrivilege privilege )
        throws ArtifactoryMigrationException;

    void receiveSecurityRole( SecurityRole role )
        throws ArtifactoryMigrationException;

    void receiveSecurityUser( SecurityUser user )
        throws ArtifactoryMigrationException;
}
