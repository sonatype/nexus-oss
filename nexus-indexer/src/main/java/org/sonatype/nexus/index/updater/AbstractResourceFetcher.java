/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractResourceFetcher
    implements ResourceFetcher
{

    public InputStream retrieve( String name )
        throws IOException, FileNotFoundException
    {
        final File target = File.createTempFile( name, "" );
        retrieve( name, target );
        return new FileInputStream( target )
        {
            @Override
            public void close()
                throws IOException
            {
                super.close();
                target.delete();
            }
        };
    }

}
