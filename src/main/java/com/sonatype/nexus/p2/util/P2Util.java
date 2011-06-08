/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.p2.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.sonatype.plugin.metadata.GAVCoordinate;

public class P2Util
{

    private static GAVCoordinate coordinate;

    public static GAVCoordinate getPluginCoordinates()
    {
        if ( coordinate == null )
        {
            Properties props = new Properties();

            InputStream is =
                P2Util.class.getResourceAsStream( "/META-INF/maven/com.sonatype.nexus.plugin/nexus-p2-plugin/pom.properties" );

            if ( is != null )
            {
                try
                {
                    props.load( is );
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( e.getMessage(), e );
                }
            }

            coordinate =
                new GAVCoordinate( "com.sonatype.nexus.plugin", "nexus-p2-plugin", props.getProperty( "version" ) );
        }
        return coordinate;
    }

}
