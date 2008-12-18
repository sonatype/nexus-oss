package org.sonatype.nexus.plugin.migration.artifactory.security.builder;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactorySecurityConfig;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactoryUser;

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
        // TODO Auto-generated method stub

    }

    @Override
    protected void parseRepoPaths()
    {
        // TODO Auto-generated method stub

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

            getConfig().addUser( user );
        }
    }

}
