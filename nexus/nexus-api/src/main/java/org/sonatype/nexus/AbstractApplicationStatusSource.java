package org.sonatype.nexus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

public abstract class AbstractApplicationStatusSource
    implements ApplicationStatusSource
{
    @Requirement
    private Logger logger;

    /**
     * System status.
     */
    private final SystemStatus systemStatus;

    /**
     * Read/Write lock guarding systemStatus updates.
     */
    private final ReadWriteLock lock;

    /**
     * Timestamp of last update.
     */
    private long lastUpdate = -1;

    /**
     * Public constructor.
     */
    public AbstractApplicationStatusSource()
    {
        this.systemStatus = new SystemStatus();

        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Internal method for getting SystemStatus. Does not perform any locking.
     * 
     * @return
     */
    protected SystemStatus getSystemStatusInternal()
    {
        return systemStatus;
    }

    /**
     * Returns the RW lock.
     * 
     * @return
     */
    protected ReadWriteLock getLock()
    {
        return lock;
    }

    // ==

    /**
     * Returns the SystemStatus, guaranteeing it's consistent state.
     */
    public SystemStatus getSystemStatus()
    {
        updateSystemStatusIfNeeded( false );

        Lock lock = getLock().readLock();

        lock.lock();

        try
        {
            return getSystemStatusInternal();
        }
        finally
        {
            lock.unlock();
        }
    }

    public boolean setState( SystemState state )
    {
        Lock lock = getLock().writeLock();

        lock.lock();

        try
        {
            getSystemStatusInternal().setState( state );

            return true;
        }
        finally
        {
            lock.unlock();
        }
    }

    // ==

    /**
     * Returns the Plexus logger.
     * 
     * @return
     */
    protected Logger getLogger()
    {
        return logger;
    }

    /**
     * Reads the version from a properties file (the one embedded by Maven into Jar).
     * 
     * @param path
     * @return
     */
    protected String readVersion( String path )
    {
        String version = "Unknown";

        try
        {
            Properties props = new Properties();

            InputStream is = getClass().getResourceAsStream( path );

            if ( is != null )
            {
                props.load( is );

                version = props.getProperty( "version" );
            }

        }
        catch ( IOException e )
        {
            logger.error( "Could not load/read version from " + path, e );
        }

        return version;
    }

    /**
     * Will check is needed a SystemStatus update (using retain time) and will perform it.
     * 
     * @param forced if update is forced (performs update forcefully)
     */
    protected void updateSystemStatusIfNeeded( boolean forced )
    {
        long currentTime = System.currentTimeMillis();

        if ( forced || ( currentTime - lastUpdate > 30000 ) )
        {
            Lock lock = getLock().writeLock();

            lock.lock();

            try
            {
                // maybe someone did the job, while we were blocked
                if ( forced || ( currentTime - lastUpdate > 30000 ) )
                {
                    renewSystemStatus( getSystemStatusInternal() );

                    lastUpdate = currentTime;
                }
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    /**
     * Discovers (probably in "edition specific" way) the version of the application.
     * 
     * @return
     */
    protected abstract String discoverApplicationVersion();

    /**
     * Implement here any updates to SystemStatus needed. No need to bother with locking, it happens in the caller of
     * this method. The method body contains exclusive lock to SystemStatus.
     * 
     * @param systemStatus
     */
    protected abstract void renewSystemStatus( SystemStatus systemStatus );
}
