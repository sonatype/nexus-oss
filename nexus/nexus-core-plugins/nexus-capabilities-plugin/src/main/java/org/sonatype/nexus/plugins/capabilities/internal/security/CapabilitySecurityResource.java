package org.sonatype.nexus.plugins.capabilities.internal.security;

import javax.inject.Singleton;

import org.sonatype.security.realms.tools.AbstractStaticSecurityResource;
import org.sonatype.security.realms.tools.StaticSecurityResource;

@Singleton
public class CapabilitySecurityResource
    extends AbstractStaticSecurityResource
    implements StaticSecurityResource
{
    @Override
    public String getResourcePath()
    {
        return "/META-INF/org.sonatype.nexus.plugins.capability.imp-security.xml";
    }

}