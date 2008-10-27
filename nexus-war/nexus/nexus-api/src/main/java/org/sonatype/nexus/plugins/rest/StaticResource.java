package org.sonatype.nexus.plugins.rest;

import java.io.IOException;
import java.io.InputStream;

/**
 * This is an abstraction for static resources that the NexusResourceBundle wants to "contribute" to Nexus Web App.
 * 
 * @author cstamas
 */
public interface StaticResource
{
    String getPath();

    String getContentType();

    long getSize();

    InputStream getInputStream()
        throws IOException;
}
