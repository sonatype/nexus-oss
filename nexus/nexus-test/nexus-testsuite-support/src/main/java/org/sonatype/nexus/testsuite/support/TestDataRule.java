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
package org.sonatype.nexus.testsuite.support;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * JUnit rule for accessing test data
 *
 * @since 1.4
 */
public class TestDataRule
    extends TestWatcher
    implements TestData
{

    /**
     * The root directory containing test data.
     * Cannot be null.
     */
    private final File dataDir;

    /**
     * Test description.
     * Set when test starts.
     */
    private Description description;

    /**
     * Constructor.
     *
     * @param dataDir root directory containing test data. Cannot be null.
     */
    public TestDataRule( final File dataDir )
    {
        this.dataDir = checkNotNull( dataDir );
    }

    @Override
    protected void starting( final Description description )
    {
        this.description = checkNotNull( description );
    }

    @Override
    public File resolveFile( final String path )
    {
        checkState( description != null, "Test was not yet initialized" );
        File level1 = testMethodSourceDirectory( path );
        if ( level1.exists() )
        {
            return level1;
        }
        File level2 = resolveFromClassDirectory( path );
        if ( level2.exists() )
        {
            return level2;
        }
        File level3 = resolveFromDataDirectory( path );
        if ( level3.exists() )
        {
            return level3;
        }
        throw new RuntimeException(
            "Path " + path + " not found in any of: " + level1 + ", " + level2 + ", " + level3
        );
    }

    /**
     * Returns a test data file.
     * <p/>
     * Format: {@code ${dataDir}/${path}}
     *
     * @param path path to be appended
     * @return test source directory specific to running test + provided path
     */
    private File resolveFromDataDirectory( final String path )
    {
        return
            new File(
                dataDir,
                path
            );
    }

    /**
     * Returns a test data file.
     * <p/>
     * Format: {@code ${dataDir}/${test class package}/${test class name}/${test method name}/${path}}
     *
     * @param path path to be appended
     * @return test source directory specific to running test class + provided path
     */
    private File resolveFromClassDirectory( String path )
    {
        return
            new File(
                new File(
                    dataDir,
                    description.getTestClass().getCanonicalName().replace( ".", "/" )
                ),
                path
            );
    }

    /**
     * Returns a test data file.
     * <p/>
     * {@code ${dataDir}/${test class package}/${test class name}/${test method name}/${path}}<br/>
     *
     * @param path path to be appended
     * @return test source directory specific to running test method + provided path
     */
    private File testMethodSourceDirectory( String path )
    {
        return
            new File(
                new File(
                    new File(
                        dataDir,
                        description.getTestClass().getCanonicalName().replace( ".", "/" )
                    ),
                    description.getMethodName()
                ),
                path
            );
    }

}
