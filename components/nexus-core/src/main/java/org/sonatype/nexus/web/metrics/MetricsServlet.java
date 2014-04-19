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
package org.sonatype.nexus.web.metrics;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonFactory;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.VirtualMachineMetrics;

/**
 * Customized {@link com.yammer.metrics.reporting.MetricsServlet} to support download.
 *
 * @since 3.0
 */
public class MetricsServlet
  extends com.yammer.metrics.reporting.MetricsServlet
{
  public MetricsServlet(final Clock clock,
                        final VirtualMachineMetrics vm,
                        final MetricsRegistry registry,
                        final JsonFactory factory,
                        final boolean showJvmMetrics)
  {
    super(clock, vm, registry, factory, showJvmMetrics);
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException
  {
    boolean download = Boolean.parseBoolean(req.getParameter("download"));
    if (download) {
      resp.addHeader("Content-Disposition", "attachment; filename='metrics.json'");
    }

    super.doGet(req, resp);
  }
}
