/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.common.hash.HashAlgorithm;

import com.google.common.collect.Maps;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Content, that is wrapped {@link Payload} with {@link Attributes}.
 *
 * @since 3.0
 */
public class Content
    implements Payload
{
  /**
   * Key of the "last modified" attribute of type {@link DateTime}.
   */
  public static final String CONTENT_LAST_MODIFIED = "lastModified";

  /**
   * Key of the "etag" attribute of type {@link String}.
   */
  public static final String CONTENT_ETAG = "etag";

  /**
   * Key of the "hashCodes" attribute of type {@link Map}, with keys {@link HashAlgorithm} and values of {@link
   * HashCode>}.
   */
  public static final String CONTENT_HASH_CODES_MAP = "hashCodesMap";

  /**
   * Custom attributes to configure backing and logging.
   */
  private static class Attributes
      extends AttributesMap
  {
    public Attributes() {
      super(Maps.<String, Object>newHashMap());
    }
  }

  private final Payload payload;

  private final Attributes attributes;

  public Content(final Payload payload)
  {
    this.payload = checkNotNull(payload);
    this.attributes = new Attributes();
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

  @Nonnull
  public AttributesMap getAttributes() {
    return attributes;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "payload=" + payload +
        ", attributes='" + attributes + '\'' +
        '}';
  }
}
