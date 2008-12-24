package org.sonatype.nexus.plugin.migration.artifactory.security.builder;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.plugin.migration.artifactory.security.ArtifactorySecurityConfig;

public abstract class AbstractSecurityConfigParser
{
    private Xpp3Dom dom;

    private ArtifactorySecurityConfig config;

    public AbstractSecurityConfigParser( Xpp3Dom dom, ArtifactorySecurityConfig config )
    {
        this.dom = dom;

        this.config = config;
    }

    protected Xpp3Dom getDom()
    {
        return dom;
    }

    protected ArtifactorySecurityConfig getConfig()
    {
        return config;
    }

    abstract protected void parseUsers();

    abstract protected void parseGroups();
    
    abstract protected void parsePermissionTargets();

    abstract protected void parseAcls();
    
    public void parse()
    {
        // NOTE that this order is critical
        parseGroups();
        
        parseUsers();
        
        parsePermissionTargets();
        
        parseAcls();
    }
}
