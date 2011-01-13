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
package org.sonatype.nexus.plugins.rest;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = NexusResourceBundle.class, hint = "simpleTest" )
public class SimpleDocumentationResourceBundle
    extends AbstractDocumentationNexusResourceBundle
{
    @Override
    public String getPluginId()
    {
        return "test";
    }

    @Override
    protected ZipFile getZipFile()
        throws IOException
    {
        final String file = new File( getClass().getResource( "/docs.zip" ).getFile() ).getCanonicalPath();
        try
        {
            return new ZipFile( file );
        }
        catch ( ZipException e )
        {
            throw new IOException( e.getMessage() + ": " + file, e );
        }
    }

    @Override
    public String getDescription()
    {
        return "Simple Test";
    }

    @Override
    public String getUrlSnippet()
    {
        return "test";
    }
}