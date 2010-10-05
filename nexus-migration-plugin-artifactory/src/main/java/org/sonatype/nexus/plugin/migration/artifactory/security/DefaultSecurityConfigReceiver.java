/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.LinkedHashSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.plugin.migration.artifactory.ArtifactoryMigrationException;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.target.Target;
import org.sonatype.nexus.proxy.target.TargetRegistry;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.realms.tools.ConfigurationManager;

@Component( role = SecurityConfigReceiver.class )
public class DefaultSecurityConfigReceiver
    implements SecurityConfigReceiver
{

    @Requirement
    private TargetRegistry targetRegistry;

    @Requirement
    private NexusConfiguration nexusConfiguration;

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager manager;

    public void receiveRepositoryTarget( CRepositoryTarget repoTarget )
        throws ArtifactoryMigrationException
    {
        try
        {
            ContentClass cc = repositoryTypeRegistry.getContentClasses().get( repoTarget.getContentClass() );

            if ( cc == null )
            {
                throw new ConfigurationException( "Content class with ID=\"" + repoTarget.getContentClass()
                    + "\" does not exists!" );
            }

            Target target = new Target( repoTarget.getId(), repoTarget.getName(), cc, repoTarget.getPatterns() );
            targetRegistry.addRepositoryTarget( target );
            nexusConfiguration.saveConfiguration();
        }
        catch ( Exception e )
        {
            throw new ArtifactoryMigrationException( "Cannot create repository target with id " + repoTarget.getId(), e );
        }
    }

    public void receiveSecurityPrivilege( CPrivilege privilege )
        throws ArtifactoryMigrationException
    {
        try
        {
            // nexusSecurity.createPrivilege( privilege );

            manager.createPrivilege( privilege );

            manager.save();
        }
        catch ( InvalidConfigurationException e )
        {
            throw new ArtifactoryMigrationException( "Cannot create privilege with name " + privilege.getName(), e );
        }

    }

    public void receiveSecurityRole( CRole role )
        throws ArtifactoryMigrationException
    {
        try
        {
            manager.createRole( role );

            manager.save();
        }
        catch ( InvalidConfigurationException e )
        {
            throw new ArtifactoryMigrationException( "Cannot create role with id " + role.getId(), e );
        }

    }

    public void receiveSecurityUser( CUser user, CUserRoleMapping map )
        throws ArtifactoryMigrationException
    {
        try
        {
            manager.createUser( user, new LinkedHashSet<String>( map.getRoles() ) );

            manager.save();
        }
        catch ( InvalidConfigurationException e )
        {
            throw new ArtifactoryMigrationException( "Cannot create user with id " + user.getId(), e );
        }

    }

}
