package org.sonatype.nexus.plugins.migration.nxcm254;

import java.io.File;

public class NXCM254ImportArtifactory125Test
    extends AbstractImportArtifactoryTest
{

    @Override
    protected File getBackupFile()
    {
        return getTestFile( "artifactory125.zip" );
    }

}
