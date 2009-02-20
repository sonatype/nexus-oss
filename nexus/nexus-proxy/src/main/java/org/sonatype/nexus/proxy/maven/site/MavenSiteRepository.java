package org.sonatype.nexus.proxy.maven.site;

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
    void deploySiteBundle( String prefix, InputStream bundle )
        throws IOException;
}
