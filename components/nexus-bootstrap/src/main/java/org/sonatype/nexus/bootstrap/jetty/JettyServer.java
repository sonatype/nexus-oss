package org.sonatype.nexus.bootstrap.jetty;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.sonatype.appcontext.internal.Preconditions.checkNotNull;

// NOTE: Copied and massaged from org.eclipse.jetty.xml.XmlConfiguration#main()

/**
 * Jetty server.
 *
 * @since 2.8
 */
public class JettyServer
{
  private static final Logger log = LoggerFactory.getLogger(JettyServer.class);

  private final String[] args;

  private final List<LifeCycle> components = new ArrayList<>();

  public JettyServer(final String[] args) {
    this.args = checkNotNull(args);
  }

  public synchronized void start() throws Exception {
    if (!components.isEmpty()) {
      throw new IllegalStateException("Already started");
    }

    log.info("Starting");

    final AtomicReference<Throwable> exception = new AtomicReference<>();

    AccessController.doPrivileged(new PrivilegedAction<Object>()
    {
      public Object run() {
        try {
          Properties properties = new Properties();
          properties.putAll(System.getProperties());

          // For all arguments, load properties or parse XMLs
          XmlConfiguration last = null;
          Object[] obj = new Object[args.length];
          for (int i = 0; i < args.length; i++) {
            if (args[i].toLowerCase(Locale.ENGLISH).endsWith(".properties")) {
              log.info("Loading properties: {}", args[i]);

              properties.load(Resource.newResource(args[i]).getInputStream());
            }
            else {
              log.info("Applying configuration: {}", args[i]);

              XmlConfiguration configuration = new XmlConfiguration(Resource.newResource(args[i]).getURL());
              if (last != null) {
                configuration.getIdMap().putAll(last.getIdMap());
              }

              if (!properties.isEmpty()) {
                Map<String, String> props = new HashMap<>();
                for (Object key : properties.keySet()) {
                  props.put(key.toString(), String.valueOf(properties.get(key)));
                }
                configuration.getProperties().putAll(props);
              }

              obj[i] = configuration.configure();
              last = configuration;
            }
          }

          // For all objects created by XmlConfigurations, start them if they are lifecycles.
          for (int i = 0; i < args.length; i++) {
            if (obj[i] instanceof LifeCycle) {
              LifeCycle lc = (LifeCycle) obj[i];

              log.info("Starting component: {}", lc);
              components.add(lc);

              if (!lc.isRunning()) {
                lc.start();
              }
            }
          }
        }
        catch (Exception e) {
          log.debug(Log.EXCEPTION, e);
          exception.set(e);
        }
        return null;
      }
    });

    Throwable th = exception.get();
    if (th != null)
    {
      log.error("Failed to start components", th);

      if (th instanceof RuntimeException) {
        throw (RuntimeException)th;
      }
      else if (th instanceof Exception) {
        throw (Exception)th;
      }
      else if (th instanceof Error) {
        throw (Error)th;
      }
      throw new Error(th);
    }

    // complain if no components were started
    if (components.isEmpty()) {
      throw new Exception("Failed to start any components");
    }

    log.info("Started {} components", components.size());
  }

  public synchronized void stop() throws Exception {
    if (components.isEmpty()) {
      throw new IllegalStateException("Not started");
    }

    log.info("Stopping {} components", components.size());

    Collections.reverse(components);

    for (LifeCycle lc : components) {
      if (!lc.isRunning()) {
        log.info("Stopping component: {}", lc);
        lc.stop();
      }
    }

    components.clear();
  }
}
