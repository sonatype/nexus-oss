package org.sonatype.sisu.locks;

import java.io.OutputStream;
import java.net.URI;

public interface ResourceSession
    extends ResourceView
{
    OutputStream write( URI resource );

    void createFile( URI resource );

    void createFolder( URI resource );

    void copy( URI from, URI to );

    void move( URI from, URI to );

    void delete( URI resource );

    void commit();
}
