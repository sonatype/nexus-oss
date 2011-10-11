package org.sonatype.sisu.locks.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public interface Resources
{
    Session startSession();

    void shutdown();

    interface Session
    {
        void createFile( URI resource );

        void createFolder( URI resource );

        void copy( URI from, URI to );

        void move( URI from, URI to );

        void delete( URI resource );

        OutputStream write( URI resource );

        InputStream read( URI resource );

        String[] list( URI resource );

        boolean isFile( URI resource );

        boolean isFolder( URI resource );

        boolean canWrite( URI resource );

        void commit();

        void rollback();
    }
}
