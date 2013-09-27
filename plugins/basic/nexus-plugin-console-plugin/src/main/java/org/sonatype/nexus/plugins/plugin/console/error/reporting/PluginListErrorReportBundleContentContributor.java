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

package org.sonatype.nexus.plugins.plugin.console.error.reporting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.error.report.ErrorReportBundleContentContributor;
import org.sonatype.nexus.error.report.ErrorReportBundleEntry;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.plugin.console.PluginConsoleManager;
import org.sonatype.nexus.plugins.plugin.console.model.PluginInfo;

import com.thoughtworks.xstream.XStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Plugin-list {@link ErrorReportBundleContentContributor}.
 */
@Named
@Singleton
public class PluginListErrorReportBundleContentContributor
    extends AbstractLoggingComponent
    implements ErrorReportBundleContentContributor
{
  private final PluginConsoleManager pluginConsoleManager;

  @Inject
  public PluginListErrorReportBundleContentContributor(final PluginConsoleManager pluginConsoleManager) {
    this.pluginConsoleManager = checkNotNull(pluginConsoleManager);
  }

  public ErrorReportBundleEntry[] getEntries() {
    List<PluginInfo> l = pluginConsoleManager.listPluginInfo();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    XStream xs = new XStream();
    xs.alias("PluginInfo", PluginInfo.class);
    xs.toXML(l, bos);

    return new ErrorReportBundleEntry[]{
        new ErrorReportBundleEntry("PluginsInfo.xml", new ByteArrayInputStream(bos.toByteArray()))
    };
  }
}
