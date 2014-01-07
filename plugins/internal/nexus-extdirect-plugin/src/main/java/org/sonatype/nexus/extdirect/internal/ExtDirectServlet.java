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

package org.sonatype.nexus.extdirect.internal;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.director.core.DirectConfiguration;
import com.director.core.Provider;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ext.Direct Servlet.
 *
 * @since 2.8
 */
@Named
@Singleton
public class ExtDirectServlet
    extends HttpServlet
{

  private static final String ENCODING = "UTF-8";

  private final DirectConfiguration configuration;

  @Inject
  public ExtDirectServlet(final ExtDirectConfiguration configuration) {
    this.configuration = checkNotNull(configuration);
  }

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException
  {
    response.setContentType("application/json");
    response.setCharacterEncoding(ENCODING);

    String providerId = request.getParameter(configuration.getProviderParamName());
    // TODO throw 403 if no provider id
    if (providerId != null) {
      Provider provider = configuration.getProvider(providerId);
      provider.process(request, response);
    }
  }

}
