/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.events;

import java.lang.management.ManagementFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.util.SystemPropertiesHelper;

/**
 * A simple "debug" event inspector that grabs all events sent to {@link EventInspector}s and simply dumps them as
 * strings to logger at INFO level. Enable it by setting system property
 * "org.sonatype.nexus.events.DebugEventInspector.enabled" have value of "true" and bouncing Nexus or simply over JMX
 * (see {@link DebugEventInspectorMBean}).
 *
 * @author cstamas
 * @since 2.1
 */
@Named
@Singleton
public class DebugEventInspector
    extends AbstractLoggingComponent
    implements EventInspector
{
  private static final String JMX_DOMAIN = "org.sonatype.nexus.events";

  private final boolean ENABLED_DEFAULT = SystemPropertiesHelper.getBoolean(
      "org.sonatype.nexus.events.DebugEventInspector.enabled", false);

  private volatile boolean enabled;

  private ObjectName jmxName;

  public DebugEventInspector() {
    this.enabled = ENABLED_DEFAULT;

    try {
      jmxName = ObjectName.getInstance(JMX_DOMAIN, "name", DebugEventInspector.class.getSimpleName());
      final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      if (server.isRegistered(jmxName)) {
        getLogger().warn("MBean already registered; replacing: {}", jmxName);
        server.unregisterMBean(jmxName);
      }
      server.registerMBean(new DefaultDebugEventInspectorMBean(this), jmxName);
    }
    catch (Exception e) {
      jmxName = null;
      getLogger().warn("Problem registering MBean for: " + getClass().getName(), e);
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public boolean accepts(Event<?> evt) {
    if (!enabled) {
      return false;
    }

    getLogger().info(String.valueOf(evt));

    return false;
  }

  @Override
  public void inspect(Event<?> evt) {
    // nop
  }
}
