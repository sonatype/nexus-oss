/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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
package org.sonatype.appcontext.source.keys;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SystemEnvironmentKeyTransformerTest
    extends TestCase
{

    public void testSimple()
    {
        final SystemEnvironmentKeyTransformer transformer = new SystemEnvironmentKeyTransformer();

        Assert.assertEquals( "doodle", transformer.transform( "DOODLE" ) );
        Assert.assertEquals( "mavenHome", transformer.transform( "MAVEN_HOME" ) );
        Assert.assertEquals( "oneTwoThree", transformer.transform( "ONE_TWO_THREE" ) );
    }

    public void testSimpleWithPrefix()
    {
        final SystemEnvironmentKeyTransformer sysEnv = new SystemEnvironmentKeyTransformer();
        final PrefixRemovingKeyTransformer transformer = new PrefixRemovingKeyTransformer( "plexus" );

        Assert.assertEquals( "foo", transformer.transform( sysEnv.transform( "PLEXUS_FOO" ) ) );
        Assert.assertEquals( "someSetting", transformer.transform( sysEnv.transform( "PLEXUS_SOME_SETTING" ) ) );
        Assert.assertEquals( "oneTwoThree", transformer.transform( sysEnv.transform( "PLEXUS_ONE_TWO_THREE" ) ) );
    }

}
