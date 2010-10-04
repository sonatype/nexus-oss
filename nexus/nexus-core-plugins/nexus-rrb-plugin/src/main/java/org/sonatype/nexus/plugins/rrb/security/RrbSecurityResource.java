package org.sonatype.nexus.plugins.rrb.security;

import org.sonatype.security.realms.tools.AbstractStaticSecurityResource;
import org.sonatype.security.realms.tools.StaticSecurityResource;

public class RrbSecurityResource
    extends AbstractStaticSecurityResource
    implements StaticSecurityResource
{
    @Override
    protected String getResourcePath()
    {
        return "/META-INF/nexus-rrb-plugin-security.xml";
    }
}
