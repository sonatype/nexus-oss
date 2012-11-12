/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.yum.internal;

import static java.io.File.pathSeparator;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.sonatype.nexus.repository.yum.internal.utils.RepositoryTestUtils;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import org.sonatype.sisu.resource.scanner.scanners.SerialScanner;

public class RpmListWriterTest
    extends TestSupport
{

    private static final String REPO_ID = "repoId";

    private static final String FILE_CONTENT = "another-artifact/0.0.1/another-artifact-0.0.1-1.noarch.rpm\n"
        + "conflict-artifact/2.2-1/conflict-artifact-2.2-1.noarch.rpm\n"
        + "conflict-artifact/2.2-2/conflict-artifact-2.2-2.noarch.rpm\n"
        + "test-artifact/1.2/test-artifact-1.2-1.noarch.rpm\n" + "test-artifact/1.3/test-artifact-1.3-1.noarch.rpm\n";

    private static final String NEW_RPM1 = "newAddFileRpm1.rpm";

    private static final String NEW_RPM2 = "newAddFileRpm2.rpm";

    @Test
    public void shouldListFileInSubDirs()
        throws Exception
    {
        File rpmListFile = writeRpmListFile( RepositoryTestUtils.RPM_BASE_FILE, null, null );
        assertEquals( FILE_CONTENT, IOUtils.toString( new FileInputStream( rpmListFile ) ) );
    }

    @Test
    public void shouldListPackagesOnHighestLevel()
        throws Exception
    {
        File rpmListFile =
            writeRpmListFile( new File( RepositoryTestUtils.RPM_BASE_FILE, "conflict-artifact/2.2-2" ), null, null );
        assertEquals( "conflict-artifact-2.2-2.noarch.rpm\n", IOUtils.toString( new FileInputStream( rpmListFile ) ) );
    }

    @Test
    public void shouldRemoveNotExistingRpmsAndAddNewlyAddedFile()
        throws Exception
    {
        File rpmListFile = writeRpmListFile( RepositoryTestUtils.RPM_BASE_FILE, null, null );

        rpmListFile = new RpmListWriter(
            REPO_ID,
            new File( RepositoryTestUtils.RPM_BASE_FILE, "tomcat-mysql-jdbc/5.1.15-2" ),
            "/is24-tomcat-mysql-jdbc-5.1.15-2.1082.noarch.rpm",
            null,
            true,
            wrap( rpmListFile ),
            new RpmScanner( new SerialScanner() )
        ).writeList();

        assertEquals( "is24-tomcat-mysql-jdbc-5.1.15-2.1082.noarch.rpm\n",
                      IOUtils.toString( new FileInputStream( rpmListFile ) ) );
    }

    @Test
    public void shouldReuseExistingPackageFile()
        throws Exception
    {
        File rpmListFile = writeRpmListFile( RepositoryTestUtils.RPM_BASE_FILE, null, null );

        rpmListFile = new RpmListWriter(
            REPO_ID,
            RepositoryTestUtils.RPM_BASE_FILE,
            null,
            null,
            true,
            wrap( rpmListFile ),
            new RpmScanner( new SerialScanner() )
        ).writeList();
        assertEquals( FILE_CONTENT, IOUtils.toString( new FileInputStream( rpmListFile ) ) );
    }

    @Test
    public void shouldCreateVersionSpecificRpmListFile()
        throws Exception
    {
        File rpmListFile = writeRpmListFile( RepositoryTestUtils.RPM_BASE_FILE, "2.2-2", null );
        assertEquals( "conflict-artifact/2.2-2/conflict-artifact-2.2-2.noarch.rpm\n",
                      IOUtils.toString( new FileInputStream( rpmListFile ) ) );
    }

    @Test
    public void shouldNotAddDuplicateToList()
        throws Exception
    {
        File rpmListFile = writeRpmListFile( RepositoryTestUtils.RPM_BASE_FILE, null, null );

        rpmListFile = new RpmListWriter(
            REPO_ID,
            RepositoryTestUtils.RPM_BASE_FILE,
            "conflict-artifact/2.2-1/conflict-artifact-2.2-1.noarch.rpm",
            null,
            true,
            wrap( rpmListFile ),
            new RpmScanner( new SerialScanner() )
        ).writeList();
        assertEquals( FILE_CONTENT, IOUtils.toString( new FileInputStream( rpmListFile ) ) );
    }

    @Test
    public void shouldAddMultipleFiles()
        throws Exception
    {
        // given written list file
        File rpmListFile = writeRpmListFile( RepositoryTestUtils.RPM_BASE_FILE, null, null );
        // when create two files and recreate list
        rpmListFile = new RpmListWriter(
            REPO_ID,
            RepositoryTestUtils.RPM_BASE_FILE,
            NEW_RPM1 + pathSeparator + NEW_RPM2,
            null,
            false,
            wrap( rpmListFile ),
            new RpmScanner( new SerialScanner() )
        ).writeList();
        // then
        final String content = IOUtils.toString( new FileInputStream( rpmListFile ) );
        assertThat( content, containsString( NEW_RPM1 ) );
        assertThat( content, containsString( NEW_RPM2 ) );
        assertThat( content, not( containsString( pathSeparator ) ) );
    }

    private File writeRpmListFile( File rpmBaseDir, String version, String addedFile )
        throws IOException
    {
        File rpmListFile = File.createTempFile( "package.", ".txt" );
        rpmListFile.delete();

        new RpmListWriter(
            REPO_ID,
            rpmBaseDir,
            addedFile,
            version,
            true,
            wrap( rpmListFile ),
            new RpmScanner( new SerialScanner() )
        ).writeList();

        return rpmListFile;
    }

    private ListFileFactory wrap( final File file )
    {
        return new ListFileFactory()
        {
            @Override
            public File getRpmListFile( String id )
            {
                return file;
            }

            @Override
            public File getRpmListFile( String id, String version )
            {
                return file;
            }
        };
    }
}
