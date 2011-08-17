/**
 * Copyright (c) 2008-2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.migration.nexus3343;

import java.io.File;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.IllegalArtifactCoordinateException;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.GavUtil;

public abstract class AbstractBigFileIT
    extends AbstractMigrationIntegrationTest
{

    protected static final Gav GAV;
    static
    {
        try
        {
            GAV = GavUtil.newGav( "nexus3343", "released", "1.0", "bin" );
        }
        catch ( IllegalArtifactCoordinateException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    protected void runOnce()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        commitMigration( migrationSummary );
    }

    @Test
    public void test()
        throws Exception
    {
        FileTestingUtils.populate( getSourceFile(), 5 );
        File result = doTest();

        Assert.assertTrue( "File don't match", FileTestingUtils.compareFileSHA1s( result, getSourceFile() ) );
    }

    public abstract File getSourceFile();

    public abstract File doTest()
        throws Exception;

}
