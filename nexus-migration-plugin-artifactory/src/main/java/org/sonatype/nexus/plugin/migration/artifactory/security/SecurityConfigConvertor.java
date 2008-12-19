package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityProperty;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.plugin.migration.artifactory.util.PatternConvertor;

public class SecurityConfigConvertor
{
    private ArtifactorySecurityConfig config;

    private SecurityConfigReceiver persistor;

    private List<CRepositoryTarget> repoTargets = new ArrayList<CRepositoryTarget>();

    private List<SecurityPrivilege> privileges = new ArrayList<SecurityPrivilege>();

    private List<SecurityRole> roles = new ArrayList<SecurityRole>();

    private List<SecurityUser> users = new ArrayList<SecurityUser>();

    private Map<ArtifactoryPermissionTarget, List<SecurityRole>> repoPathRolesMap = new HashMap<ArtifactoryPermissionTarget, List<SecurityRole>>();

    public SecurityConfigConvertor( ArtifactorySecurityConfig config, SecurityConfigReceiver persistor )
    {
        this.config = config;

        this.persistor = persistor;
    }

    public List<SecurityUser> getSecurityUsers()
    {
        return users;
    }

    public List<SecurityRole> getSecurityRoles()
    {
        return roles;
    }

    public List<CRepositoryTarget> getRepositoryTargets()
    {
        return repoTargets;
    }

    public List<SecurityPrivilege> getSecurityPrivileges()
    {
        return privileges;
    }

    public void convert()
    {
        repoTargets = new ArrayList<CRepositoryTarget>( config.getPermissionTargets().size() );

        for ( ArtifactoryPermissionTarget target : config.getPermissionTargets() )
        {
            CRepositoryTarget repoTarget = buildRepositoryTarget( target );

            SecurityPrivilege createPrivilege = buildSecurityPrivilege( target, repoTarget, "create" );
            SecurityPrivilege readPrivilege = buildSecurityPrivilege( target, repoTarget, "read" );
            SecurityPrivilege updatePrivilege = buildSecurityPrivilege( target, repoTarget, "update" );
            SecurityPrivilege deletePrivilege = buildSecurityPrivilege( target, repoTarget, "delete" );

            SecurityRole readerRole = buildSecurityRole( target, "reader", readPrivilege );

            SecurityRole deployerRole = buildSecurityRole( target, "deployer", createPrivilege, updatePrivilege );

            SecurityRole adminRole = buildSecurityRole( target, "admin", updatePrivilege, deletePrivilege );

            List<SecurityRole> roles = new ArrayList<SecurityRole>( 3 );
            roles.add( readerRole );
            roles.add( deployerRole );
            roles.add( adminRole );

            repoPathRolesMap.put( target, roles );
        }

        for ( ArtifactoryUser artifactoryUser : config.getUsers() )
        {
            SecurityUser user = buildSecurityUser( artifactoryUser );

            if ( artifactoryUser.isAdmin() )
            {
                user.addRole( "admin" );
            }
            else
            {
                for ( ArtifactoryAcl acl : config.getAcls() )
                {
                    List<SecurityRole> roles = (List<SecurityRole>) repoPathRolesMap.get( acl.getPermissionTarget() );

                    if (acl.getPermissions().contains( ArtifactoryPermission.READER ) )
                    {
                        user.addRole( roles.get( 0 ).getId() );
                    }
                    if (acl.getPermissions().contains( ArtifactoryPermission.DEPLOYER ))
                    {
                        user.addRole( roles.get( 1 ).getId() );
                    }
                    if ( acl.getPermissions().contains( ArtifactoryPermission.ADMIN ) )
                    {
                        user.addRole( roles.get( 2 ).getId() );
                    }
                }
            }

            persistor.receiveSecurityUser( user );

            users.add( user );
        }

    }

    private CRepositoryTarget buildRepositoryTarget( ArtifactoryPermissionTarget target )
    {
        CRepositoryTarget repoTarget = new CRepositoryTarget();

        repoTarget.setId( target.getId() );

        repoTarget.setName( target.getId() );

        repoTarget.setContentClass( "maven2" );

        List<String> patterns = new ArrayList<String>();

        for ( String include : target.getIncludes() )
        {
            patterns.add( PatternConvertor.convert125Pattern( include ) );
        }

        repoTarget.setPatterns( patterns );

        persistor.receiveRepositoryTarget( repoTarget );

        repoTargets.add( repoTarget );

        return repoTarget;
    }

    private SecurityPrivilege buildSecurityPrivilege( ArtifactoryPermissionTarget permissionTarget,
        CRepositoryTarget repoTarget, String method )
    {
        SecurityPrivilege privilege = new SecurityPrivilege();

        privilege.setName( permissionTarget.getId() + "-" + method );

        privilege.setDescription( permissionTarget.getId() + "-" + method );

        privilege.setType( "target" );

        SecurityProperty prop = new SecurityProperty();
        prop.setKey( "method" );
        prop.setValue( method );

        privilege.addProperty( prop );

        prop = new SecurityProperty();
        prop.setKey( "repositoryTargetId" );
        prop.setValue( repoTarget.getId() );

        privilege.addProperty( prop );

        prop = new SecurityProperty();
        prop.setKey( "repositoryId" );
        prop.setValue( permissionTarget.getRepoKey() );

        privilege.addProperty( prop );

        persistor.receiveSecurityPrivilege( privilege );

        privileges.add( privilege );

        return privilege;
    }

    private SecurityRole buildSecurityRole( ArtifactoryPermissionTarget target, String key,
        SecurityPrivilege... privileges )
    {
        SecurityRole role = new SecurityRole();

        role.setId( target.getId() + "-" + key );

        role.setName( target.getId() + "-" + key );

        role.setSessionTimeout( 60 );

        List<String> privIds = new ArrayList<String>( privileges.length );

        for ( SecurityPrivilege priv : privileges )
        {
            privIds.add( priv.getId() );
        }

        role.setPrivileges( privIds );

        persistor.receiveSecurityRole( role );

        roles.add( role );

        return role;
    }

    private SecurityUser buildSecurityUser( ArtifactoryUser artifactoryUser )
    {
        SecurityUser securityUser = new SecurityUser();

        securityUser.setId( artifactoryUser.getUsername() );

        securityUser.setName( artifactoryUser.getUsername() );

        securityUser.setEmail( artifactoryUser.getEmail() );

        securityUser.setStatus( "active" );

        return securityUser;
    }
}
