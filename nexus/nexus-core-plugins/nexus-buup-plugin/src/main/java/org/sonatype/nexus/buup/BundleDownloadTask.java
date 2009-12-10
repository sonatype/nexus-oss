package org.sonatype.nexus.buup;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.io.RawInputStreamFacade;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.HttpClientProxyUtil;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.util.DigesterUtils;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Task that will be spawned as part of the "initiate upgrade" 1st part. This task only downloads the upgrade bundle,
 * ensures about it's consistency and unzips it to the define place. In every other "error" case, it cleans up after the
 * run.
 * 
 * @author cstamas
 */
@Component( role = SchedulerTask.class, hint = "BundleDownloadTask", instantiationStrategy = "per-lookup" )
public class BundleDownloadTask
    extends AbstractNexusTask<Object>
{
    @Requirement
    private Nexus nexus;

    @Requirement
    private NexusConfiguration nexusConfiguration;

    private boolean finished = false;

    private boolean successful = false;

    private File targetDirectory;

    private HttpClient httpClient;

    /**
     * True when task is done, unrelated is it success or not.
     * 
     * @return
     */
    public boolean isFinished()
    {
        return finished;
    }

    /**
     * True if task finished successfully.
     * 
     * @return
     */
    public boolean isSuccessful()
    {
        return successful;
    }

    /**
     * The bundle unzip target dir.
     * 
     * @return
     */
    public File getTargetDirectory()
    {
        return targetDirectory;
    }

    /**
     * The bundle unzip target dir setter.
     * 
     * @param targetDirectory
     */
    public void setTargetDirectory( File targetDirectory )
    {
        this.targetDirectory = targetDirectory;
    }

    /**
     * TODO: ???
     * 
     * @return
     */
    public String getBaseUrl()
    {
        return "http://www.sonatype.com/buup-files/";
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        try
        {
            // downloads bundle from somewhere (from where?)
            // checks it's checksum (similar is applied as to maven artifacts)
            File bundle =
                retrieveAndCheckBuupFile( "nexus-pro-upgrade-" + nexus.getSystemStatus().getVersion() + ".zip" );

            // unzip it to targetDirectory
            unzipUpgradeBundle( bundle );

            successful = true;
        }
        catch ( IOException e )
        {
            // store it and present as reason to user
            throw e;
        }
        finally
        {
            finished = true;
        }

        return "OK";
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

    // ==

    protected void unzipUpgradeBundle( File zipfile )
        throws IOException
    {
        try
        {
            ZipUnArchiver unzip = new ZipUnArchiver( zipfile );

            unzip.setDestDirectory( getTargetDirectory() );

            unzip.extract();
        }
        catch ( ArchiverException e )
        {
            FileUtils.cleanDirectory( getTargetDirectory() );

            IOException ioe = new IOException( e.getMessage() );
            ioe.initCause( e );
            throw ioe;
        }
    }

    protected File retrieveAndCheckBuupFile( String name )
        throws IOException
    {
        File targetFile = retrieveBuupFile( name );

        File checksumFile = retrieveBuupFile( name + ".sha1" );

        String downloadedChecksum = FileUtils.fileRead( checksumFile );

        String calculatedChecksum = DigesterUtils.getSha1Digest( targetFile );

        if ( StringUtils.equals( downloadedChecksum, calculatedChecksum ) )
        {
            return targetFile;
        }
        else
        {
            if ( targetFile != null )
            {
                FileUtils.forceDelete( targetFile );

                if ( checksumFile != null )
                {
                    FileUtils.forceDelete( checksumFile );
                }
            }

            throw new IOException( "Downloaded bundle file has mismatching checksum!" );
        }
    }

    protected File retrieveBuupFile( String name )
        throws IOException
    {
        if ( httpClient == null )
        {
            getLogger().info( "Creating HttpClient to fetch the upgrade bundle..." );

            httpClient = new HttpClient();

            HttpClientProxyUtil.applyProxyToHttpClient( httpClient, nexusConfiguration.getGlobalRemoteStorageContext(),
                getLogger() );
        }

        GetMethod method = new GetMethod( getBaseUrl() + name );

        int status = httpClient.executeMethod( method );

        if ( HttpStatus.SC_OK == status )
        {
            // save the file somewhere
            File target = File.createTempFile( "buup-bundle", null );

            FileUtils.copyStreamToFile( new RawInputStreamFacade( method.getResponseBodyAsStream() ), target );

            return target;
        }
        else
        {
            throw new IOException( "Could not retrieve the upgrade bundle. Expected HTTP reponse 200, but got "
                + status + ". The bundle download has failed." );
        }
    }
}
