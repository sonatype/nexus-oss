package org.sonatype.nexus.plugins.plugin.console.security;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.realms.tools.AbstractStaticSecurityResource;
import org.sonatype.security.realms.tools.StaticSecurityResource;

@Component( role = StaticSecurityResource.class, hint = "PluginConsoleSecurityResource" )
public class PluginConsoleSecurityResource
    extends AbstractStaticSecurityResource
{
    @Override
    protected String getResourcePath()
    {
        return "/META-INF/nexus-plugin-console-plugin-security.xml";
    }

}
