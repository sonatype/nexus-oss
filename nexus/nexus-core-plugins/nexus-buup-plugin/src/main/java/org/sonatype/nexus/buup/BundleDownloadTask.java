package org.sonatype.nexus.buup;

import java.io.File;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.scheduling.SchedulerTask;

@Component( role = SchedulerTask.class, hint = "BundleDownloadTask", instantiationStrategy = "per-lookup" )
public class BundleDownloadTask
    extends AbstractNexusTask<Object>
{
    private boolean successful;

    private File targetDirectory;

    public boolean isSuccessful()
    {
        return successful;
    }

    public File getTargetDirectory()
    {
        return targetDirectory;
    }

    public void setTargetDirectory( File targetDirectory )
    {
        this.targetDirectory = targetDirectory;
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        // downloads bundle from somewhere (from where?)
        // checks it's checksum (similar is applied as to maven artifacts)
        // unzip it to targetDirectory

        return null;
    }

    @Override
    protected String getAction()
    {
        return "BUUP";
    }

    @Override
    protected String getMessage()
    {
        return "Downloading bundle for OneClickUpgrade";
    }

}
