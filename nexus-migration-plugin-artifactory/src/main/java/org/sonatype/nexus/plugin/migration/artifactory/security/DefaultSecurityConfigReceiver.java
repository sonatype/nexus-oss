package org.sonatype.nexus.plugin.migration.artifactory.security;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.jsecurity.NexusSecurity;
import org.sonatype.nexus.plugin.migration.artifactory.ArtifactoryMigrationException;
import org.sonatype.nexus.plugin.migration.artifactory.persist.MappingConfiguration;

@Component( role = SecurityConfigReceiver.class )
public class DefaultSecurityConfigReceiver
    implements SecurityConfigReceiver
{

    @Requirement
    private NexusSecurity nexusSecurity;

    @Requirement
    private Nexus nexus;

    @Requirement( role = MappingConfiguration.class, hint = "default" )
    private MappingConfiguration mapping;
    
    public void receiveRepositoryTarget( CRepositoryTarget repoTarget )
        throws ArtifactoryMigrationException
    {
        try
        {
            nexus.createRepositoryTarget( repoTarget );
        }
        catch ( Exception e )
        {
            throw new ArtifactoryMigrationException( "Cannot create repository target with id " + repoTarget.getId(), e );
        }

    }

    public void receiveSecurityPrivilege( SecurityPrivilege privilege )
        throws ArtifactoryMigrationException
    {
        try
        {
            nexusSecurity.createPrivilege( privilege );
        }
        catch ( InvalidConfigurationException e )
        {
            throw new ArtifactoryMigrationException( "Cannot create privilege with name " + privilege.getName(), e );
        }

    }

    public void receiveSecurityRole( SecurityRole role )
        throws ArtifactoryMigrationException
    {
        try
        {
            nexusSecurity.createRole( role );
        }
        catch ( InvalidConfigurationException e )
        {
            throw new ArtifactoryMigrationException( "Cannot create role with id " + role.getId(), e );
        }

    }

    public void receiveSecurityUser( SecurityUser user )
        throws ArtifactoryMigrationException
    {
        try
        {
            nexusSecurity.createUser( user );
        }
        catch ( InvalidConfigurationException e )
        {
            throw new ArtifactoryMigrationException( "Cannot create user with id " + user.getId(), e );
        }

    }

}
