package org.sonatype.nexus.configuration.security.source;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.realms.tools.StaticSecurityResource;

@Component( role = StaticSecurityResource.class, hint = "NexusStaticSecurityResource" )
public class NexusStaticSecurityResource
    implements StaticSecurityResource
{
    public String getResourcePath()
    {
        return "/META-INF/nexus/static-security.xml";
    }
    
    public Configuration getConfiguration()
    {
        return null;
    }
}
