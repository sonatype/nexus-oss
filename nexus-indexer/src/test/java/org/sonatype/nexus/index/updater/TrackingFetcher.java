package org.sonatype.nexus.index.updater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ResourceFetcher that keeps track of all requested remote resources.
 * 
 * @author igor
 */
public class TrackingFetcher
    extends DefaultIndexUpdater.FileFetcher
{
    
    private final ArrayList<String> resources = new ArrayList<String>();

    public TrackingFetcher( File basedir )
    {
        super( basedir );
    }

    @Override
    public InputStream retrieve( String name )
        throws IOException, FileNotFoundException
    {
        resources.add( name );
        return super.retrieve( name );
    }
    
    @Override
    public void retrieve( String name, File targetFile )
        throws IOException, FileNotFoundException
    {
        resources.add( name );
        super.retrieve( name, targetFile );
    }

    public List<String> getRetrievedResources()
    {
        return resources;
    }
}
