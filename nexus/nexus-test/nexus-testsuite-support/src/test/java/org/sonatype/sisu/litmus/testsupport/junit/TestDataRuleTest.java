/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.sisu.litmus.testsupport.junit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Rule;
import org.junit.Test;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

/**
 * {@link TestDataRule} UTs.
 *
 * @since 1.4
 */
public class TestDataRuleTest
    extends TestSupport
{

    @Rule
    public TestDataRule underTest = new TestDataRule( util.resolveFile( "src/test/uncopied-resources" ) );

    /**
     * Test that a file is resolved from root directory.
     *
     * @throws java.io.FileNotFoundException re-thrown
     * @since 1.0
     */
    @Test
    public void resolveFromRoot()
        throws FileNotFoundException
    {
        File file = underTest.resolveFile( "from-root" );
        assertThat( file, is( equalTo( util.resolveFile(
            "src/test/uncopied-resources/from-root"
        ) ) ) );
    }

    /**
     * Test that a file is resolved from package directory in root directory.
     *
     * @throws java.io.FileNotFoundException re-thrown
     * @since 1.0
     */
    @Test
    public void resolveFromPackage()
        throws FileNotFoundException
    {
        File file = underTest.resolveFile( "from-package" );
        assertThat( file, is( equalTo( util.resolveFile(
            "src/test/uncopied-resources/org/sonatype/sisu/litmus/testsupport/junit/from-package"
        ) ) ) );
    }

    /**
     * Test that a file is resolved from class directory in root directory.
     *
     * @throws java.io.FileNotFoundException re-thrown
     * @since 1.0
     */
    @Test
    public void resolveFromClass()
        throws FileNotFoundException
    {
        File file = underTest.resolveFile( "from-class" );
        assertThat( file, is( equalTo( util.resolveFile(
            "src/test/uncopied-resources/org/sonatype/sisu/litmus/testsupport/junit/TestDataRuleTest/from-class"
        ) ) ) );
    }

    /**
     * Test that a file is resolved from method directory in root directory.
     *
     * @throws java.io.FileNotFoundException re-thrown
     * @since 1.0
     */
    @Test
    public void resolveFromMethod()
        throws FileNotFoundException
    {
        File file = underTest.resolveFile( "from-method" );
        assertThat( file, is( equalTo( util.resolveFile(
            "src/test/uncopied-resources/org/sonatype/sisu/litmus/testsupport/junit/TestDataRuleTest/resolveFromMethod/from-method"
        ) ) ) );
    }

}
