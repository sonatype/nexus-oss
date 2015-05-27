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
package org.sonatype.nexus.repository.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.repository.InvalidContentException;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link ContentValidator}.
 *
 * @since 3.0
 */
@Named(DefaultContentValidator.NAME)
@Singleton
public class DefaultContentValidator
    extends ComponentSupport
    implements ContentValidator
{
  public static final String NAME = "default";

  private final MimeSupport mimeSupport;

  @Inject
  public DefaultContentValidator(final MimeSupport mimeSupport) {
    this.mimeSupport = checkNotNull(mimeSupport);
  }

  @Nonnull
  @Override
  public String determineContentType(boolean strictContentTypeValidation,
                                     Supplier<InputStream> contentSupplier,
                                     @Nullable String contentName,
                                     @Nullable String declaredContentType) throws IOException
  {
    checkNotNull(contentSupplier);
    String contentType = declaredContentType;

    if (contentType == null) {
      log.trace("Content {} has no declared content type.", contentName);
      try (InputStream is = contentSupplier.get()) {
        contentType = mimeSupport.detectMimeType(is, contentName);
      }
      log.debug("Mime support implies content type {}", contentType);

      if (contentType == null && strictContentTypeValidation) {
        throw new InvalidContentException("Content type could not be determined.");
      }
    }
    else {
      try (InputStream is = contentSupplier.get()) {
        final List<String> types = mimeSupport.detectMimeTypes(is, contentName);
        if (!types.isEmpty() && !types.contains(contentType)) {
          log.debug("Declared content type {}, discovered types {}", declaredContentType, types);
          if (strictContentTypeValidation) {
            throw new InvalidContentException(
                String.format("Declared content type %s, but discovered %s.", contentType, types));
          }
        }
      }
    }
    if (contentType == null) {
      contentType = ContentTypes.OCTET_STREAM;
    }
    return contentType;
  }
}
