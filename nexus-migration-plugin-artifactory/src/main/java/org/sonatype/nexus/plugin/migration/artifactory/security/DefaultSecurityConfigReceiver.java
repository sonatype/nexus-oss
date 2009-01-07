/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.jsecurity.NexusSecurity;
import org.sonatype.nexus.plugin.migration.artifactory.ArtifactoryMigrationException;

@Component( role = SecurityConfigReceiver.class )
public class DefaultSecurityConfigReceiver
    implements SecurityConfigReceiver
{

/*    @Requirement
    private NexusSecurity nexusSecurity;*/

    @Requirement
    private Nexus nexus;
    
    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager manager;

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
            //nexusSecurity.createPrivilege( privilege );
            
            manager.createPrivilege( privilege );
            
            manager.save();
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
            manager.createRole( role );
            
            manager.save();
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
            manager.createUser( user );
            
            manager.save();
        }
        catch ( InvalidConfigurationException e )
        {
            throw new ArtifactoryMigrationException( "Cannot create user with id " + user.getId(), e );
        }

    }

}
