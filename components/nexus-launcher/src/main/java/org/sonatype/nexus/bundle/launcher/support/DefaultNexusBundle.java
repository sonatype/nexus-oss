/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
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

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.bootstrap.Launcher;
import org.sonatype.nexus.bootstrap.monitor.CommandMonitorTalker;
import org.sonatype.nexus.bootstrap.monitor.CommandMonitorThread;
import org.sonatype.nexus.bootstrap.monitor.KeepAliveThread;
import org.sonatype.nexus.bootstrap.monitor.commands.ExitCommand;
import org.sonatype.nexus.bootstrap.monitor.commands.HaltCommand;
import org.sonatype.nexus.bootstrap.monitor.commands.PingCommand;
import org.sonatype.nexus.bootstrap.monitor.commands.StopApplicationCommand;
import org.sonatype.nexus.bootstrap.monitor.commands.StopMonitorCommand;
import org.sonatype.nexus.bundle.launcher.NexusBundle;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.sisu.bl.support.DefaultWebBundle;
import org.sonatype.sisu.bl.support.RunningBundles;
import org.sonatype.sisu.bl.support.TimedCondition;
import org.sonatype.sisu.bl.support.port.PortReservationService;
import org.sonatype.sisu.filetasks.FileTaskBuilder;
import org.sonatype.sisu.goodies.common.Time;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.condition.Os;

