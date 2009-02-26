package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.nexus.plugin.migration.artifactory.MigrationResult;
import org.sonatype.nexus.plugin.migration.artifactory.persist.MappingConfiguration;
import org.sonatype.nexus.plugin.migration.artifactory.security.DefaultSecurityConfigConvertor.TargetSuite;

public class SecurityConfigConvertorRequest
{
    private final ArtifactorySecurityConfig config;

    private final SecurityConfigReceiver persistor;

    private final MappingConfiguration mappingConfiguration;

    private final MigrationResult migrationResult;

    // by default, resolve artifactory permissions
    private boolean resolvePermission = true;

    // converted users
    private List<SecurityUser> users = new ArrayList<SecurityUser>();

    // Mapping between the target id and target suite
    private Map<String, TargetSuite> mapping = new HashMap<String, TargetSuite>();

    public SecurityConfigConvertorRequest( ArtifactorySecurityConfig config, SecurityConfigReceiver persistor,
        MappingConfiguration mappingConfiguration, MigrationResult migrationResult )
    {
        this.config = config;

        this.persistor = persistor;

        this.mappingConfiguration = mappingConfiguration;

        this.migrationResult = migrationResult;
    }

    public ArtifactorySecurityConfig getConfig()
    {
        return config;
    }

    public SecurityConfigReceiver getPersistor()
    {
        return persistor;
    }

    public MappingConfiguration getMappingConfiguration()
    {
        return mappingConfiguration;
    }

    public MigrationResult getMigrationResult()
    {
        return migrationResult;
    }

    public boolean isResolvePermission()
    {
        return resolvePermission;
    }

    public void setResolvePermission( boolean resolvePermission )
    {
        this.resolvePermission = resolvePermission;
    }

    public List<SecurityUser> getMigratedUsers()
    {
        return users;
    }

    public Map<String, TargetSuite> getMapping()
    {
        return mapping;
    }

}
