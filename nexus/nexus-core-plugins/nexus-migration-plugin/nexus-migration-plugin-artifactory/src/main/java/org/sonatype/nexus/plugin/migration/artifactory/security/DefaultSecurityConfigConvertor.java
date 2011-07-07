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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.plugin.migration.artifactory.ArtifactoryMigrationException;
import org.sonatype.nexus.plugin.migration.artifactory.persist.model.CMapping;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;

@Component( role = SecurityConfigConvertor.class )
public class DefaultSecurityConfigConvertor
    implements SecurityConfigConvertor
{

    public void convert( SecurityConfigConvertorRequest request )
    {
        request.getMigrationResult().addInfoMessage( "Starting Artifactory Security Migration." );

        try
        {
            this.buildTargetSuites( request );
        }
        catch ( ArtifactoryMigrationException e )
        {
            request.getMigrationResult().addErrorMessage( "Failed to import Target Suites: " + e.getMessage(), e );
        }

        try
        {
            this.buildGroupRoles( request );
        }
        catch ( ArtifactoryMigrationException e )
        {
            request.getMigrationResult().addErrorMessage( "Failed to import Group Roles: " + e.getMessage(), e );
        }

        this.buildSecurityUsers( request );
    }

    private void buildGroupRoles( SecurityConfigConvertorRequest request )
        throws ArtifactoryMigrationException
    {
        for ( ArtifactoryGroup group : request.getConfig().getGroups() )
        {
            CRole role = new CRole();

            role.setId( group.getName() );

            role.setName( group.getName() );

            role.setDescription( group.getDescription() );

            role.setSessionTimeout( 60 );

            if ( request.isResolvePermission() )
            {
                List<String> subRoles = new ArrayList<String>();

                for ( ArtifactoryAcl acl : request.getConfig().getAcls() )
                {
                    if ( acl.getGroup() != null && acl.getGroup().getName().equals( group.getName() ) )
                    {
                        subRoles.addAll( getRoleListByAcl( request, acl ) );
                    }
                }
                role.setRoles( subRoles );
            }

            // nexus doesn't allow a new role without privileges and roles
            if ( role.getRoles().isEmpty() && role.getPrivileges().isEmpty() )
            {
                role.addRole( "anonymous" );
            }

            request.getPersistor().receiveSecurityRole( role );
        }
    }

    private void buildTargetSuites( SecurityConfigConvertorRequest request )
        throws ArtifactoryMigrationException
    {
        if ( !request.isResolvePermission() )
        {
            return;
        }

        for ( ArtifactoryPermissionTarget target : request.getConfig().getPermissionTargets() )
        {
            String id = target.getId();

            TargetSuite targetSuite = new TargetSuite();

            CRepositoryTarget repoTarget = buildRepositoryTarget( request, target );

            targetSuite.setRepositoryTarget( repoTarget );

            for ( String repoKey : target.getRepoKeys() )
            {
                if ( repoKey.equals( "ANY LOCAL" ) )
                {
                    for ( CMapping mapping : request.getMappingConfiguration().listMappings() )
                    {
                        if ( mapping.getArtifactoryRepositoryId().endsWith( "-local" ) )
                        {
                            buildPrivilegesAndRolesForRepository( request, target, targetSuite, mapping
                                .getArtifactoryRepositoryId() );
                        }
                    }
                }
                else if ( repoKey.equals( "ANY REMOTE" ) )
                {
                    for ( CMapping mapping : request.getMappingConfiguration().listMappings() )
                    {
                        if ( !mapping.getArtifactoryRepositoryId().endsWith( "-local" ) )
                        {
                            buildPrivilegesAndRolesForRepository( request, target, targetSuite, mapping
                                .getArtifactoryRepositoryId() );
                        }
                    }
                }
                else
                {
                    buildPrivilegesAndRolesForRepository( request, target, targetSuite, repoKey );
                }
            }

            request.getMapping().put( id, targetSuite );
        }
    }
    
    private void buildPrivilegesAndRolesForRepository( SecurityConfigConvertorRequest request,
        ArtifactoryPermissionTarget target, TargetSuite targetSuite, String repoKey )
        throws ArtifactoryMigrationException
    {
        CPrivilege createPrivilege =
            buildSecurityPrivilege( request, target, repoKey, targetSuite
            .getRepositoryTarget(), "create" );
        CPrivilege readPrivilege =
            buildSecurityPrivilege( request, target, repoKey, targetSuite
            .getRepositoryTarget(), "read" );
        CPrivilege updatePrivilege =
            buildSecurityPrivilege( request, target, repoKey, targetSuite
            .getRepositoryTarget(), "update" );
        CPrivilege deletePrivilege =
            buildSecurityPrivilege( request, target, repoKey, targetSuite
            .getRepositoryTarget(), "delete" );

        CRole readerRole = buildSecurityRoleFromPrivilege( request, target, repoKey, "reader", readPrivilege );
        CRole deployerRole =
            buildSecurityRoleFromPrivilege( request, target, repoKey, "deployer", createPrivilege, updatePrivilege );
        CRole deleteRole = buildSecurityRoleFromPrivilege( request, target, repoKey, "delete", deletePrivilege );
        CRole adminRole =
            buildSecurityRoleFromPrivilege( request, target, repoKey, "admin", updatePrivilege, deletePrivilege );

        targetSuite.addPrivilege( repoKey, createPrivilege );
        targetSuite.addPrivilege( repoKey, readPrivilege );
        targetSuite.addPrivilege( repoKey, updatePrivilege );
        targetSuite.addPrivilege( repoKey, deletePrivilege );
        targetSuite.addRole( repoKey, readerRole );
        targetSuite.addRole( repoKey, deployerRole );
        targetSuite.addRole( repoKey, deleteRole );
        targetSuite.addRole( repoKey, adminRole );
    }

    private CRepositoryTarget buildRepositoryTarget( SecurityConfigConvertorRequest request,
        ArtifactoryPermissionTarget target )
        throws ArtifactoryMigrationException
    {
        CRepositoryTarget repoTarget = new CRepositoryTarget();

        repoTarget.setId( target.getId().replace( " " , "-" ) );

        repoTarget.setName( target.getId() );

        repoTarget.setContentClass( "maven2" );

        List<String> patterns = new ArrayList<String>();

        for ( String include : target.getIncludes() )
        {
            patterns.add( include );
        }

        repoTarget.setPatterns( patterns );

        request.getPersistor().receiveRepositoryTarget( repoTarget );

        return repoTarget;
    }

    private CPrivilege buildSecurityPrivilege( SecurityConfigConvertorRequest request,
        ArtifactoryPermissionTarget permissionTarget, String repoKey, CRepositoryTarget repoTarget, String method )
        throws ArtifactoryMigrationException
    {
        CPrivilege privilege = new CPrivilege();

        privilege.setName( permissionTarget.getId() + "-" + repoKey + "-" + method );

        privilege.setDescription( permissionTarget.getId() + "-" + repoKey + "-" + method );

        privilege.setType( "target" );

        CProperty prop = new CProperty();
        prop.setKey( "method" );
        prop.setValue( method );
        privilege.addProperty( prop );

        prop = new CProperty();
        prop.setKey( "repositoryTargetId" );
        prop.setValue( repoTarget.getId() );
        privilege.addProperty( prop );

        // for creating privs with a repoTarget to all repos, set the repoId and repoGroupId to be empty
        if ( repoKey.equals( "ANY" ) )
        {
            prop = new CProperty();
            prop.setKey( "repositoryGroupId" );
            prop.setValue( "" );
            privilege.addProperty( prop );

            prop = new CProperty();
            prop.setKey( "repositoryId" );
            prop.setValue( "" );
            privilege.addProperty( prop );
        }
        else
        {
            privilege.addProperty( buildRepoIdGroupIdProperty( request, repoKey ) );
        }

        request.getPersistor().receiveSecurityPrivilege( privilege );

        return privilege;
    }

    private CProperty buildRepoIdGroupIdProperty( SecurityConfigConvertorRequest request, String repoKey )
        throws ArtifactoryMigrationException

    {
        String artiRepoKey = repoKey;

        // Here I have to hack damned Artifactory again, the repoKeys in its artifactory.config.xml and security.xml are
        // inconsistant
        if ( artiRepoKey.endsWith( "-cache" ) )
        {
            artiRepoKey = artiRepoKey.substring( 0, artiRepoKey.indexOf( "-cache" ) );
        }

        CMapping mapping = request.getMappingConfiguration().getMapping( artiRepoKey );

        if ( mapping == null )
        {
            throw new ArtifactoryMigrationException( "Cannot find the mapping repo/repoGroup id for key '"
                + artiRepoKey + "'." );
        }
        CProperty prop = new CProperty();

        if ( !StringUtils.isEmpty( mapping.getNexusGroupId() ) )
        {
            prop.setKey( "repositoryGroupId" );

            prop.setValue( mapping.getNexusGroupId() );
        }
        else if ( !StringUtils.isEmpty( mapping.getNexusRepositoryId() ) )
        {
            prop.setKey( "repositoryId" );

            prop.setValue( mapping.getNexusRepositoryId() );
        }
        else
        {
            throw new ArtifactoryMigrationException( "Cannot find the mapping repo/repoGroup id for repo key '"
                + artiRepoKey + "'." );
        }

        return prop;
    }

    private CRole buildSecurityRoleFromPrivilege( SecurityConfigConvertorRequest request,
                                                  ArtifactoryPermissionTarget target, String repoKey, String key,
                                                  CPrivilege... privileges )
        throws ArtifactoryMigrationException
    {
        CRole role = new CRole();

        role.setId( target.getId() + "-" + repoKey + "-" + key );

        role.setName( target.getId() + "-" + repoKey + "-" + key );

        role.setSessionTimeout( 60 );

        List<String> privIds = new ArrayList<String>( privileges.length );

        for ( CPrivilege priv : privileges )
        {
            privIds.add( priv.getId() );
        }

        role.setPrivileges( privIds );

        request.getPersistor().receiveSecurityRole( role );

        return role;
    }

    private void buildSecurityUsers( SecurityConfigConvertorRequest request )
    {
        for ( ArtifactoryUser artifactoryUser : request.getConfig().getUsers() )
        {
            request.getMigrationResult().addInfoMessage( "Importing user: " + artifactoryUser.getUsername() );

            if ( StringUtils.isEmpty( artifactoryUser.getPassword() ) )
            {
                // assuming that the user is from a external realm (LDAP)
                request
                    .getMigrationResult()
                    .addWarningMessage(
                        "Failed to add user: '"
                            + artifactoryUser.getUsername()
                            + "'.  User was missing a password. Usually this means the user is from an external Realm, e.g., LDAP." );
            }
            else
            {
                CUser user = buildSecurityUser( request, artifactoryUser );
                CUserRoleMapping map = new CUserRoleMapping();
                map.setUserId( user.getId() );

                if ( artifactoryUser.isAdmin() )
                {
                    map.addRole( "nx-admin" );
                }
                else
                {
                    buildUserAclRole( request, user, map );
                }

                // add group roles
                for ( ArtifactoryGroup group : artifactoryUser.getGroups() )
                {
                    map.addRole( group.getName() );
                }

                // nexus doesn't allow a user has no role assigned
                if ( map.getRoles().isEmpty() )
                {
                    map.addRole( "anonymous" );
                }
                // save the user
                try
                {
                    request.getPersistor().receiveSecurityUser( user, map );
                }
                catch ( ArtifactoryMigrationException e )
                {
                    request.getMigrationResult().addErrorMessage( "Failed to import user: '" + user.getId() + "'.", e );
                }

                request.getMigratedUsers().put( user, map );

                request.getMigrationResult().addDebugMessage( "User '" + user.getId() + "' was imported successfully" );
            }
        }
    }

    private CUser buildSecurityUser( SecurityConfigConvertorRequest request, ArtifactoryUser artifactoryUser )
    {
        CUser securityUser = new CUser();

        securityUser.setId( artifactoryUser.getUsername() );

        securityUser.setPassword( artifactoryUser.getPassword() );

        securityUser.setFirstName( artifactoryUser.getUsername() );

        securityUser.setEmail( artifactoryUser.getEmail() );

        securityUser.setStatus( "active" );

        return securityUser;
    }

    private void buildUserAclRole( SecurityConfigConvertorRequest request, CUser user, CUserRoleMapping map )
    {
        if ( !request.isResolvePermission() )
        {
            return;
        }

        for ( ArtifactoryAcl acl : request.getConfig().getAcls() )
        {
            if ( acl.getUser() != null && acl.getUser().getUsername().equals( user.getFirstName() ) )
            {
                map.getRoles().addAll( getRoleListByAcl( request, acl ) );
            }
        }
    }

    private List<String> getRoleListByAcl( SecurityConfigConvertorRequest request, ArtifactoryAcl acl )
    {
        List<String> roleList = new ArrayList<String>();

        for ( List<CRole> repositoryRoles : request
            .getMapping().get( acl.getPermissionTarget().getId() ).listAllRepositoryRoles() )
        {
            if ( acl.getPermissions().contains( ArtifactoryPermission.READER ) )
            {
                roleList.add( repositoryRoles.get( 0 ).getId() );
            }
            if ( acl.getPermissions().contains( ArtifactoryPermission.DEPLOYER ) )
            {
                roleList.add( repositoryRoles.get( 1 ).getId() );
            }
            if ( acl.getPermissions().contains( ArtifactoryPermission.DELETE ) )
            {
                roleList.add( repositoryRoles.get( 2 ).getId() );
            }
            if ( acl.getPermissions().contains( ArtifactoryPermission.ADMIN ) )
            {
                roleList.add( repositoryRoles.get( 3 ).getId() );
            }
        }

        return roleList;
    }

    /**
     * One permission target will be converted a one TargetSuite
     * 
     * @author Juven Xu
     */
    public class TargetSuite
    {
        /**
         * Id of the permission target where this suite be converted from
         */
        private CRepositoryTarget repositoryTarget;

        private Map<String, List<CPrivilege>> privileges = new HashMap<String, List<CPrivilege>>();

        private Map<String, List<CRole>> roles = new HashMap<String, List<CRole>>();

        public CRepositoryTarget getRepositoryTarget()
        {
            return repositoryTarget;
        }

        public void setRepositoryTarget( CRepositoryTarget repositoryTarget )
        {
            this.repositoryTarget = repositoryTarget;
        }

        public List<CPrivilege> getPrivileges( String repokey )
        {
            return privileges.get( repokey );
        }

        public List<CRole> getRoles( String repoKey )
        {
            return roles.get( repoKey );
        }

        public void addPrivilege( String repoKey, CPrivilege privilege )
        {
            if ( privileges.get( repoKey ) == null )
            {
                privileges.put( repoKey, new ArrayList<CPrivilege>() );
            }

            privileges.get( repoKey ).add( privilege );
        }

        public void addRole( String repoKey, CRole role )
        {
            if ( roles.get( repoKey ) == null )
            {
                roles.put( repoKey, new ArrayList<CRole>() );
            }

            roles.get( repoKey ).add( role );
        }
        
        public Collection<List<CRole>> listAllRepositoryRoles()
        {
            return roles.values();
        }
    }
}
