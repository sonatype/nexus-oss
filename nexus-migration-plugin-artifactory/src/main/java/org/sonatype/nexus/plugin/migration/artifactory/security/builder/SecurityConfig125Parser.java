package org.sonatype.nexus.plugin.migration.artifactory.security.builder;

import java.util.Set;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactoryAcl;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactoryPermission;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactoryPermissionTarget;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactorySecurityConfig;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactoryUser;
import org.sonatype.nexus.plugin.migration.artifactory.util.DomUtil;
import org.sonatype.nexus.plugin.migration.artifactory.util.PatternConvertor;

public class SecurityConfig125Parser
    extends AbstractSecurityConfigParser
{

    public SecurityConfig125Parser( Xpp3Dom dom, ArtifactorySecurityConfig config )
    {
        super( dom, config );
    }

    @Override
    public void parseAcls()
    {
        // build acls
        Xpp3Dom aclsDom = getDom().getChild( "acls" );

        for ( Xpp3Dom aclDom : aclsDom.getChildren() )
        {
            String maskValue = aclDom.getChild( "mask" ).getValue();

            // no permission set, skip
            if ( maskValue.equals( "0" ) )
            {
                continue;
            }

            Set<ArtifactoryPermission> permissions = ArtifactoryPermission.buildPermission125( Integer
                .parseInt( maskValue ) );

            String username = aclDom.getChild( "recipient" ).getValue();

            ArtifactoryUser user = getConfig().getUserByUsername( username );

            Xpp3Dom repoPathDom = aclDom.getChild( "aclObjectIdentity" );

            if ( repoPathDom.getAttribute( "reference" ) != null )
            {
                repoPathDom = DomUtil.findReference( repoPathDom );
            }

            String repoKeyValue = repoPathDom.getChild( "repoKey" ).getValue();

            String pathValue = repoPathDom.getChild( "path" ).getValue();

            ArtifactoryPermissionTarget repoPath = getConfig().getArtifactoryRepoTarget(
                repoKeyValue,
                PatternConvertor.convert125Pattern( pathValue ) );

            ArtifactoryAcl acl = new ArtifactoryAcl( repoPath, user );

            acl.getPermissions().addAll( permissions );

            getConfig().addAcl( acl );
        }

    }

    @Override
    public void parsePermissionTargets()
    {
        Xpp3Dom repoPathsDom = getDom().getChild( "repoPaths" );

        for ( Xpp3Dom repoPathDom : repoPathsDom.getChildren() )
        {
            String repoKeyValue = repoPathDom.getChild( "repoKey" ).getValue();

            String pathValue = repoPathDom.getChild( "path" ).getValue();

            ArtifactoryPermissionTarget repoTarget = new ArtifactoryPermissionTarget( repoKeyValue );

            repoTarget.addInclude( PatternConvertor.convert125Pattern( pathValue ) );

            getConfig().addPermissionTarget( repoTarget );
        }

    }

    @Override
    public void parseUsers()
    {
        Xpp3Dom usersDom = getDom().getChild( "users" );

        for ( Xpp3Dom userDom : usersDom.getChildren() )
        {
            String username = userDom.getChild( "username" ).getValue();
            
            String password = userDom.getChild( "password" ).getValue();

            ArtifactoryUser user = new ArtifactoryUser( username, password );

            for ( Xpp3Dom roleDom : userDom.getChild( "authorities" ).getChildren() )
            {
                String roleValue = roleDom.getChild( "role" ).getValue();

                if ( roleValue.equals( "ADMIN" ) )
                {
                    user.setAdmin( true );
                }
            }

            getConfig().addUser( user );
        }
    }

    @Override
    protected void parseGroups()
    {
        // Artifactory 1.2.5 does not have groups

    }

}
