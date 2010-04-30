package org.sonatype.nexus.integrationtests.nxcm970;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;

public class ContinuousDeployer
    implements Runnable
{
    private HttpClient httpClient;

    private volatile boolean deploying;

    private final String targetUrl;

    private int result = -1;

    public ContinuousDeployer( String targetUrl )
    {
        super();

        this.targetUrl = targetUrl;

        this.deploying = true;

        this.httpClient = new HttpClient();
    }

    public boolean isDeploying()
    {
        return deploying;
    }

    public void finishDeploying()
    {
        this.deploying = false;
    }

    public boolean isFinished()
    {
        return result != -1;
    }

    public int getResult()
    {
        return result;
    }

    public void run()
    {
        PutMethod method = new PutMethod( targetUrl );

        method.setRequestEntity( new InputStreamRequestEntity( new EndlessBlockingInputStream( this ) ) );

        try
        {
            result = httpClient.executeMethod( method );
        }
        catch ( Exception e )
        {
            result = -2;

            e.printStackTrace();
        }
    }

    /**
     * This is an endless stream, that will sleep a little and then serve the 'O' character.
     * 
     * @author cstamas
     */
    public static class EndlessBlockingInputStream
        extends InputStream
    {
        private final ContinuousDeployer continuousDeployer;

        public EndlessBlockingInputStream( ContinuousDeployer deployer )
        {
            this.continuousDeployer = deployer;
        }

        @Override
        public int read()
            throws IOException
        {
            if ( continuousDeployer.isDeploying() )
            {
                try
                {
                    Thread.sleep( 300 );

                    return 'T';
                }
                catch ( InterruptedException e )
                {
                    throw new IOException( e.getMessage() );
                }
            }
            else
            {
                // finish
                return -1;
            }
        }
    }
}
