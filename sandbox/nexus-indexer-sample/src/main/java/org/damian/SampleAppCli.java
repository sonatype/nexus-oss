package org.damian;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.tools.cli.AbstractCli;

public class SampleAppCli
    extends AbstractCli
{
    @Override
    public Options buildCliOptions( Options options )
    {
        return options;
    }
    
    @Override
    public void invokePlexusComponent( CommandLine cli, PlexusContainer container )
        throws Exception
    {
        container.getContext().put( "repository.path", "src/test/resources/repo" );
        container.getContext().put( "index.path", "target/indexOutput" );
        container.lookup( SampleApp.class ).index();
    }
    
    public static void main( String[] args )
        throws Exception
    {
        new SampleAppCli().execute( args );
    }
}