import static org.sonatype.nexus.bootstrap.monitor.CommandMonitorThread.LOCALHOST;
import static org.sonatype.sisu.filetasks.FileTaskRunner.onDirectory;
import static org.sonatype.sisu.filetasks.builder.FileRef.path;
import static org.sonatype.sisu.goodies.common.SimpleFormat.format;

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
  private CommandMonitorThread keepAliveThread;

  private ConfigurationStrategy strategy;

  @Inject
  public DefaultNexusBundle(final Provider<NexusBundleConfiguration> configurationProvider,
                            final RunningBundles runningBundles,
                            final FileTaskBuilder fileTaskBuilder,
                            final PortReservationService portReservationService)
  {
    super("nexus", configurationProvider, runningBundles, fileTaskBuilder, portReservationService);
    this.fileTaskBuilder = fileTaskBuilder;
  }

  private void installStopShutdownHook(final int commandPort) {
    Thread stopShutdownHook = new Thread("Nexus Sanity Stopper")
    {
      @Override
      public void run() {
        terminateRemoteNexus(commandPort);
      }
    };

    Runtime.getRuntime().addShutdownHook(stopShutdownHook);
    log.debug("Installed stop shutdown hook");
  }

  private void sendStopToNexus(final int commandPort) {
    log.debug("Sending stop command to Nexus");
    try {
      // FIXME HOSTNAME should be configurable
      new CommandMonitorTalker(LOCALHOST, commandPort).send(StopApplicationCommand.NAME);
    }
    catch (Exception e) {
      log.debug(
          "Skipping exception got while sending stop command to Nexus {}:{}",
          e.getClass().getName(), e.getMessage()
      );
    }
  }

  // FIXME accept host to terminate
  private void terminateRemoteNexus(final int commandPort) {
    log.debug("attempting to terminate gracefully at {}", commandPort);

    // First attempt graceful shutdown
    sendStopToNexus(commandPort);

    // FIXME LOCALHOST should be getHostName
    CommandMonitorTalker talker = new CommandMonitorTalker(LOCALHOST, commandPort);
    long started = System.currentTimeMillis();
    long max = 5 * 60 * 1000; // wait 5 minutes for NX to shutdown, before attempting to halt it
    long period = 1000;

    // Then ping for a bit and finally give up and ask it to halt
    while (true) {
      try {
        talker.send(PingCommand.NAME);
      }
      catch (ConnectException e) {
        // likely its shutdown already
        break;
      }
      catch (Exception e) {
        // ignore, not sure there is much we can do
      }

      // If we have waited long enough, then ask remote to halt
      if (System.currentTimeMillis() - started > max) {
        try {
          talker.send(HaltCommand.NAME);
          break;
        }
        catch (Exception e) {
          // ignore, not sure there is much we can do
          break;
        }
      }

      // Wait a wee bit and try again
      try {
        Thread.sleep(period);
      }
      catch (InterruptedException e) {
        // ignore
      }
    }
  }

  private void sendStopToKeepAlive(final int commandPort) {
    log.debug("Sending stop command to keep alive thread");
    try {
      // FIXME replace LOCALHOST with getHostName
      new CommandMonitorTalker(LOCALHOST, commandPort).send(StopMonitorCommand.NAME);
    }
    catch (Exception e) {
      log.debug(
          "Skipping exception got while sending stop command to keep alive thread {}:{}",
          e.getClass().getName(), e.getMessage()
      );
    }
  }

  /**
   * Additionally <br/>
   * - configures Nexus/Jetty port<br/>
   * - installs command monitor<br/>
   * - installs keep alive monitor<br/>
   * - configure remote debugging if requested
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

    strategy = determineConfigurationStrategy();

    configureNexusProperties(strategy);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  protected void unconfigure() {
    super.unconfigure();

    if (commandMonitorPort > 0) {
      getPortReservationService().cancelPort(commandMonitorPort);
      commandMonitorPort = 0;
    }
    if (keepAlivePort > 0) {
      getPortReservationService().cancelPort(keepAlivePort);
      keepAlivePort = 0;
    }
    strategy = null;
  }

  /**
   * Starts Nexus.
   * <p/>
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  protected void startApplication() {
    try {
      keepAliveThread = new CommandMonitorThread(
          keepAlivePort,
          new PingCommand(),
          new StopMonitorCommand(),
          new ExitCommand(),
          new HaltCommand()
      );
      keepAliveThread.start();
    }
    catch (IOException e) {
      throw new RuntimeException("Could not start keep alive thread", e);
    }
    installStopShutdownHook(commandMonitorPort);

    final File nexusDir = getNexusDirectory();

    makeExecutable(nexusDir, "bin/*");

    // log whenever ports are configured to aid solving test port conflicts
    log.info("{} ({}) spawned env [{}={},{}={}]", getName(), getConfiguration().getId(),
        strategy.commandMonitorProperty(), commandMonitorPort, strategy.keepAliveProperty(), keepAlivePort);
    onDirectory(nexusDir).apply(
        fileTaskBuilder.exec().spawn()
            .script(path("bin/nexus" + (Os.isFamily(Os.FAMILY_WINDOWS) ? ".bat" : "")))
            .withArgument("start")
            .withEnv(strategy.commandMonitorProperty(), String.valueOf(commandMonitorPort))
            .withEnv(strategy.keepAliveProperty(), String.valueOf(keepAlivePort))
    );

    if (getConfiguration().isSuspendOnStart()) {
      // verify the debugger socket has been opened and is waiting for a debugger to connect
      // command monitor thread is not started while suspended so this is the best we can do
      final boolean jvmSuspended = new TimedCondition()
      {
        @Override
        protected boolean isSatisfied()
            throws Exception
        {
          Socket socket = new Socket();
          socket.setSoTimeout(5000);
          socket.connect(
              new InetSocketAddress(getConfiguration().getHostName(), getConfiguration().getDebugPort()));
          return true;
        }
      }.await(Time.seconds(10), Time.seconds(100), Time.seconds(1));
      if (jvmSuspended) {
        log.info("{} ({}) suspended for debugging at {}:{}", getName(), getConfiguration().getId(),
            getConfiguration().getHostName(), getConfiguration().getDebugPort());
      }
      else {
        throw new RuntimeException(
            format(
                "%s (%s) no open socket for debugging at %s:%s within 10 seconds", getName(),
                getConfiguration().getId(), getConfiguration().getHostName(),
                getConfiguration().getDebugPort()
            )
        );
      }
    }
    else {
      // when not suspending, we expect the internal command monitor thread to start well before bundle is ready
      // so we only give it 10 seconds to be available
      log.info("{} ({}) pinging command monitor at {}:{}", getName(), getConfiguration().getId(),
          getConfiguration().getHostName(), commandMonitorPort);
      final boolean monitorInstalled = new TimedCondition()
      {
        @Override
        protected boolean isSatisfied()
            throws Exception
        {
          // FIXME replace LOCALHOST with getHostName() after making default hostname be 127.0.0.1
          new CommandMonitorTalker(LOCALHOST, commandMonitorPort).send(PingCommand.NAME);
          return true;
        }
      }.await(Time.seconds(10), Time.seconds(100), Time.seconds(1));
      if (monitorInstalled) {
        log.debug("{} ({}) command monitor detected at {}:{}", getName(), getConfiguration().getId(),
            getConfiguration().getHostName(), commandMonitorPort);
      }
      else {
        throw new RuntimeException(
            format("%s (%s) no command monitor detected at %s:%s within 10 seconds", getName(),
                getConfiguration().getId(), getConfiguration().getHostName(),
                commandMonitorPort
            )
        );
      }
    }
  }

  /**
   * Stops Nexus.
   * <p/>
   * {@inheritDoc}
   *
   * @since 2.0
   */
  @Override
  protected void stopApplication() {
    // application may be in suspended state waiting for debugger to attach, if we can reasonably guess this is
    // the case, then we should resume the vm so that we can ask command monitor to immediately halt
    try {
      if (getConfiguration().isSuspendOnStart()) {

        boolean isSuspended = new TimedCondition()
        {
          @Override
          protected boolean isSatisfied()
              throws Exception
          {
            Socket socket = new Socket();
            socket.setSoTimeout(5000);
            socket.connect(
                new InetSocketAddress(getConfiguration().getHostName(),
                    getConfiguration().getDebugPort()));
            return true;
          }
        }.await(Time.seconds(1), Time.seconds(10), Time.seconds(1));

        if (isSuspended) {
          // FIXME avoiding the compile time dependency for now on jdi classes (DebuggerUtils)
          throw new RuntimeException(
              format(
                  "%s (%s) looks suspended at {}:{}, CANNOT STOP THIS BUNDLE!", getName(),
                  getConfiguration().getId(), getConfiguration().getHostName(),
                  getConfiguration().getDebugPort()
              )
          );
        }
      }

      terminateRemoteNexus(commandMonitorPort);

      try {
        // clear transient cache to avoid filesystem bloat when running ITs
        FileUtils.deleteDirectory(new File(getNexusDirectory(), "data/cache"));
      }
      catch (IOException e) {
        // couldn't delete directory, too bad
      }
    }
    finally {
      // Stop the launcher-controller-side monitor thread if there is one
      if (keepAliveThread != null) {
        sendStopToKeepAlive(keepAlivePort);
        keepAliveThread = null;
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
  protected boolean applicationAlive() {
    return RequestUtils.isNexusRESTStarted(getUrl().toExternalForm());
  }

  /**
   * Configure Nexus properties using provided configuration strategy.
   *
   * @param strategy configuration strategy
   */
  private static void configureNexusProperties(final ConfigurationStrategy strategy) {
    strategy.configureNexus();
  }

  /**
   * Determines a configuration strategy based on version of Nexus to be started.
   *
   * @return configuration strategy. Never null.
   */
  private ConfigurationStrategy determineConfigurationStrategy() {
    return new NexusOnKaraf();
  }

  @Override
  protected String composeApplicationURL() {
    return String.format("http://%s:%s/", getConfiguration().getHostName(), getPort());
  }

  @Override
  public File getWorkDirectory() {
    return new File(getConfiguration().getTargetDirectory(), "sonatype-work/nexus");
  }

  public File getNexusDirectory() {
    return new File(getConfiguration().getTargetDirectory(), "nexus");
  }

  @Override
  public File getNexusLog() {
    return new File(getWorkDirectory(), "logs/nexus.log");
  }

  @Override
  public File getLauncherLog() {
    return new File(getNexusDirectory(), "data/log/karaf.log");
  }

  @Override
  protected String generateId() {
    return "nx"; // TODO? use a system property if we should or not add: + "-" + System.currentTimeMillis();
  }

  private void makeExecutable(final File baseDir,
                              final String scriptName)
  {
    if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
      onDirectory(baseDir).apply(
          fileTaskBuilder.chmod(path("/"))
              .include("**/" + scriptName)
              .permissions("u+x")
      );
    }
  }

  private static interface ConfigurationStrategy
  {
    String commandMonitorProperty();

    String keepAliveProperty();

    void configureNexus();
  }

  private class NexusOnKaraf
      implements ConfigurationStrategy
  {
    @Override
    public String commandMonitorProperty() {
      return Launcher.COMMAND_MONITOR_PORT;
    }

    @Override
    public String keepAliveProperty() {
      return KeepAliveThread.KEEP_ALIVE_PORT;
    }

    @Override
    public void configureNexus() {
      final Properties nexusProperties = new Properties();

      nexusProperties.setProperty("application-port", String.valueOf(getPort()));

      final Map<String, String> systemProperties = getConfiguration().getSystemProperties();
      if (!systemProperties.isEmpty()) {
        for (final Map.Entry<String, String> entry : systemProperties.entrySet()) {
          nexusProperties.setProperty(entry.getKey(), entry.getValue() == null ? "true" : entry.getValue());
        }
      }

      onDirectory(getNexusDirectory()).apply(
          fileTaskBuilder.properties(path("etc/nexus-test.properties"))
              .properties(nexusProperties)
      );
    }

//TODO:KARAF customize Karaf settings for debug+JMX
//  @Override
//  public void configureJSW(final JSWConfig jswConfig) {
//    // configure remote debug if requested
//    if (getConfiguration().getDebugPort() > 0) {
//      jswConfig.addJavaStartupParameter("-Xdebug");
//      jswConfig.addJavaStartupParameter("-Xnoagent");
//      jswConfig.addJavaStartupParameter(
//          "-Xrunjdwp:transport=dt_socket,server=y,suspend="
//              + (getConfiguration().isSuspendOnStart() ? "y" : "n")
//              + ",address=" + getConfiguration().getDebugPort()
//      );
//      jswConfig.addJavaSystemProperty("java.compiler", "NONE");
//    }
//
//    JMXConfiguration jmxConfig = getConfiguration().getJmxConfiguration();
//    if (jmxConfig.getRemotePort() != null) {
//      Map<String, String> jmxProps = jmxConfig.getSystemProperties();
//      jmxProps.put(JMXConfiguration.PROP_COM_SUN_MANAGEMENT_JMXREMOTE_PORT, Integer.toString(getJmxRemotePort()));
//      jswConfig.addJavaSystemProperties(jmxProps);
//    }
//  }
  }

}
