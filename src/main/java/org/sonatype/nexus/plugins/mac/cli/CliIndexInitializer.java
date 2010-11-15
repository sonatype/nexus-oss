package org.sonatype.nexus.plugins.mac.cli;

import java.io.File;

import org.apache.maven.index.context.IndexingContext;

public interface CliIndexInitializer
{
    IndexingContext initializeIndex( File indexDir, File tempDir );
}
