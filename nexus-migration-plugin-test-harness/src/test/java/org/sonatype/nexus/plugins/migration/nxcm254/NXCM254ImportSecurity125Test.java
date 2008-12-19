package org.sonatype.nexus.plugins.migration.nxcm254;

import java.io.File;

public class NXCM254ImportSecurity125Test
    extends AbstractImportSecurityTest
{
    @Override
    protected File getBackupFile()
    {
        return getTestFile( "artifactory-security-125.zip" );
    }
}
