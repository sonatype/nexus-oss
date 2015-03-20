/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.view.payloads;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.view.Payload;

import com.google.common.collect.Maps;
import org.apache.http.HttpEntity;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Decorates existing payload with some spice.
 *
 * @since 3.0
 */
public class DecoratedPayload
    implements Payload
{
  private final Payload payload;

  private final Map<Class<?>, Object> decoratorsMap;

  public DecoratedPayload(final Payload payload, final Object... decorators) {
    this.payload = checkNotNull(payload);
    this.decoratorsMap = Maps.newHashMap();
    for (Object decorator : decorators) {
      decoratorsMap.put(decorator.getClass(), decorator);
    }
  }

  @Override
  public InputStream openInputStream() throws IOException {
    return payload.openInputStream();
  }

  @Override
  public long getSize() {
    return payload.getSize();
  }

  @Nullable
  @Override
  public String getContentType() {
    return payload.getContentType();
  }

  @Nullable
  public <T> T decorator(Class<T> clazz) {
    return (T) decoratorsMap.get(clazz);
  }
}