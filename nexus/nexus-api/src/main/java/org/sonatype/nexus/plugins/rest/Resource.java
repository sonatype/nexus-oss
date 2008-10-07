package org.sonatype.nexus.plugins.rest;

import java.io.IOException;
import java.io.InputStream;

public interface Resource
{
    String getPath();

    String getContentType();

    long getSize();

    InputStream getInputStream()
        throws IOException;
}
