/**
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.proxy.maven.site;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.proxy.repository.AbstractWebSiteRepositoryConfiguration;

public class DefaultMavenSiteRepositoryConfiguration
    extends AbstractWebSiteRepositoryConfiguration
{

    public DefaultMavenSiteRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

}
