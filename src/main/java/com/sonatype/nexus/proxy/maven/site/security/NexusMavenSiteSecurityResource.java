
/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package com.sonatype.nexus.proxy.maven.site.security;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.realms.tools.AbstractStaticSecurityResource;
import org.sonatype.security.realms.tools.StaticSecurityResource;

@Component( role = StaticSecurityResource.class, hint = "NexusMavenSiteSecurityResource" )
public class NexusMavenSiteSecurityResource
    extends AbstractStaticSecurityResource
{
    public String getResourcePath()
    {
        return "/META-INF/nexus-maven-site-plugin-security.xml";
    }
}
