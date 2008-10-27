/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.attributes.DefaultAttributeStorage;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.proxy.router.ResourceStoreIdBasedRepositoryRouter;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

/**
 * The Class AbstractProxyTestEnvironment.
 * 
 * @author cstamas
 */
public abstract class AbstractProxyTestEnvironment
    extends AbstractNexusTestEnvironment
{

    /** The logger. */
    private Logger logger;

    /** The config */
    private ApplicationConfiguration applicationConfiguration;

    /** The repository registry. */
    private RepositoryRegistry repositoryRegistry;

    /** The local repository storage. */
    private AttributesHandler attributesHandler;

    /** The local repository storage. */
    private LocalRepositoryStorage localRepositoryStorage;

    /** The remote repository storage. */
    private RemoteRepositoryStorage remoteRepositoryStorage;

    /** The shared remote storage context */
    private RemoteStorageContext remoteStorageContext;

    /** The root router */
    private RepositoryRouter rootRouter;

    /** The test listener */
    private TestItemEventListener testEventListener;

    /**
     * The all routers.
     */
    private Map<String, RepositoryRouter> routers;

    public ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    public void setApplicationConfiguration( ApplicationConfiguration applicationConfiguration )
    {
        this.applicationConfiguration = applicationConfiguration;
    }

    public RemoteStorageContext getRemoteStorageContext()
    {
        return remoteStorageContext;
    }

    public void setRemoteStorageContext( RemoteStorageContext remoteStorageContext )
    {
        this.remoteStorageContext = remoteStorageContext;
    }

    /**
     * Gets the repository registry.
     * 
     * @return the repository registry
     */
    public RepositoryRegistry getRepositoryRegistry()
    {
        return repositoryRegistry;
    }

    /**
     * Sets the repository registry.
     * 
     * @param repositoryRegistry the new repository registry
     */
    public void setRepositoryRegistry( RepositoryRegistry repositoryRegistry )
    {
        this.repositoryRegistry = repositoryRegistry;
    }

    /**
     * Gets the local repository storage.
     * 
     * @return the local repository storage
     */
    public LocalRepositoryStorage getLocalRepositoryStorage()
    {
        return localRepositoryStorage;
    }

    /**
     * Sets the local repository storage.
     * 
     * @param localRepositoryStorage the new local repository storage
     */
    public void setLocalRepositoryStorage( LocalRepositoryStorage localRepositoryStorage )
    {
        this.localRepositoryStorage = localRepositoryStorage;
    }

    /**
     * Gets the remote repository storage.
     * 
     * @return the remote repository storage
     */
    public RemoteRepositoryStorage getRemoteRepositoryStorage()
    {
        return remoteRepositoryStorage;
    }

    /**
     * Sets the remote repository storage.
     * 
     * @param remoteRepositoryStorage the new remote repository storage
     */
    public void setRemoteRepositoryStorage( RemoteRepositoryStorage remoteRepositoryStorage )
    {
        this.remoteRepositoryStorage = remoteRepositoryStorage;
    }

    /**
     * Gets the logger.
     * 
     * @return the logger
     */
    public Logger getLogger()
    {
        return logger;
    }

    /**
     * Gets the root router.
     * 
     * @return
     */
    public RepositoryRouter getRootRouter()
    {
        return rootRouter;
    }

    /**
     * Gets a specific router.
     * 
     * @param hint
     * @return
     */
    public RepositoryRouter getRouter( String hint )
    {
        return routers.get( hint );
    }

    /**
     * Gets the test event listener.
     * 
     * @return
     */
    public TestItemEventListener getTestEventListener()
    {
        return testEventListener;
    }

    /*
     * (non-Javadoc)
     * @see org.codehaus.plexus.PlexusTestCase#setUp()
     */
    public void setUp()
        throws Exception
    {
        super.setUp();

        LoggerManager loggerManager = getContainer().getLoggerManager();

        this.logger = loggerManager.getLoggerForComponent( this.getClass().toString() );

        applicationConfiguration = (ApplicationConfiguration) lookup( ApplicationConfiguration.class );

        // deleting files
        FileUtils.forceDelete( getApplicationConfiguration().getWorkingDirectory() );

        repositoryRegistry = (RepositoryRegistry) lookup( RepositoryRegistry.class );

        testEventListener = new TestItemEventListener();

        repositoryRegistry.addProximityEventListener( testEventListener );

        attributesHandler = (AttributesHandler) lookup( AttributesHandler.class );

        ( (DefaultAttributeStorage) attributesHandler.getAttributeStorage() )
            .setWorkingDirectory( getApplicationConfiguration().getWorkingDirectory( "proxy/attributes" ) );

        localRepositoryStorage = (LocalRepositoryStorage) lookup( LocalRepositoryStorage.class, "file" );

        remoteStorageContext = new DefaultRemoteStorageContext( null );

        remoteRepositoryStorage = (RemoteRepositoryStorage) lookup( RemoteRepositoryStorage.class, "apacheHttpClient3x" );

        rootRouter = (RepositoryRouter) lookup( ResourceStoreIdBasedRepositoryRouter.ROLE );

        routers = getContainer().lookupMap( RepositoryRouter.class );

        getEnvironmentBuilder().buildEnvironment( this );

        applicationConfiguration
            .notifyProximityEventListeners( new ConfigurationChangeEvent( applicationConfiguration ) );

        applicationConfiguration.notifyProximityEventListeners( new NexusStartedEvent() );

        getEnvironmentBuilder().startService();
    }

    /*
     * (non-Javadoc)
     * @see org.codehaus.plexus.PlexusTestCase#tearDown()
     */
    public void tearDown()
        throws Exception
    {
        getEnvironmentBuilder().stopService();

        super.tearDown();
    }

    /**
     * Gets the environment builder.
     * 
     * @return the environment builder
     */
    protected abstract EnvironmentBuilder getEnvironmentBuilder()
        throws Exception;

    /**
     * Check for file and match contents.
     * 
     * @param item the item
     * @return true, if successful
     */
    protected void checkForFileAndMatchContents( StorageItem item )
        throws Exception
    {
        // file exists
        assertTrue( new File( getBasedir(), "target/test-classes/"
            + item.getRepositoryItemUid().getRepository().getId() + item.getRepositoryItemUid().getPath() ).exists() );
        // match content
        checkForFileAndMatchContents( item, new FileInputStream( new File( getBasedir(), "target/test-classes/"
            + item.getRepositoryItemUid().getRepository().getId() + item.getRepositoryItemUid().getPath() ) ) );
    }

    /**
     * Check for file and match contents.
     * 
     * @param item the item
     * @param wantedContent the wanted content
     * @throws Exception the exception
     */
    protected void checkForFileAndMatchContents( StorageItem item, InputStream wantedContent )
        throws Exception
    {
        // is file
        assertTrue( StorageFileItem.class.isAssignableFrom( item.getClass() ) );
        // is non-virtual
        assertFalse( item.isVirtual() );
        // have UID
        assertTrue( item.getRepositoryItemUid() != null );

        StorageFileItem file = (StorageFileItem) item;

        // is reusable
        assertTrue( file.isReusableStream() );
        // content equals
        InputStream fileContent = file.getInputStream();
        assertTrue( IOUtil.contentEquals( wantedContent, fileContent ) );
        fileContent.close();
    }

    protected File getFile( Repository repository, String path )
        throws IOException
    {
        return new File( getApplicationConfiguration().getWorkingDirectory(), "proxy/store/" + repository.getId()
            + path );
    }

    protected File getRemoteFile( Repository repository, String path )
        throws IOException
    {
        return new File( getBasedir(), "target/test-classes/" + repository.getId() + path );
    }

    protected void saveInputStreamToFile( InputStream inputStream, File file )
        throws IOException
    {
        FileOutputStream fos = new FileOutputStream( file );
        try
        {
            IOUtil.copy( inputStream, fos );
            fos.flush();
        }
        finally
        {
            if ( fos != null )
            {
                fos.close();
            }
        }

    }

    public PlexusContainer getPlexusContainer()
    {
        return this.getContainer();
    }

    protected class TestItemEventListener
        implements EventListener
    {
        private List<AbstractEvent> events = new ArrayList<AbstractEvent>();

        public List<AbstractEvent> getEvents()
        {
            return events;
        }

        public AbstractEvent getFirstEvent()
        {
            if ( events.size() > 0 )
            {
                return events.get( 0 );
            }
            else
            {
                return null;
            }
        }

        public AbstractEvent getLastEvent()
        {
            if ( events.size() > 0 )
            {
                return events.get( events.size() - 1 );
            }
            else
            {
                return null;
            }
        }

        public void reset()
        {
            events.clear();
        }

        public void onProximityEvent( AbstractEvent evt )
        {
            if ( RepositoryItemEvent.class.isAssignableFrom( evt.getClass() ) )
            {
                events.add( evt );
            }
        }
    }

    protected class TestRepositoryEventListener
        implements EventListener
    {
        private List<AbstractEvent> events = new ArrayList<AbstractEvent>();

        public List<AbstractEvent> getEvents()
        {
            return events;
        }

        public AbstractEvent getFirstEvent()
        {
            if ( events.size() > 0 )
            {
                return events.get( 0 );
            }
            else
            {
                return null;
            }
        }

        public AbstractEvent getLastEvent()
        {
            if ( events.size() > 0 )
            {
                return events.get( events.size() - 1 );
            }
            else
            {
                return null;
            }
        }

        public void reset()
        {
            events.clear();
        }

        public void onProximityEvent( AbstractEvent evt )
        {
            if ( !RepositoryItemEvent.class.isAssignableFrom( evt.getClass() ) )
            {
                events.add( evt );
            }
        }
    }

}
