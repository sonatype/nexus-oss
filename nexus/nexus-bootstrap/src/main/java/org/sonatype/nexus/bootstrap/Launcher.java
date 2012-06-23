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
package org.sonatype.nexus.bootstrap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.Factory;
import org.sonatype.appcontext.publisher.EntryPublisher;
import org.sonatype.appcontext.publisher.SystemPropertiesEntryPublisher;
import org.sonatype.appcontext.source.PropertiesEntrySource;
import org.sonatype.appcontext.source.StaticEntrySource;
import org.sonatype.sisu.jetty.Jetty8;

/**
 * Nexus bootstrap launcher.
 *
 * @since 2.1
 */
public class Launcher
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final String BUNDLEBASEDIR_KEY = "bundleBasedir";

    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    private Jetty8 server;

    public Integer start(final String[] args) throws Exception {
        if (args.length != 1) {
            log.error("Missing Jetty configuration file parameter");
            return 1; // exit
        }

        AppContext context = createAppContext();
        server = new Jetty8(new File(args[0]), context);

        ensureTmpDirSanity();
        maybeEnableCommandMonitor();

        server.startJetty();
        return null; // continue running
    }

    private AppContext createAppContext() throws Exception {
        File cwd = new File(".").getCanonicalFile();
        log.info("Current directory: {}", cwd);

        // we have three properties file:
        // default.properties -- embedded in this jar (not user editable)
        // this is the place to set java.io.tmp and debug options by users

        // nexus.properties -- mandatory, will be picked up into context
        // this is place to set nexus properties like workdir location etc (as today)

        // nexus-test.properties -- optional, if present, will override values from those above
        // this is place to set test properties (like jetty port) etc

        // we "push" whole app context into system properties

        // create app context request, with ID "nexus", without parent, and due to NEXUS-4520 add "plexus" alias too
        final AppContextRequest request = Factory.getDefaultRequest("nexus", null, Arrays.asList("plexus"));

        // NOTE: sources list is "ascending by importance", 1st elem in list is "weakest" and last elem in list is
        // "strongest" (overrides). Factory already created us some sources, so we are just adding to that list without
        // disturbing the order of the list (we add to list head and tail)

        // add the defaults as least important, is mandatory to be present
        addProperties(request, "defaults", "default.properties", true);

        // NOTE: These are loaded as resources, and its expected that <install>/conf is included in the classpath

        // add the nexus.properties, is mandatory to be present
        addProperties(request, "nexus", "/nexus.properties", true);

        // add the nexus-test.properties, not mandatory to be present
        addProperties(request, "nexus-test", "/nexus-test.properties", false);

        // ultimate source of "bundleBasedir" (hence, is added as last in sources list)
        // Now, that will be always overridden by value got from cwd and that seems correct to me
        request.getSources().add(new StaticEntrySource(BUNDLEBASEDIR_KEY, cwd.getAbsolutePath()));

        // by default, publishers list will contain one "dump" publisher and hence, on creation, a dump will be written
        // out (to System.out or SLF4J logger, depending is latter on classpath or not)
        // if we dont want to "mute" this dump, just uncomment this below
        // request.getPublishers().clear();

        // publishers (order does not matter for us, unlike sources)
        // we need to publish one property: "bundleBasedir"
        request.getPublishers().add(new EntryPublisher()
        {
            @Override
            public void publishEntries(final AppContext context) {
                System.setProperty(BUNDLEBASEDIR_KEY, String.valueOf(context.get(BUNDLEBASEDIR_KEY)));
            }
        });

        // TODO: Canonical-ize nexus-work

        // we need to publish all entries coming from loaded properties
        request.getPublishers().add(new SystemPropertiesEntryPublisher(true));

        // create the context and use it as "parent" for Jetty8
        // when context created, the context is built and all publisher were invoked (system props set for example)
        return Factory.create(request);
    }

    private Properties loadProperties(final Resource resource) throws IOException {
        assert resource != null;
        log.debug("Loading properties from: {}", resource);
        Properties props = new Properties();
        InputStream input = resource.getInputStream();
        try {
            props.load(input);
            if (log.isDebugEnabled()) {
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    log.debug("  {}='{}'", entry.getKey(), entry.getValue());
                }
            }
        }
        finally {
            input.close();
        }
        return props;
    }

    private Properties loadProperties(final String resource, final boolean required) throws IOException {
        URL url = getClass().getResource(resource);
        if (url == null) {
            if (required) {
                log.error("Missing resource: {}", resource);
                throw new IOException("Missing resource: " + resource);
            }
            else {
                log.debug("Missing optional resource: {}", resource);
            }
            return null;
        }
        else {
            return loadProperties(Resource.newResource(url));
        }
    }

    private void addProperties(final AppContextRequest request, final String name, final String resource, final boolean required) throws IOException {
        Properties props = loadProperties(resource, required);
        if (props != null) {
            request.getSources().add(new PropertiesEntrySource(name, props));
        }
    }

    protected void ensureTmpDirSanity() throws IOException {
        // Make sure that java.io.tmpdir points to a real directory
        String tmp = System.getProperty(JAVA_IO_TMPDIR, "tmp");
        File dir = new File(tmp).getCanonicalFile();
        log.info("Temp directory: {}", dir);

        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.debug("Created tmp dir: {}", dir);
            }
        }
        else if (!dir.isDirectory()) {
            log.warn("Tmp dir is configured to a location which is not a directory: {}", dir);
        }

        // Ensure we can actually create a new tmp file
        File file = File.createTempFile(getClass().getSimpleName(), ".tmp");
        file.createNewFile();
        file.delete();

        System.setProperty(JAVA_IO_TMPDIR, dir.getAbsolutePath());
    }

    protected void maybeEnableCommandMonitor() throws IOException {
        String commandMonitorPort = System.getProperty(CommandMonitorThread.class.getName() + ".port");
        if (commandMonitorPort != null) {
            new CommandMonitorThread(this, Integer.parseInt(commandMonitorPort)).start();
        }
    }

    public void commandStop() {
        System.exit(0);
    }

    public void commandRestart() {
        log.error("Restart not supported, stopping instead");
        System.exit(0);
    }

    public void stop() throws Exception {
        server.stopJetty();
    }

    public static void main(final String[] args) throws Exception {
        new Launcher().start(args);
    }
}
