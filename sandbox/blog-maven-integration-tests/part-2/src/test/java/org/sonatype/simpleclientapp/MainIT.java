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
package org.sonatype.simpleclientapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.junit.Test;

public class MainIT
{
    @Test
    public void testExecute()
        throws Exception
    {
        assertEquals( 0, execute( new String[] {} ) );
        assertEquals( 1, execute( new String[] { "one" } ) );
        assertEquals( 6, execute( new String[] { "one", "two", "three", "four", "five", "six" } ) );
    }

    private int execute( String[] args )
        throws Exception
    {
        File jar = new File( "target/emma/simple-client-app-1.0-SNAPSHOT.jar" );

        String[] execArgs = new String[args.length + 3];
        System.arraycopy( args, 0, execArgs, 3, args.length );
        execArgs[0] = "java";
        execArgs[1] = "-jar";
        execArgs[2] = jar.getCanonicalPath();
        Process p = Runtime.getRuntime().exec( execArgs );
        p.waitFor();
        String s = null;
        BufferedReader br = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
        while ( ( s = br.readLine() ) != null )
        {
            System.out.println( s );
        }

        return p.exitValue();
    }

}
