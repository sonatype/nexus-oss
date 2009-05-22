package org.sample.plugin;

import java.io.InputStream;

import javax.inject.Named;

@Named( "XY" )
public class XYVirusScanner
    implements VirusScanner
{

    public boolean hasVirus( InputStream is )
    {
        // DO THE JOB HERE
        return false;
    }

}
