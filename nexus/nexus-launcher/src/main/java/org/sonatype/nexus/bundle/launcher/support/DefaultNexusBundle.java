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
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.bundle.launcher.NexusBundle;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.sisu.bl.support.DefaultWebBundle;
import org.sonatype.sisu.bl.support.port.PortReservationService;
import org.sonatype.sisu.filetasks.FileTaskBuilder;
import org.sonatype.sisu.jsw.exec.JSWExec;
import org.sonatype.sisu.jsw.exec.JSWExecFactory;
import org.sonatype.sisu.jsw.monitor.CommandMonitorTalker;
import org.sonatype.sisu.jsw.monitor.KeepAliveThread;
import org.sonatype.sisu.jsw.monitor.internal.log.Slf4jLogProxy;
import org.sonatype.sisu.jsw.util.JSWConfig;

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
     * Used to reserve custom overlord ports.
     * Cannot be null.
     */
    private final PortReservationService portService;

    /**
     * Port on which Nexus JSW Monitor is running. 0 (zero) if application is not running.
     */
    private int jswMonitorPort;

    /**
     * Port on which Nexus JSW Keep Alive is running. 0 (zero) if application is not running.
     */
    private int jswKeepAlivePort;

    /**
     * JSW keep alive thread. Null if server is not started.
     */
    private KeepAliveThread keepAliveThread;

    /**
     * Constructor.
     *
     * @param jswExecFactory JSW executor factory.
     * @since 2.0
     */
    @Inject
    public DefaultNexusBundle( final JSWExecFactory jswExecFactory,
                               final FileTaskBuilder fileTaskBuilder,
                               final PortReservationService portService )
    {
        super( "nexus" );
        this.fileTaskBuilder = fileTaskBuilder;
        this.jswExecFactory = checkNotNull( jswExecFactory );
        this.portService = checkNotNull( portService );
    }

    /**
     * Additionally <br/>
     * - configures Nexus/Jetty port<br/>
     * - installs JSW command monitor<br/>
     * - installs JSW keep alive monitor<br/>
     * - configure remote debugging if requested<br/>
     * - installs plugins.
     * <p/>
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    protected void configure()
    {
        super.configure();

        jswMonitorPort = portService.reservePort();
        jswKeepAlivePort = portService.reservePort();

        configureJSW();
        installPlugins();
        configureNexusPort();
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

        if ( jswMonitorPort > 0 )
        {
            portService.cancelPort( jswMonitorPort );
            jswMonitorPort = 0;
        }
        if ( jswKeepAlivePort > 0 )
        {
            portService.cancelPort( jswKeepAlivePort );
            jswKeepAlivePort = 0;
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
        CommandMonitorTalker.installStopShutdownHook( jswMonitorPort );
        try
        {
            keepAliveThread = new KeepAliveThread( jswKeepAlivePort, new Slf4jLogProxy( log() ) );
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
            jswExec = jswExecFactory.create( getConfiguration().getTargetDirectory(), "nexus" );
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
     * Configures "application-port" nexus property.
     */
    private void configureNexusPort()
    {
        onDirectory( getConfiguration().getTargetDirectory() ).apply(
            fileTaskBuilder.properties( path( "nexus/conf/nexus.properties" ) )
                .property( "application-port", String.valueOf( getPort() ) )
        );
    }

    /**
     * Creates a JSW configuration file specifying:<br/>
     * - debugging options if debugging is enabled<br/>
     * - installs JSW command monitor
     *
     * @throws RuntimeException if a problem occurred during reading of JSW configuration or writing the additional JSW
     *                          configuration file
     */
    private void configureJSW()
        throws RuntimeException
    {
        try
        {
            NexusBundleConfiguration config = getConfiguration();

            File jswConfigFile = new File( config.getTargetDirectory(), "nexus/bin/jsw/conf/wrapper.conf" );
            File jswAddonConfigFile = new File( config.getTargetDirectory(), "nexus/conf/wrapper-override.conf" );

            JSWConfig jswConfig = new JSWConfig( jswConfigFile, jswAddonConfigFile );
            jswConfig.load();

            jswConfig.configureMonitor( jswMonitorPort );
            jswConfig.configureKeepAlive( jswKeepAlivePort );

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

            jswConfig.save();

        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public File getWorkDirectory()
    {
        return new File( getConfiguration().getTargetDirectory(), "sonatype-work/nexus" );
    }

}
