package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class ArtifactorySecurityConfig
{
    private List<ArtifactoryUser> users = new ArrayList<ArtifactoryUser>();

    private List<ArtifactoryRepoPath> repoPaths = new ArrayList<ArtifactoryRepoPath>();

    private List<ArtifactoryAcl> acls = new ArrayList<ArtifactoryAcl>();

    public static ArtifactorySecurityConfig read( File file )
        throws IOException,
            XmlPullParserException
    {
        XmlStreamReader reader = ReaderFactory.newXmlReader( file );

        return build( Xpp3DomBuilder.build( reader ) );
    }

    public static ArtifactorySecurityConfig read( InputStream inputStream )
        throws IOException,
            XmlPullParserException
    {
        XmlStreamReader reader = ReaderFactory.newXmlReader( inputStream );

        return build( Xpp3DomBuilder.build( reader ) );
    }

    public static ArtifactorySecurityConfig build( Xpp3Dom dom )
    {
        ArtifactorySecurityConfig securityConfig = new ArtifactorySecurityConfig();

        // build users
        Xpp3Dom usersDom = dom.getChild( "users" );

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
                    user.addRole( ArtifactoryRole.ADMIN );
                }
                else if ( roleValue.equals( "USER" ) )
                {
                    user.addRole( ArtifactoryRole.USER );
                }
            }
            securityConfig.addUser( user );
        }

        // build repoPaths
        Xpp3Dom repoPathsDom = dom.getChild( "repoPaths" );

        for ( Xpp3Dom repoPathDom : repoPathsDom.getChildren() )
        {
            String repoKey = repoPathDom.getChild( "repoKey" ).getValue();

            String path = repoPathDom.getChild( "path" ).getValue();

            ArtifactoryRepoPath repoPath = new ArtifactoryRepoPath( repoKey, path );

            securityConfig.addRepoPath( repoPath );
        }
        
        // TODO: build acls
        return securityConfig;
    }

    public List<ArtifactoryUser> getUsers()
    {
        return users;
    }

    public List<ArtifactoryRepoPath> getRepoPaths()
    {
        return repoPaths;
    }

    public List<ArtifactoryAcl> getAcls()
    {
        return acls;
    }

    public void addUser( ArtifactoryUser user )
    {
        users.add( user );
    }

    public void addRepoPath( ArtifactoryRepoPath repoPath )
    {
        repoPaths.add( repoPath );
    }

    public void addAcl( ArtifactoryAcl acl )
    {
        acls.add( acl );
    }

}
