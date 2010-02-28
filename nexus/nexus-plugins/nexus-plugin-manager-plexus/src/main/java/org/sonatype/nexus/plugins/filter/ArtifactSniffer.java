package org.sonatype.nexus.plugins.filter;

import java.io.File;
import java.net.URL;

public interface ArtifactSniffer
{
    FileInfo snif( File file );

    FileInfo snif( URL url );
}
