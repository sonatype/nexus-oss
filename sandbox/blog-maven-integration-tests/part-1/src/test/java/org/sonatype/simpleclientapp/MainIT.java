/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.simpleclientapp;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class MainIT
{

    @Test
    public void testExecute()
        throws Exception
    {
        Assert.assertEquals( 0, execute( new String[] {} ) );
        Assert.assertEquals( 1, execute( new String[] { "one" } ) );
        Assert.assertEquals( 6, execute( new String[] { "one", "two", "three", "four", "five", "six" } ) );
    }

    private int execute( String[] args )
        throws Exception
    {
        File jar = new File( "target/simple-client-app-1.0-SNAPSHOT.jar" );

        String[] execArgs = new String[args.length + 3];
        System.arraycopy( args, 0, execArgs, 3, args.length );
        execArgs[0] = "java";
        execArgs[1] = "-jar";
        execArgs[2] = jar.getCanonicalPath();
        Process p = Runtime.getRuntime().exec( execArgs );
        p.waitFor();
        return p.exitValue();
    }

}
