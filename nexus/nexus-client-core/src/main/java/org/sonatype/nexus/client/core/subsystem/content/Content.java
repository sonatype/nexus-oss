package org.sonatype.nexus.client.core.subsystem.content;

import java.io.File;
import java.io.IOException;

public interface Content
{
    void download( Location location, File target )
        throws IOException;

    void upload( Location location, File target )
        throws IOException;

    void delete( Location location )
        throws IOException;

    // describe
}
