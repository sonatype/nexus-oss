package org.sonatype.nexus.buup.security;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.realms.tools.AbstractStaticSecurityResource;
import org.sonatype.security.realms.tools.StaticSecurityResource;

/**
 * @author juven
 */
@Component( role = StaticSecurityResource.class, hint = "BuupSecurityResource" )
public class BuupSecurityResource
    extends AbstractStaticSecurityResource
{
    @Override
    protected String getResourcePath()
    {
        return "/META-INF/nexus-buup-plugin-security.xml";
    }

}
