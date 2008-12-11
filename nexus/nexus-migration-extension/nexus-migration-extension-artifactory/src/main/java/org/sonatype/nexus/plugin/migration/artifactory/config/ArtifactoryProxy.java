package org.sonatype.nexus.plugin.migration.artifactory.config;

import static org.sonatype.nexus.plugin.migration.artifactory.util.DomUtil.getValue;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class ArtifactoryProxy
{

    private Xpp3Dom dom;

    public ArtifactoryProxy( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    public String getKey()
    {
        return getValue( dom, "key" );
    }

    public String getHost()
    {
        return getValue( dom, "host" );
    }

    public int getPort()
    {
        return Integer.parseInt( getValue( dom, "port" ) );
    }

    public String getUsername()
    {
        return getValue( dom, "username" );
    }

    public String getPassword()
    {
        return getValue( dom, "password" );
    }

    public String getDomain()
    {
        return getValue( dom, "domain" );
    }

}
