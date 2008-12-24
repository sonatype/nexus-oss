package org.sonatype.nexus.plugin.migration.artifactory.security.builder;

import java.util.Set;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactoryAcl;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactoryGroup;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactoryPermission;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactoryPermissionTarget;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactorySecurityConfig;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactoryUser;
import org.sonatype.nexus.plugin.migration.artifactory.util.PatternConvertor;

public class SecurityConfig130Parser
    extends AbstractSecurityConfigParser
{

    public SecurityConfig130Parser( Xpp3Dom dom, ArtifactorySecurityConfig config )
    {
        super( dom, config );
    }

    @Override
    protected void parseAcls()
    {
        Xpp3Dom aclsDom = getDom().getChild( "acls" );

        if ( aclsDom.getChildCount() == 0 )
        {
            return;
        }

        for ( Xpp3Dom aclDom : aclsDom.getChildren() )
        {

            String permTargetName = aclDom.getChild( "permissionTarget" ).getChild( "name" ).getValue();

            ArtifactoryPermissionTarget permTarget = getConfig().getPermissionTarget( permTargetName );

            Xpp3Dom aces = aclDom.getChild( "aces" );

            if ( aces.getChildCount() == 0 )
            {
                continue;
            }

            for ( Xpp3Dom ace : aces.getChildren() )
            {
                String mask = ace.getChild( "mask" ).getValue();

                if ( mask.equals( "0" ) )
                {
                    continue;
                }

                String principal = ace.getChild( "principal" ).getValue();

                boolean isGroup = ace.getChild( "group" ).getValue().equals( "true" );

                ArtifactoryAcl acl;

                if ( isGroup )
                {
                    ArtifactoryGroup group = getConfig().getGroupByName( principal );

                    acl = new ArtifactoryAcl( permTarget, group );
                }
                else
                {
                    ArtifactoryUser user = getConfig().getUserByUsername( principal );

                    acl = new ArtifactoryAcl( permTarget, user );
                }

                Set<ArtifactoryPermission> permissions = ArtifactoryPermission.buildPermission130( Integer
                    .parseInt( mask ) );

                acl.getPermissions().addAll( permissions );

                getConfig().addAcl( acl );
            }

        }

    }

    @Override
    protected void parsePermissionTargets()
    {
        Xpp3Dom aclsDom = getDom().getChild( "acls" );

        if ( aclsDom.getChildCount() == 0 )
        {
            return;
        }

        for ( Xpp3Dom acl : aclsDom.getChildren() )
        {
            Xpp3Dom targetDom = acl.getChild( "permissionTarget" );

            String name = targetDom.getChild( "name" ).getValue();

            String repoKey = targetDom.getChild( "repoKey" ).getValue();

            ArtifactoryPermissionTarget target = new ArtifactoryPermissionTarget( name, repoKey );

            Xpp3Dom includes = targetDom.getChild( "includes" );

            if ( includes.getChildCount() > 0 )
            {
                for ( Xpp3Dom include : includes.getChildren() )
                {
                    target.addInclude( PatternConvertor.convertAntStylePattern( include.getValue() ) );
                }
            }

            getConfig().addPermissionTarget( target );
        }

    }

    @Override
    public void parseUsers()
    {
        Xpp3Dom usersDom = getDom().getChild( "users" );

        for ( Xpp3Dom userDom : usersDom.getChildren() )
        {
            String username = userDom.getChild( "username" ).getValue();

            ArtifactoryUser user = new ArtifactoryUser( username );

            if ( userDom.getChild( "admin" ) != null && userDom.getChild( "admin" ).getValue().equals( "true" ) )
            {
                user.setAdmin( true );
            }

            if ( userDom.getChild( "email" ) != null )
            {
                user.setEmail( userDom.getChild( "email" ).getValue() );
            }

            Xpp3Dom groupsDom = userDom.getChild( "groups" );

            if ( groupsDom != null && groupsDom.getChildCount() != 0 )
            {
                for ( Xpp3Dom groupDom : groupsDom.getChildren() )
                {
                    user.getGroups().add( getConfig().getGroupByName( groupDom.getValue() ) );
                }
            }

            getConfig().addUser( user );
        }
    }

    @Override
    protected void parseGroups()
    {
        Xpp3Dom groupsDom = getDom().getChild( "groups" );

        if ( groupsDom.getChildCount() == 0 )
        {
            return;
        }

        for ( Xpp3Dom groupDom : groupsDom.getChildren() )
        {
            String name = groupDom.getChild( "groupName" ).getValue();

            String description = groupDom.getChild( "description" ).getValue();

            getConfig().addGroup( new ArtifactoryGroup( name, description ) );
        }
    }

}
