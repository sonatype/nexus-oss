package org.sonatype.sisu.locks;

import java.io.InputStream;
import java.net.URI;

public interface ResourceView
{
    boolean isFile( URI resource );

    boolean isFolder( URI resource );

    InputStream read( URI resource );

    String[] list( URI resource );
}
