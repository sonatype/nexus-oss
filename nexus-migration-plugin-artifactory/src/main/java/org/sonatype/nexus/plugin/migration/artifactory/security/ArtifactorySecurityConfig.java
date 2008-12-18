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
    
    public ArtifactoryRepoPath getArtifactoryRepoPath( String repoKey, String path )
    {
        for ( ArtifactoryRepoPath repoPath : repoPaths )
        {
            if ( repoPath.getRepoKey().equals( repoKey ) && repoPath.getPath().equals( path ) )
            {
                return repoPath;
            }
        }

        return null;
    }

}
