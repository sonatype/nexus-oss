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
package org.sonatype.nexus.componentviews.internal;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.componentviews.View;
import org.sonatype.nexus.componentviews.ViewRegistry;
import org.sonatype.nexus.componentviews.ViewRequest;
import org.sonatype.nexus.web.ErrorStatusException;

import com.google.common.base.Throwables;

/**
 * The central servlet of the view framework, which dispatches incoming requests to the views available in the {@link
 * ViewRegistry}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ComponentViewServlet
    extends HttpServlet
{
  private final ViewRegistry viewRegistry;

  private final ViewModuleBooter startup;

  @Inject
  public ComponentViewServlet(final ViewRegistry viewRegistry, final ViewModuleBooter startup) {
    this.viewRegistry = viewRegistry;
    this.startup = startup;
  }

  private boolean initialized = false;

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException
  {
    final ViewNameParser viewNameParser = new ViewNameParser(req.getPathInfo());

    final View view = viewRegistry.getView(viewNameParser.getViewName());
    if (view == null) {
      throw new ErrorStatusException(HttpServletResponse.SC_NOT_FOUND, "Not Found", "View not found.");
    }

    final ViewRequest request = new WrappingViewRequest(req, viewNameParser.getViewName(), view.getConfig(),
        viewNameParser.getRemainingPath());

    try {
      view.dispatch(request).send(resp);
    }
    catch (Exception e) {

      Throwables.propagate(e);
    }
  }
}
