package org.sonatype.nexus.buup;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.buup.checks.FSPermissionChecker;
import org.sonatype.nexus.buup.invoke.NexusBuupInvocationException;
import org.sonatype.nexus.buup.invoke.NexusBuupInvocationRequest;
import org.sonatype.nexus.buup.invoke.NexusBuupInvoker;
import org.sonatype.nexus.scheduling.NexusScheduler;

@Component( role = NexusBuupPlugin.class )
public class DefaultNexusBuupPlugin
    implements NexusBuupPlugin
{
    @Requirement
    private NexusBuupInvoker invoker;

    @Requirement
    private NexusScheduler nexusScheduler;

    @Requirement
    private FSPermissionChecker permissionChecker;

    @Configuration( value = "${basedir}" )
    private File basedir;

    @Configuration( value = "${nexus-app}" )
    private File nexusAppDir;

    @Configuration( value = "${nexus-work}" )
    private File nexusWorkDir;

    @Configuration( value = "${nexus-work}/upgrade-bundle" )
    private File upgradeBundleDir;

    private BundleDownloadTask downloadTask;

    public void initiateBundleDownload()
        throws IOException
    {
        // check FS permissions
        permissionChecker.checkFSPermissions( basedir );
        permissionChecker.checkFSPermissions( nexusAppDir );
        permissionChecker.checkFSPermissions( nexusWorkDir );

        if ( !upgradeBundleDir.exists() && !upgradeBundleDir.mkdirs() )
        {
            throw new IOException( "Cannot create directory for bundle download!" );
        }

        // start download thread
        downloadTask = nexusScheduler.createTaskInstance( BundleDownloadTask.class );
        downloadTask.setTargetDirectory( upgradeBundleDir );
        nexusScheduler.submit( "Bundle Download", downloadTask );
    }

    public boolean isUpgradeProcessReady()
    {
        if ( downloadTask != null && downloadTask.isSuccessful() )
        {
            // check for unziped files
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean initiateUpgradeProcess()
        throws NexusBuupInvocationException
    {
        if ( !isUpgradeProcessReady() )
        {
            return false;
        }

        NexusBuupInvocationRequest request = new NexusBuupInvocationRequest( upgradeBundleDir );

        // simulate we have params for now
        request.setNexusBundleXms( 128 );

        request.setNexusBundleXmx( 512 );

        invoker.invokeBuup( request );

        return true;
    }

}
