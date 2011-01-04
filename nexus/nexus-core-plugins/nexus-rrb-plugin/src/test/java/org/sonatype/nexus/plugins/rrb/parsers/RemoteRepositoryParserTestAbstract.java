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
package org.sonatype.nexus.plugins.rrb.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

abstract class RemoteRepositoryParserTestAbstract
{

    /**
     * Extract the content of a file from the classpath.
     */
    protected String getExampleFileContent( String exampleFileName )
        throws IOException
    {
        URL exampleFileURL = this.getClass().getResource( exampleFileName );
        File file = new File( exampleFileURL.getPath() );

        StringBuilder content = new StringBuilder();
        BufferedReader input = new BufferedReader( new FileReader( file ) );
        try
        {
            String line = null;
            while ( ( line = input.readLine() ) != null )
            {
                content.append( line );
                content.append( System.getProperty( "line.separator" ) );
            }
        }
        finally
        {
            input.close();
        }

        return content.toString();
    }

}
