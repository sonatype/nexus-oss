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

package org.sonatype.nexus.repository.view;

import org.sonatype.nexus.repository.util.Attributes;

import com.google.common.collect.Maps;

/**
 * View response.
 *
 * @since 3.0
 */
public class Response
{
  private final Status status;

  private final Headers headers = new Headers();

  private final Attributes attributes = new Attributes(Maps.<String, Object>newHashMap());

  public Response(final Status status) {
    this.status = status;
  }

  public Status getStatus() {
    return status;
  }

  public Headers getHeaders() {
    return headers;
  }

  public Attributes getAttributes() {
    return attributes;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "status=" + status +
        '}';
  }
}
