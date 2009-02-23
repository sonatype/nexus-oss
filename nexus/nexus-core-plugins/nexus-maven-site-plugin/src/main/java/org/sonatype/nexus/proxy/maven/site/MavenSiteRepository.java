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
