package org.sonatype.nexus.buup.invoke;

import java.io.File;
import java.util.HashMap;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.buup.BuupInvocationException;
import org.sonatype.buup.BuupInvocationRequest;
import org.sonatype.buup.BuupInvoker;

@Component( role = NexusBuupInvoker.class )
public class DefaultNexusBuupInvoker
    implements NexusBuupInvoker
{
    @Configuration( value = "${basedir}" )
    private File basedir;

    private BuupInvoker invoker = new BuupInvoker();

    public void invokeBuup( NexusBuupInvocationRequest request )
        throws NexusBuupInvocationException
    {
        HashMap<String, String> params = new HashMap<String, String>();

        if ( StringUtils.isNotBlank( request.getNexusBundleXmx() ) )
        {
            params.put( "nexus.bundle.xmx", request.getNexusBundleXmx() );
        }

        if ( StringUtils.isNotBlank( request.getNexusBundleXms() ) )
        {
            params.put( "nexus.bundle.xms", request.getNexusBundleXms() );
        }

        BuupInvocationRequest br =
            new BuupInvocationRequest( basedir, request.getExplodedUpgradeBundleDirectory(),
                "org.sonatype.buup.nexus.NexusBuup", params );

        try
        {
            // this call will never return! Will exit JVM!
            invoker.invoke( br );
        }
        catch ( BuupInvocationException e )
        {
            throw new NexusBuupInvocationException( "Cannot invoke BUUP!", e );
        }
    }

}
