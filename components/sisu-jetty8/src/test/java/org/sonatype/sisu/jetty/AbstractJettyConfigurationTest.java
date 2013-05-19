/*
 * Copyright (c) 2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.sisu.jetty;

import java.io.File;
import java.net.URL;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import static org.junit.Assert.fail;

public abstract class AbstractJettyConfigurationTest
    extends TestSupport
{
    protected String getJettyXmlPath( String jettyXmlName )
    {
        String result = null;

        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        URL res = cloader.getResource( "jetty-xmls/" + jettyXmlName );
        if ( res == null )
        {
            System.out.println( "Can't find jetty-xml: " + jettyXmlName + " on classpath; trying filesystem." );
            File f = new File( "src/test/resources/jetty-xmls/", jettyXmlName );

            if ( !f.isFile() )
            {
                fail("Cannot find Jetty configuration file: " + jettyXmlName
                    + " (tried classpath and base-path src/test/resources/jetty-xmls)");
            }

            result = f.getAbsolutePath();
        }
        else
        {
            result = res.getPath();
        }

        System.out.println( "Jetty configuration path is: '" + result + "'" );
        return result;
    }
}
