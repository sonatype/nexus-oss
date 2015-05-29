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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.mime.MimeRulesSource;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.repository.InvalidContentException;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;

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
                                     @Nullable MimeRulesSource mimeRulesSource,
                                     @Nullable String contentName,
                                     @Nullable String declaredContentType) throws IOException
  {
    checkNotNull(contentSupplier);

    final List<String> contentDetectedMimeTypes = new ArrayList<>();
    try (InputStream is = contentSupplier.get()) {
      contentDetectedMimeTypes.addAll(mimeSupport.detectMimeTypes(is, contentName));
    }
    log.debug("Mime support detected content types {}", contentDetectedMimeTypes);

    if (strictContentTypeValidation && isUnknown(contentDetectedMimeTypes)) {
      throw new InvalidContentException("Content type could not be determined");
    }

    final List<String> nameAssumedMimeTypes = new ArrayList<>();
    if (contentName != null) {
      nameAssumedMimeTypes.addAll(mimeSupport.guessMimeTypesListFromPath(mimeRulesSource, contentName));
      if (strictContentTypeValidation && isUnknown(nameAssumedMimeTypes)) {
        throw new InvalidContentException("Content type could not be guessed");
      }

      Set<String> intersection = Sets.intersection(
          Sets.newHashSet(contentDetectedMimeTypes),
          Sets.newHashSet(nameAssumedMimeTypes)
      );
      if (strictContentTypeValidation && intersection.isEmpty()) {
        throw new InvalidContentException(
            String.format("Detected content type %s, but by name %s expected %s",
                contentDetectedMimeTypes, contentName, nameAssumedMimeTypes)
        );
      }
    }

    if (declaredContentType != null) {
      Set<String> union = Sets.union(
          Sets.newHashSet(contentDetectedMimeTypes),
          Sets.newHashSet(nameAssumedMimeTypes)
      );
      if (!union.contains(declaredContentType)) {
        log.debug("Declared content type {}, discovered types {}", declaredContentType, union);
        if (strictContentTypeValidation) {
          throw new InvalidContentException(
              String.format("Declared content type %s not among discovered types %s", declaredContentType, union));
        }
      }
    }

    String finalContentType;
    if (mimeRulesSource != null && contentName != null) {
      // format implied contentType
      finalContentType = nameAssumedMimeTypes.get(0);
    }
    else if (declaredContentType != null) {
      finalContentType = declaredContentType;
    }
    else if (!nameAssumedMimeTypes.isEmpty()) {
      finalContentType = nameAssumedMimeTypes.get(0);
    }
    else {
      finalContentType = contentDetectedMimeTypes.get(0);
    }

    log.debug("Content {} declared as {}, determined as {}", contentName, declaredContentType, finalContentType);

    return finalContentType;
  }

  /**
   * Returns {@code true} if list of mimeTypes is actually saying "unkown" content.
   */
  private boolean isUnknown(final List<String> mimeTypes) {
    return mimeTypes.isEmpty() || (mimeTypes.size() == 1 && mimeTypes.contains(ContentTypes.OCTET_STREAM));
  }
}
