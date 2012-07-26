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

import java.io.File;

/**
 * Nexus Integration Tests file resolver utilities.
 *
 * @since 2.2
 *        <p/>
 *        TODO move this to litmus
 */
public class NexusITFileResolver
{

    /**
     * Path in project where IT resources will be searched.
     */
    private static final String SRC_TEST_IT_RESOURCES = "src/test/it-resources";

    /**
     * The base directory of project containing the test class.
     * Cannot be null.
     */
    private final File baseDir;

    /**
     * The target directory of project containing the test class.
     * Cannot be null.
     */
    private final File targetDir;

    /**
     * The name of running test method.
     * Cannot be null.
     */
    private final String testMethodName;

    /**
     * Constructor.
     *
     * @param baseDir        base directory of project containing the test class. Cannot be null.
     * @param targetDir      target directory of project containing the test class. Cannot be null.
     * @param testMethodName name of running test method. Cannot be null.
     */
    public NexusITFileResolver( final File baseDir,
                                final File targetDir,
                                final String testMethodName )
    {
        this.targetDir = checkNotNull( targetDir );
        this.baseDir = checkNotNull( baseDir );
        this.testMethodName = checkNotNull( testMethodName );
    }

    /**
     * Returns a directory specific to running test method.
     * <p/>
     * Format: {@code <project>/target/its/<test class package>/<test class name>/<test method name>/<path>}
     *
     * @param path path to be appended to test method specific directory
     * @return directory specific to running test method + provided path
     */
    public File methodSpecificDirectory( String path )
    {
        return
            new File(
                new File(
                    new File(
                        new File(
                            targetDir,
                            "its"
                        ),
                        getClass().getSimpleName()
                    ),
                    testMethodName
                ),
                path
            );
    }

    /**
     * Resolves a test file by looking up the specified path into test resources.
     * <p/>
     * It searches the following path locations:<br/>
     * {@code <project>/src/test/it-resources/<test class package>/<test class name>/<test method name>/<path>}<br/>
     * {@code <project>/src/test/it-resources/<test class package>/<test class name>/<path>}<br/>
     * {@code <project>/src/test/it-resources/<path>}<br/>
     *
     * @param path path to look up
     * @return found file
     * @throws RuntimeException if path cannot be found in any of above locations
     */
    public File resolveTestFile( final String path )
        throws RuntimeException
    {
        File level1 = testMethodSourceDirectory( path );
        if ( level1.exists() )
        {
            return level1;
        }
        File level2 = testClassSourceDirectory( path );
        if ( level2.exists() )
        {
            return level2;
        }
        File level3 = testSourceDirectory( path );
        if ( level3.exists() )
        {
            return level3;
        }
        throw new RuntimeException(
            "Path " + path + " not found in any of: " + level1 + ", " + level2 + ", " + level3 );
    }

    /**
     * Returns a test source directory specific to running test.
     * <p/>
     * Format: {@code <project>/src/test/it-resources/<path>}
     *
     * @param path path to be appended
     * @return test source directory specific to running test + provided path
     */
    private File testSourceDirectory( String path )
    {
        return
            new File(
                new File(
                    baseDir,
                    SRC_TEST_IT_RESOURCES
                ),
                path
            );
    }

    /**
     * Returns a test source directory specific to running test class.
     * <p/>
     * Format: {@code <project>/src/test/it-resources/<test class package>/<test class name>/<path>}
     *
     * @param path path to be appended
     * @return test source directory specific to running test class + provided path
     */
    private File testClassSourceDirectory( String path )
    {
        return
            new File(
                new File(
                    new File(
                        baseDir,
                        SRC_TEST_IT_RESOURCES
                    ),
                    getClass().getCanonicalName().replace( ".", "/" )
                ),
                path
            );
    }

    /**
     * Returns a test source directory specific to running test method.
     * <p/>
     * Format: {@code <project>/src/test/it-resources/<test class package>/<test class name>/<test method name>/<path>}
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
                        new File(
                            baseDir,
                            SRC_TEST_IT_RESOURCES
                        ),
                        getClass().getCanonicalName().replace( ".", "/" )
                    ),
                    testMethodName
                ),
                path
            );
    }

}
