package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.plugin.migration.artifactory.util.DomUtil;

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

        try
        {
            return build( Xpp3DomBuilder.build( reader ) );
        }

        finally
        {
            IOUtils.closeQuietly( reader );
        }
    }

    public static ArtifactorySecurityConfig read( InputStream inputStream )
        throws IOException,
            XmlPullParserException
    {
        XmlStreamReader reader = ReaderFactory.newXmlReader( inputStream );

        try
        {
            return build( Xpp3DomBuilder.build( reader ) );
        }
        finally
        {
            IOUtils.closeQuietly( reader );
        }
    }

    public static ArtifactorySecurityConfig build( Xpp3Dom dom )
    {
        ArtifactorySecurityConfig securityConfig = new ArtifactorySecurityConfig();

        // build users
        Xpp3Dom usersDom = dom.getChild( "users" );

        for ( Xpp3Dom userDom : usersDom.getChildren() )
        {
            securityConfig.addUser( buildUser( userDom ) );
        }

        // build repoPaths
        Xpp3Dom repoPathsDom = dom.getChild( "repoPaths" );

        for ( Xpp3Dom repoPathDom : repoPathsDom.getChildren() )
        {
            securityConfig.addRepoPath( buildRepoPath( repoPathDom ) );
        }

        // build acls
        Xpp3Dom aclsDom = dom.getChild( "acls" );

        for ( Xpp3Dom aclDom : aclsDom.getChildren() )
        {
            String maskValue = aclDom.getChild( "mask" ).getValue();

            // no permission set, skip
            if ( maskValue.equals( "0" ) )
            {
                continue;
            }
            
            Set<ArtifactoryPermission> permissions = ArtifactoryPermission.buildPermission( Integer.parseInt( maskValue ) );

            String username = aclDom.getChild( "recipient" ).getValue();

            ArtifactoryUser user = securityConfig.getUserByUsername( username );

            ArtifactoryRepoPath repoPath = null;

            Xpp3Dom repoPathDom = aclDom.getChild( "aclObjectIdentity" );

            if ( repoPathDom.getAttribute( "reference" ) == null )
            {
                repoPath = buildRepoPath( repoPathDom );
            }
            else
            {
                repoPath = buildRepoPath( DomUtil.findReference(repoPathDom) );
            }
            
            ArtifactoryAcl acl = new ArtifactoryAcl(repoPath, user);
            
            for ( ArtifactoryPermission permission : permissions)
            {
                acl.addPermission( permission );
            }
            
            securityConfig.addAcl( acl );
        }

        return securityConfig;
    }

    public static ArtifactoryRepoPath buildRepoPath( Xpp3Dom dom )
    {
        String repoKeyValue = dom.getChild( "repoKey" ).getValue();

        String pathValue = dom.getChild( "path" ).getValue();

        return new ArtifactoryRepoPath( repoKeyValue, pathValue );
    }
    
    public static ArtifactoryUser buildUser( Xpp3Dom dom )
    {
        String username = dom.getChild( "username" ).getValue();

        String password = dom.getChild( "password" ).getValue();

        ArtifactoryUser user = new ArtifactoryUser( username, password );

        for ( Xpp3Dom roleDom : dom.getChild( "authorities" ).getChildren() )
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

        return user;
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
    
    public ArtifactoryUser getUserByUsername( String username )
    {
        for ( ArtifactoryUser user : users )
        {
            if ( user.getUsername().equals( username ) )
            {
                return user;
            }
        }
        return null;
    }

}
