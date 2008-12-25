package org.sonatype.nexus.plugin.migration.artifactory.security.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactorySecurityConfig;

public class ArtifactorySecurityConfigBuilder
{
    public static final String VERSION_125 = "1.2.5";

    public static final String VERSION_130 = "1.3.0";

    public static final String VERSION_UNKNOWN = "unknown";

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
            IOUtil.close( reader );
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
            IOUtil.close( reader );
        }
    }

    /**
     * @param dom
     * @return version of the security config dom
     */
    public static String validate( Xpp3Dom dom )
    {
        if ( dom.getChild( "users" ) == null )
        {
            return VERSION_UNKNOWN;
        }

        if ( dom.getChild( "users" ).getChildren( "org.artifactory.security.SimpleUser" ).length > 0 )
        {
            return VERSION_125;
        }

        if ( dom.getChild( "users" ).getChildren( "user" ).length > 0 )
        {
            return VERSION_130;
        }

        return VERSION_UNKNOWN;
    }

    public static ArtifactorySecurityConfig build( Xpp3Dom dom )
    {
        ArtifactorySecurityConfig securityConfig = new ArtifactorySecurityConfig();

        if ( validate( dom ).equals( VERSION_125 ) )
        {
            new SecurityConfig125Parser( dom, securityConfig ).parse();
        }
        else if ( validate( dom ).equals( VERSION_130 ) )
        {
            new SecurityConfig130Parser( dom, securityConfig ).parse();
        }

        return securityConfig;

    }

}
