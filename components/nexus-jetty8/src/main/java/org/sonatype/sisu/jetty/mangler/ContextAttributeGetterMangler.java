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

package org.sonatype.sisu.jetty.mangler;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;

/**
 * Sets context attribute.
 *
 * @author cstamas
 */
public class ContextAttributeGetterMangler
    extends AbstractContextMangler
    implements ServerMangler<Object>
{
  private final String attributeKey;

  public ContextAttributeGetterMangler(final String contextPath, final String attributeKey) {
    super(contextPath);
    this.attributeKey = attributeKey;
  }

  public Object mangle(final Server server) {
    ContextHandler ctx = getContext(server);

    if (ctx != null && ctx.getServletContext() != null) {
      // try with servlet context is available, it falls back to attributes anyway
      return ctx.getServletContext().getAttribute(attributeKey);
    }
    else if (ctx != null) {
      // try plain jetty attributes
      return ctx.getAttribute(attributeKey);
    }
    else {
      return null;
    }
  }
}
