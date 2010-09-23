package org.sonatype.nexus.plugins.mac.cli;

import java.io.File;

import org.sonatype.nexus.index.context.IndexingContext;

public interface CliIndexInitializer
{
    IndexingContext initializeIndex( File indexDir, File tempDir );
}
