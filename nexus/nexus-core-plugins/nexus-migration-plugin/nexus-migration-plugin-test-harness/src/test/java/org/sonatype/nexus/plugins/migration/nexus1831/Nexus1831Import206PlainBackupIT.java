package org.sonatype.nexus.plugins.migration.nexus1831;

import java.io.File;

import org.sonatype.nexus.plugins.migration.nexus1434.AbstractImportArtifactoryIT;

public class Nexus1831Import206PlainBackupIT
    extends AbstractImportArtifactoryIT
{

    @Override
    protected File getBackupFile()
    {
        return getTestFile( "backup" );
    }
}
