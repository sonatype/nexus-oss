package org.sonatype.nexus.plugins.migration.nexus1831;

import java.io.File;

import org.sonatype.nexus.plugins.migration.nexus1434.AbstractImportArtifactoryTest;

public class Nexus1831Import206PlainBackupTest
    extends AbstractImportArtifactoryTest
{

    @Override
    protected File getBackupFile()
    {
        return getTestFile( "backup" );
    }
}
