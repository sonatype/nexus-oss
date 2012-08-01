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
package org.sonatype.nexus.bundle.launcher.support;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static org.sonatype.sisu.filetasks.FileTaskRunner.onDirectory;
import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.filetasks.builder.FileRef.path;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.bootstrap.KeepAliveThread;
import org.sonatype.nexus.bootstrap.Launcher;
import org.sonatype.nexus.bundle.launcher.NexusBundle;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.sisu.bl.support.DefaultWebBundle;
import org.sonatype.sisu.bl.support.RunningBundles;
import org.sonatype.sisu.bl.support.port.PortReservationService;
import org.sonatype.sisu.filetasks.FileTaskBuilder;
import org.sonatype.sisu.jsw.exec.JSWExec;
import org.sonatype.sisu.jsw.exec.JSWExecFactory;
import org.sonatype.sisu.jsw.util.JSWConfig;
import com.google.common.base.Throwables;

/**
 * Default Nexus bundle implementation.
 *
 * @since 2.0
 */
@Named
public class DefaultNexusBundle
    extends DefaultWebBundle<NexusBundle, NexusBundleConfiguration>
    implements NexusBundle
{

    /**
     * JSW utility factory.
     * Cannot be null.
     */
    private final JSWExecFactory jswExecFactory;

    /**
     * JSW utility to star/stop Nexus.
     * Lazy created.
     */
    private JSWExec jswExec;

    /**
     * File task builder.
     * Cannot be null.
     */
    private final FileTaskBuilder fileTaskBuilder;

    /**
     * Port on which Nexus Command Monitor is running. 0 (zero) if application is not running.
     */
    private int commandMonitorPort;

    /**
     * Port on which Nexus Keep Alive is running. 0 (zero) if application is not running.
     */
    private int keepAlivePort;

    /**
     * Keep alive thread. Null if server is not started.
     */
    private KeepAliveThread keepAliveThread;

    /**
     * Constructor.
     *
     * @param jswExecFactory JSW executor factory.
     * @since 2.0
     */
    @Inject
    public DefaultNexusBundle( final Provider<NexusBundleConfiguration> configurationProvider,
                               final RunningBundles runningBundles,
                               final FileTaskBuilder fileTaskBuilder,
                               final PortReservationService portReservationService,
                               final JSWExecFactory jswExecFactory )
    {
        super( "nexus", configurationProvider, runningBundles, fileTaskBuilder, portReservationService );
        this.fileTaskBuilder = fileTaskBuilder;
        this.jswExecFactory = checkNotNull( jswExecFactory );
    }

    /**
     * Additionally <br/>
     * - configures Nexus/Jetty port<br/>
     * - installs command monitor<br/>
     * - installs keep alive monitor<br/>
     * - configure remote debugging if requested<br/>
     * - installs plugins.
     * <p/>
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    protected void configure()
        throws Exception
    {
        super.configure();

        commandMonitorPort = getPortReservationService().reservePort();
        keepAlivePort = getPortReservationService().reservePort();

        configureJSW();
        configureNexusProperties();
        installPlugins();
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    protected void unconfigure()
    {
        super.unconfigure();

        if ( commandMonitorPort > 0 )
        {
            getPortReservationService().cancelPort( commandMonitorPort );
            commandMonitorPort = 0;
        }
        if ( keepAlivePort > 0 )
        {
            getPortReservationService().cancelPort( keepAlivePort );
            keepAlivePort = 0;
        }
    }

    /**
     * Starts Nexus using JSW.
     * <p/>
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    protected void startApplication()
    {
        try
        {
            keepAliveThread = new KeepAliveThread( keepAlivePort );
            keepAliveThread.start();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Could not start JSW keep alive thread", e );
        }
        jswExec().start();
    }

    /**
     * Stops Nexus using JSW.
     * <p/>
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    protected void stopApplication()
    {
        try
        {
            jswExec().stop();
        }
        finally
        {
            if ( keepAliveThread != null )
            {
                keepAliveThread.stopRunning();
            }
        }
    }

    /**
     * Checks if Nexus is alive by using REST status service.
     *
     * @return true if Nexus is alive
     * @since 2.0
     */
    @Override
    protected boolean applicationAlive()
    {
        try
        {
            return RequestUtils.isNexusRESTStarted( getUrl().toExternalForm() );
        }
        catch ( IOException ignore )
        {
            return false;
        }
    }

    /**
     * Lazy creates and returns JSW utility.
     *
     * @return JSW utility (never null)
     */
    private JSWExec jswExec()
    {
        if ( jswExec == null )
        {
            jswExec = jswExecFactory.create( getConfiguration().getTargetDirectory(), "nexus", commandMonitorPort );
        }
        return jswExec;
    }

    /**
     * Install Nexus plugins in {@code sonatype-work/nexus/plugin-repository}.
     */
    private void installPlugins()
    {
        NexusBundleConfiguration config = getConfiguration();
        List<File> plugins = config.getPlugins();
        for ( File plugin : plugins )
        {
            if ( plugin.isDirectory() )
            {
                onDirectory( config.getTargetDirectory() ).apply(
                    fileTaskBuilder.copy()
                        .directory( file( plugin ) )
                        .to().directory( path( "sonatype-work/nexus/plugin-repository" ) )
                );
            }
            else
            {
                onDirectory( config.getTargetDirectory() ).apply(
                    fileTaskBuilder.expand( file( plugin ) )
                        .to().directory( path( "sonatype-work/nexus/plugin-repository" ) )
                );
            }
        }
    }

    /**
     * Configures Nexus properties, depending on nexus version.
     * If Nexus is using nexus-bootstrap (after Nexus 2.1) will use "conf/nexus.properties" otherwise will use
     * "conf/nexus-test.properties" that is used as an override.
     */
    private void configureNexusProperties()
    {
        if ( isNexusVersion21OrBigger() )
        {
            final Properties nexusProperties = new Properties();

            nexusProperties.setProperty( "application-port", String.valueOf( getPort() ) );
            nexusProperties.setProperty( Launcher.COMMAND_MONITOR_PORT, String.valueOf( commandMonitorPort ) );
            nexusProperties.setProperty( Launcher.KEEP_ALIVE_PORT, String.valueOf( keepAlivePort ) );

            final Map<String, String> systemProperties = getConfiguration().getSystemProperties();
            if ( !systemProperties.isEmpty() )
            {
                for ( final Map.Entry<String, String> entry : systemProperties.entrySet() )
                {
                    nexusProperties.setProperty( entry.getKey(), entry.getValue() == null ? "true" : entry.getValue() );
                }
            }

            onDirectory( getConfiguration().getTargetDirectory() ).apply(
                fileTaskBuilder.properties( path( "nexus/conf/nexus-test.properties" ) )
                    .properties( nexusProperties )
            );
        }
        else
        {
            onDirectory( getConfiguration().getTargetDirectory() ).apply(
                fileTaskBuilder.properties( path( "nexus/conf/nexus.properties" ) )
                    .property( "application-port", String.valueOf( getPort() ) )
            );
        }
    }

    /**
     * Creates a JSW configuration file specifying:<br/>
     * - debugging options if debugging is enabled<br/>
     * - installs JSW command monitor
     */
    private void configureJSW()
    {
        try
        {
            final NexusBundleConfiguration config = getConfiguration();

            final File jswConfigFile = new File( config.getTargetDirectory(), "nexus/bin/jsw/conf/wrapper.conf" );

            final JSWConfig jswConfig = new JSWConfig(
                jswConfigFile,
                "The following properties are added by Nexus IT as an override of properties already configured"
            ).load();

            // For Nexus versions not 2.1 or bigger we will replace the main class and use sisu-jsw-utils threads and
            // configuration file for setting system properties
            if ( !isNexusVersion21OrBigger() )
            {
                jswConfig.configureMonitor( commandMonitorPort );
                jswConfig.configureKeepAlive( keepAlivePort );

                final Map<String, String> systemProperties = config.getSystemProperties();
                if ( !systemProperties.isEmpty() )
                {
                    for ( final Map.Entry<String, String> entry : systemProperties.entrySet() )
                    {
                        jswConfig.addIndexedProperty(
                            "wrapper.java.additional",
                            format( "-D%s=%s", entry.getKey(), entry.getValue() == null ? "true" : entry.getValue() )
                        );
                    }
                }
            }

            // configure remote debug if requested
            if ( config.getDebugPort() > 0 )
            {
                jswConfig.addIndexedProperty( "wrapper.java.additional", "-Xdebug" );
                jswConfig.addIndexedProperty( "wrapper.java.additional", "-Xnoagent" );
                jswConfig.addIndexedProperty( "wrapper.java.additional", "-Djava.compiler=NONE" );
                jswConfig.addIndexedProperty( "wrapper.java.additional",
                                              "-Xrunjdwp:transport=dt_socket,server=y,suspend="
                                                  + ( config.isSuspendOnStart() ? "y" : "n" )
                                                  + ",address=" + config.getDebugPort() );
            }

            jswConfig.save();
        }
        catch ( final IOException e )
        {
            throw Throwables.propagate( e );
        }
    }

    /**
     * Determines if Nexus version is bigger then 2.1 (one that uses nexus-bootstrap.
     *
     * @return true if Nexus version is bigger then 2.1
     *         <p/>
     *         TODO this is not a very proof way of determining version
     */
    private boolean isNexusVersion21OrBigger()
    {
        try
        {
            final File jswConfigFile = new File(
                getConfiguration().getTargetDirectory(), "nexus/bin/jsw/conf/wrapper.conf"
            );
            final JSWConfig jswConfig = new JSWConfig( jswConfigFile ).load();
            final String mainClass = jswConfig.getProperty( "wrapper.java.mainclass" );

            return mainClass.startsWith( "org.sonatype.nexus.bootstrap" );
        }
        catch ( final IOException e )
        {
            throw Throwables.propagate( e );
        }
    }

    @Override
    public File getWorkDirectory()
    {
        return new File( getConfiguration().getTargetDirectory(), "sonatype-work/nexus" );
    }

    @Override
    protected String generateId()
    {
        return getName() + "-" + System.currentTimeMillis();
    }
}
