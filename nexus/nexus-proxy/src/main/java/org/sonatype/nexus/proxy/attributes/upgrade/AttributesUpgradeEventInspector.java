/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.attributes.upgrade;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.util.SystemPropertiesHelper;
import org.sonatype.plexus.appevents.Event;

/**
 * EventInspector that handles upgrade of "legacy attribute storage". It does it by detecting it's presence, and firing
 * the rebuild attributes background task if needed. Finally, it leaves "marker" file to mark the fact upgrade did
 * happen, to not kick in on any subsequent reboot.
 * 
 * @since 1.10.0
 */
@Component( role = EventInspector.class, hint = "AttributesUpgradeEventInspector" )
public class AttributesUpgradeEventInspector
    extends AbstractEventInspector
    implements EventInspector, AsynchronousEventInspector
{
    /**
     * The "switch" for performing upgrade (by bg thread), default is true (upgrade will happen).
     */
    private final boolean UPGRADE = SystemPropertiesHelper.getBoolean( getClass().getName() + ".upgrade", true );

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Override
    public boolean accepts( Event<?> evt )
    {
        return evt instanceof NexusStartedEvent;
    }

    @Override
    public void inspect( Event<?> evt )
    {
        final File legacyAttributesDirectory = applicationConfiguration.getWorkingDirectory( "proxy/attributes", false );

        if ( !legacyAttributesDirectory.isDirectory() )
        {
            // file not found or not a directory, stay put to not create noise in logs (new or tidied up nexus
            // instance)
        }
        else
        {
            if ( isUpgradeDone( legacyAttributesDirectory, null ) )
            {
                // nag the user to remove the directory
                getLogger().info(
                    "Legacy attribute directory present, but is marked already as upgraded. Please delete, move or rename the \""
                        + legacyAttributesDirectory.getAbsolutePath() + "\" folder." );
            }
            else
            {
                if ( UPGRADE )
                {
                    new UpgraderThread( legacyAttributesDirectory, repositoryRegistry ).start();
                }
                else
                {
                    getLogger().info(
                        "Legacy attribute directory present, but upgrade prevented by system property. Not upgrading it." );
                }
            }
        }
    }

    // ==

    private static final String MARKER_FILENAME = "README.txt";

    private static final String MARKER_TEXT =
        "Migration of legacy attributes finished.\nPlease delete, remove or rename this directory!";

    protected static boolean isUpgradeDone( final File attributesDirectory, final String repoId )
    {
        try
        {
            if ( StringUtils.isBlank( repoId ) )
            {
                return StringUtils.equals( MARKER_TEXT,
                    FileUtils.fileRead( new File( attributesDirectory, MARKER_FILENAME ) ) );
            }
            else
            {
                return StringUtils.equals( MARKER_TEXT,
                    FileUtils.fileRead( new File( new File( attributesDirectory, repoId ), MARKER_FILENAME ) ) );
            }
        }
        catch ( IOException e )
        {
            return false;
        }
    }

    protected static void markUpgradeDone( final File attributesDirectory, final String repoId )
    {
        try
        {
            if ( StringUtils.isBlank( repoId ) )
            {
                FileUtils.fileWrite( new File( attributesDirectory, MARKER_FILENAME ), MARKER_TEXT );
            }
            else
            {
                final File target = new File( new File( attributesDirectory, repoId ), MARKER_FILENAME );
                // this step is needed if new repo added while upgrade not done: it will NOT have legacy attributes
                // as other reposes, that were present in old/upgraded instance
                target.getParentFile().mkdirs();
                FileUtils.fileWrite( target, MARKER_TEXT );
            }
        }
        catch ( IOException e )
        {
            // hum?
        }
    }

    // ==

    protected static class UpgraderThread
        extends Thread
    {
        private Logger logger = LoggerFactory.getLogger( getClass() );

        private final File legacyAttributesDirectory;

        private final RepositoryRegistry repositoryRegistry;

        public UpgraderThread( final File legacyAttributesDirectory, final RepositoryRegistry repositoryRegistry )
        {
            this.legacyAttributesDirectory = legacyAttributesDirectory;
            this.repositoryRegistry = repositoryRegistry;
            // to have it clearly in thread dumps
            setName( "LegacyAttributesUpgrader" );
            // to not prevent sudden reboots (by user, if upgrading, and rebooting)
            setDaemon( true );
            // to not interfere much with other stuff
            setPriority( Thread.MIN_PRIORITY );
        }

        @Override
        public void run()
        {
            if ( !isUpgradeDone( legacyAttributesDirectory, null ) )
            {
                logger.info( "Legacy attribute directory present, and upgrade needed. Upgrading it in background thread." );
                ResourceStoreRequest req = new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT );
                List<Repository> reposes = repositoryRegistry.getRepositories();
                for ( Repository repo : reposes )
                {
                    if ( !isUpgradeDone( legacyAttributesDirectory, repo.getId() ) )
                    {
                        logger.info( "Upgrading legacy attributes of repository {}.",
                            RepositoryStringUtils.getHumanizedNameString( repo ) );
                        repo.recreateAttributes( req, null );
                        markUpgradeDone( legacyAttributesDirectory, repo.getId() );
                    }
                }
                markUpgradeDone( legacyAttributesDirectory, null );
                logger.info(
                    "Legacy attribute directory upgrade finished. Please delete, move or rename the \"{}\" folder.",
                    legacyAttributesDirectory.getAbsolutePath() );
            }
        }
    }
}
