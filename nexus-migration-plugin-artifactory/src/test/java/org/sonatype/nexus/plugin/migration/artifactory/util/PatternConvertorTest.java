/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.plugin.migration.artifactory.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Test;

public class PatternConvertorTest
{
    @Test
    public void convert125Pattern()
        throws Exception
    {
        Pattern pattern = Pattern.compile( PatternConvertor.convert125Pattern( "ANY" ) );

        assertMatch(pattern, "org/apache");
        assertMatch(pattern, "com/");
        
        pattern = Pattern.compile( PatternConvertor.convert125Pattern( "org/apache" ) );
        assertMatch(pattern, "org/apache/");
        assertMatch(pattern, "org/apache/maven");
        assertMatch(pattern, "org/apache/maven/artifact.jar");
        assertNotMatch(pattern, "org/sonatype");
        assertNotMatch(pattern, "org");
        assertNotMatch(pattern, "foo/org/apache");
    }

    @Test
    public void convert130Pattern()
    {
        List<String> includes = new ArrayList<String>();
        List<String> excludes = new ArrayList<String>();

        includes.add( "org/apache/**" );
        includes.add( "com/acme/**" );
        includes.add( "org/?aven/**" );
        includes.add( "**/*-sources.*" );
        excludes.add( "com/acme/exp-project/**" );
        excludes.add( "**/*-sources.*" );
        excludes.add( "**/*-SNAPSHOT/**" );

        Pattern pattern = Pattern.compile( PatternConvertor.convert130Pattern( includes, excludes ) );
        
        assertMatch(pattern, "org/apache/maven");
        assertMatch(pattern, "com/acme/");
        assertMatch(pattern, "com/acme/artifactory");
        assertMatch(pattern, "org/maven/");
        assertMatch(pattern, "org/raven/");
        assertMatch(pattern, "com/sonatype/my/artifact-1.0-sources.jar");
        assertNotMatch(pattern, "com/sonatype/my/artifact-1.0-sourcesjar");
        //TODO: the asserts below should be passed
/*        assertNotMatch(pattern, "org/");
        assertNotMatch(pattern, "org/ntaven/");
        assertNotMatch(pattern, "com/acme/exp-project/");
        assertNotMatch(pattern, "org/apache/maven/artifact-sources.jar");
        assertNotMatch(pattern, "org/apache/artifact-sources.jar.md5");
        assertNotMatch(pattern, "org/apache/maven/1.0-SNAPSHOT/");
        assertNotMatch(pattern, "org/acme/0.1-SNAPSHOT/artifact-0.1-SNAPSHOT.jar");*/
    }

    protected void assertMatch( Pattern pattern, String text )
    {
        Assert.assertTrue( text + " doesn't match pattern " + pattern.pattern(), pattern.matcher( text ).matches() );
    }

    protected void assertNotMatch( Pattern pattern, String text )
    {
        Assert.assertFalse( text + " matches pattern " + pattern.pattern(), pattern.matcher( text ).matches() );
    }
    
    public static void main(String[] args)
    {
        System.out.println(PatternConvertor.convertAntStylePattern("**/*-sources.*"));
    }
}
