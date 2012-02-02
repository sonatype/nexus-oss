/**
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.proxy.maven.site;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.proxy.repository.WebSiteRepository;

/**
 * A Repository that holds Maven Site.
 * 
 * @author cstamas
 */
public interface MavenSiteRepository
    extends WebSiteRepository
{
    /**
     * Accepts the Maven Site az ZIP file, and automatically "unzips" it honoring the dir structures in ZIP file and
     * prefixing those with the prefix.
     * 
     * @param prefix
     * @param bundle
     * @throws IOException
     */
    void deploySiteBundle( String prefix, InputStream bundle )
        throws IOException;
}
