package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.sonatype.nexus.plugin.migration.artifactory.MigrationResult;
import org.sonatype.nexus.plugin.migration.artifactory.persist.MappingConfiguration;
import org.sonatype.nexus.plugin.migration.artifactory.security.DefaultSecurityConfigConvertor.TargetSuite;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;

public class SecurityConfigConvertorRequest
{
    private final ArtifactorySecurityConfig config;

    private final SecurityConfigReceiver persistor;

    private final MappingConfiguration mappingConfiguration;

    private final MigrationResult migrationResult;

    // by default, resolve artifactory permissions
    private boolean resolvePermission = true;

    // converted users
    private Map<CUser, CUserRoleMapping> users = new LinkedHashMap<CUser, CUserRoleMapping>();

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

    public Map<CUser, CUserRoleMapping> getMigratedUsers()
    {
        return users;
    }

    public Map<String, TargetSuite> getMapping()
    {
        return mapping;
    }

}
