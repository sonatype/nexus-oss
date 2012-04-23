package org.sonatype.nexus.plugin.deploy;

import java.io.File;
import java.io.IOException;

public interface Zapper
{
    void deployDirectory( String remoteUrl, File directory )
        throws IOException;
}
