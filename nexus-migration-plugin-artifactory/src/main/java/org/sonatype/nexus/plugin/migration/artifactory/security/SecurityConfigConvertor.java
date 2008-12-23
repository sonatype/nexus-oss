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
import org.sonatype.nexus.plugin.migration.artifactory.ArtifactoryMigrationException;
import org.sonatype.nexus.plugin.migration.artifactory.util.PatternConvertor;

public class SecurityConfigConvertor
{
    private ArtifactorySecurityConfig config;

    private SecurityConfigReceiver receiver;

    // by default, resolve artifactory permissions
    private boolean resolvePermission = true;

    private List<SecurityUser> users = new ArrayList<SecurityUser>();

    private Map<String, TargetSuite> mapping = new HashMap<String, TargetSuite>();

    public SecurityConfigConvertor( ArtifactorySecurityConfig config, SecurityConfigReceiver persistor )
    {
        this.config = config;

        this.receiver = persistor;
    }

    public boolean isResolvePermission()
    {
        return resolvePermission;
    }

    public void setResolvePermission( boolean resolvePermission )
    {
        this.resolvePermission = resolvePermission;
    }

    public List<SecurityUser> getSecurityUsers()
    {
        return users;
    }

    public void convert()
        throws ArtifactoryMigrationException
    {
        buildTargetSuite();

        buildSecurityUsers();
    }

    private void buildTargetSuite()
        throws ArtifactoryMigrationException
    {
        if ( !resolvePermission )
        {
            return;
        }

        for ( ArtifactoryPermissionTarget target : config.getPermissionTargets() )
        {
            String id = target.getId();

            TargetSuite targetSuite = new TargetSuite();

            CRepositoryTarget repoTarget = buildRepositoryTarget( target );

            SecurityPrivilege createPrivilege = buildSecurityPrivilege( target, repoTarget, "create" );
            SecurityPrivilege readPrivilege = buildSecurityPrivilege( target, repoTarget, "read" );
            SecurityPrivilege updatePrivilege = buildSecurityPrivilege( target, repoTarget, "update" );
            SecurityPrivilege deletePrivilege = buildSecurityPrivilege( target, repoTarget, "delete" );

            SecurityRole readerRole = buildSecurityRole( target, "reader", readPrivilege );
            SecurityRole deployerRole = buildSecurityRole( target, "deployer", createPrivilege, updatePrivilege );
            SecurityRole adminRole = buildSecurityRole( target, "admin", updatePrivilege, deletePrivilege );

            targetSuite.setRepositoryTarget( repoTarget );
            targetSuite.getPrivileges().add( createPrivilege );
            targetSuite.getPrivileges().add( readPrivilege );
            targetSuite.getPrivileges().add( updatePrivilege );
            targetSuite.getPrivileges().add( deletePrivilege );
            targetSuite.getRoles().add( readerRole );
            targetSuite.getRoles().add( deployerRole );
            targetSuite.getRoles().add( adminRole );

            mapping.put( id, targetSuite );
        }
    }

    private CRepositoryTarget buildRepositoryTarget( ArtifactoryPermissionTarget target )
        throws ArtifactoryMigrationException
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

        receiver.receiveRepositoryTarget( repoTarget );

        return repoTarget;
    }

    private SecurityPrivilege buildSecurityPrivilege( ArtifactoryPermissionTarget permissionTarget,
        CRepositoryTarget repoTarget, String method )
        throws ArtifactoryMigrationException
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

        // for creating privs with a repoTarget to all repos, set the repoId and repoGroupId to be empty
        if ( prop.getValue().equals( "ANY" ) )
        {
            prop.setKey( "" );
        }

        privilege.addProperty( prop );

        prop = new SecurityProperty();
        prop.setKey( "repositoryId" );
        prop.setValue( permissionTarget.getRepoKey() );

        privilege.addProperty( prop );

        receiver.receiveSecurityPrivilege( privilege );

        return privilege;
    }

    private SecurityRole buildSecurityRole( ArtifactoryPermissionTarget target, String key,
        SecurityPrivilege... privileges )
        throws ArtifactoryMigrationException
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

        receiver.receiveSecurityRole( role );

        return role;
    }

    private void buildSecurityUsers()
        throws ArtifactoryMigrationException
    {
        for ( ArtifactoryUser artifactoryUser : config.getUsers() )
        {
            SecurityUser user = buildSecurityUser( artifactoryUser );

            if ( artifactoryUser.isAdmin() )
            {
                user.addRole( "admin" );
            }
            else
            {
                buildUserAclRole( user );
            }

            // nexus doesn't allow a user has no role assigned
            if ( user.getRoles().isEmpty() )
            {
                user.addRole( "anonymous" );
            }
            receiver.receiveSecurityUser( user );

            users.add( user );
        }
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

    private void buildUserAclRole( SecurityUser user )
    {
        if ( !resolvePermission )
        {
            return;
        }

        for ( ArtifactoryAcl acl : config.getAcls() )
        {
            if ( !acl.getUser().getUsername().equals( user.getName() ) )
            {
                continue;
            }
            List<SecurityRole> roles = mapping.get( acl.getPermissionTarget().getId() ).getRoles();

            if ( acl.getPermissions().contains( ArtifactoryPermission.READER ) )
            {
                user.addRole( roles.get( 0 ).getId() );
            }
            if ( acl.getPermissions().contains( ArtifactoryPermission.DEPLOYER ) )
            {
                user.addRole( roles.get( 1 ).getId() );
            }
            if ( acl.getPermissions().contains( ArtifactoryPermission.ADMIN ) )
            {
                user.addRole( roles.get( 2 ).getId() );
            }
        }
    }

    /**
     * One permission target will be converted a one TargetSuite
     * 
     * @author Juven Xu
     */
    class TargetSuite
    {

        /**
         * Id of the permission target where this suite be converted from
         */

        private CRepositoryTarget repositoryTarget;

        private List<SecurityPrivilege> privileges = new ArrayList<SecurityPrivilege>();

        private List<SecurityRole> roles = new ArrayList<SecurityRole>();

        public CRepositoryTarget getRepositoryTarget()
        {
            return repositoryTarget;
        }

        public void setRepositoryTarget( CRepositoryTarget repositoryTarget )
        {
            this.repositoryTarget = repositoryTarget;
        }

        public List<SecurityPrivilege> getPrivileges()
        {
            return privileges;
        }

        public void setPrivileges( List<SecurityPrivilege> privileges )
        {
            this.privileges = privileges;
        }

        public List<SecurityRole> getRoles()
        {
            return roles;
        }

        public void setRoles( List<SecurityRole> roles )
        {
            this.roles = roles;
        }
    }
}
