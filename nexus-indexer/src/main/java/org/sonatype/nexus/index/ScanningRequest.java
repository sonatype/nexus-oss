/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.File;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * A scanning request provides various input parameters for repository scan
 * 
 * @author Jason van Zyl
 */
public class ScanningRequest
{
    private final IndexingContext context;

    private final ArtifactScanningListener artifactScanningListener;

    private final String startingPath;

    public ScanningRequest( final IndexingContext context, final ArtifactScanningListener artifactScanningListener )
    {
        this( context, artifactScanningListener, null );
    }

    public ScanningRequest( final IndexingContext context, final ArtifactScanningListener artifactScanningListener,
                            final String startingPath )
    {
        this.context = context;
        this.artifactScanningListener = artifactScanningListener;
        this.startingPath = startingPath;
    }

    public IndexingContext getIndexingContext()
    {
        return context;
    }

    public ArtifactScanningListener getArtifactScanningListener()
    {
        return artifactScanningListener;
    }

    public String getStartingPath()
    {
        return startingPath;
    }

    public File getStartingDirectory()
    {
        if ( StringUtils.isBlank( startingPath ) )
        {
            return getIndexingContext().getRepository();
        }
        else
        {
            return new File( getIndexingContext().getRepository(), startingPath );
        }
    }
}
