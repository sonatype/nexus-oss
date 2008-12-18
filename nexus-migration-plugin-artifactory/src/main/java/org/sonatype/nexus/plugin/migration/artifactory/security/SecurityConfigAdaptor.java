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

public class SecurityConfigAdaptor
{
    private ArtifactorySecurityConfig config;

    private SecurityConfigAdaptorPersistor persistor;

    private List<CRepositoryTarget> repoTargets = new ArrayList<CRepositoryTarget>();

    private List<SecurityPrivilege> privileges = new ArrayList<SecurityPrivilege>();

    private List<SecurityRole> roles = new ArrayList<SecurityRole>();

    private List<SecurityUser> users = new ArrayList<SecurityUser>();

    private Map<ArtifactoryRepoPath, List<SecurityRole>> repoPathRolesMap = new HashMap<ArtifactoryRepoPath, List<SecurityRole>>();

    public SecurityConfigAdaptor( ArtifactorySecurityConfig config, SecurityConfigAdaptorPersistor persistor )
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

    /**
     * Convert a Artifactory repoPath to a nexus repoTarget pattern
     * 
     * @param artifactoryRepoPath
     * @return
     */
    private static String convertPathToPattern( String artifactoryRepoPath )
    {
        if ( artifactoryRepoPath.equals( ArtifactoryRepoPath.PATH_ANY ) )
        {
            return ".*";
        }
        return artifactoryRepoPath + "/.*";
    }

    public void convert()
    {
        repoTargets = new ArrayList<CRepositoryTarget>( config.getRepoPaths().size() );

        for ( ArtifactoryRepoPath repoPath : config.getRepoPaths() )
        {
            CRepositoryTarget repoTarget = buildRepositoryTarget( repoPath );

            SecurityPrivilege createPrivilege = buildSecurityPrivilege( repoPath, repoTarget, "create" );
            SecurityPrivilege readPrivilege = buildSecurityPrivilege( repoPath, repoTarget, "read" );
            SecurityPrivilege updatePrivilege = buildSecurityPrivilege( repoPath, repoTarget, "update" );
            SecurityPrivilege deletePrivilege = buildSecurityPrivilege( repoPath, repoTarget, "delete" );

            SecurityRole readRole = buildSecurityRole( repoPath, "read", readPrivilege );
            SecurityRole allRole = buildSecurityRole(
                repoPath,
                "all",
                createPrivilege,
                readPrivilege,
                updatePrivilege,
                deletePrivilege );

            List<SecurityRole> roles = new ArrayList<SecurityRole>( 2 );
            roles.add( readRole );
            roles.add( allRole );

            repoPathRolesMap.put( repoPath, roles );
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
                    List<SecurityRole> roles = (List<SecurityRole>) repoPathRolesMap.get( acl.getRepoPath() );

                    if ( acl.getPermissions().contains( ArtifactoryPermission.ADMIN )
                        || acl.getPermissions().contains( ArtifactoryPermission.DEPLOYER ) )
                    {
                        user.addRole( roles.get( 1 ).getId() );
                    }
                    else if ( acl.getPermissions().contains( ArtifactoryPermission.READER ) )
                    {
                        user.addRole( roles.get( 0 ).getId() );
                    }
                }
            }

            persistor.persistSecurityUser( user );

            users.add( user );
        }

    }

    private CRepositoryTarget buildRepositoryTarget( ArtifactoryRepoPath repoPath )
    {
        CRepositoryTarget repoTarget = new CRepositoryTarget();

        repoTarget.setId( repoPath.getRepoKey() );
        
        repoTarget.setName( repoPath.getRepoKey() );

        repoTarget.setContentClass( "maven2" );

        List<String> patterns = new ArrayList<String>( 1 );

        patterns.add( convertPathToPattern( repoPath.getPath() ) );

        repoTarget.setPatterns( patterns );

        persistor.persistRepositoryTarget( repoTarget );

        repoTargets.add( repoTarget );

        return repoTarget;
    }

    private SecurityPrivilege buildSecurityPrivilege( ArtifactoryRepoPath repoPath, CRepositoryTarget repoTarget,
        String method )
    {
        SecurityPrivilege privilege = new SecurityPrivilege();

        privilege.setName( repoPath.getRepoKey() + " - (" + method + ")" );

        privilege.setDescription( repoPath.getRepoKey() + " - (" + method + ") imported from Artifactory" );

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
        prop.setValue( repoPath.getRepoKey() );

        privilege.addProperty( prop );

        persistor.persistSecurityPrivilege( privilege );

        privileges.add( privilege );

        return privilege;
    }

    private SecurityRole buildSecurityRole( ArtifactoryRepoPath repoPath, String key, SecurityPrivilege... privileges )
    {
        SecurityRole role = new SecurityRole();

        role.setId( "repo-" + repoPath.getRepoKey() + "-" + key );

        role.setName( "Repo: " + repoPath.getRepoKey() + " (" + key + ")" );

        role.setSessionTimeout( 60 );

        List<String> privIds = new ArrayList<String>( privileges.length );

        for ( SecurityPrivilege priv : privileges )
        {
            privIds.add( priv.getId() );
        }

        role.setPrivileges( privIds );

        persistor.persistSecurityRole( role );

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
