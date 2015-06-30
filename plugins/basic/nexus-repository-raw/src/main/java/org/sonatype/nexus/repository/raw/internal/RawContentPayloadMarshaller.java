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
package org.sonatype.nexus.repository.raw.internal;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;

import org.sonatype.nexus.repository.raw.RawContent;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.StreamPayload;
import org.sonatype.nexus.repository.view.payloads.StreamPayload.InputStreamSupplier;

import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converts between {@link Payload} and {@link RawContent}
 *
 * @since 3.0
 */
public class RawContentPayloadMarshaller
{
  private RawContentPayloadMarshaller() { }

  public static RawContent toContent(final Payload payload, final DateTime lastVerified) {
    return new RawContent()
    {
      @Override
      public String getContentType() {
        return payload.getContentType();
      }

      @Override
      public long getSize() {
        return payload.getSize();
      }

      @Override
      public InputStream openInputStream() throws IOException {
        return payload.openInputStream();
      }

      @Override
      public DateTime getLastVerified() {
        return lastVerified;
      }
    };
  }

  public static Payload toPayload(final RawContent content) throws IOException {
    checkNotNull(content);
    return new StreamPayload(
        new InputStreamSupplier() {
          @Nonnull
          @Override
          public InputStream get() throws IOException {
            return content.openInputStream();
          }
        },
        content.getSize(),
        content.getContentType()
    );
  }
}
