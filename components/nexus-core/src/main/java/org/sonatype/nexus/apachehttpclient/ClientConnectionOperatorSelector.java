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

package org.sonatype.nexus.apachehttpclient;

import org.apache.http.HttpHost;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

/**
 * A strategy for {@link ClientConnectionOperator} selection based on http host, http context and http parameters.
 * <p/>
 * If selector does not return an operator, other eventual selectors will be used. If none of them returns an operator,
 * a default operator will be used.
 *
 * @since 2.4
 */
public interface ClientConnectionOperatorSelector
{

  /**
   * Selects an operator based on http host, http context and http parameters.
   *
   * @param host    http host (not null)
   * @param context http context (not null)
   * @param params  http parameters (not null)
   * @return an operator or null if selector cannot determine one based on provided parameters
   */
  ClientConnectionOperator get(HttpHost host, HttpContext context, HttpParams params);

}
