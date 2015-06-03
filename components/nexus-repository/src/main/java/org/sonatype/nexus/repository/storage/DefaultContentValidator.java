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
import java.util.LinkedHashSet;
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

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import com.google.common.net.MediaType;

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
    String declaredBaseContentType = mediaTypeWithoutParameters(declaredContentType);

    final LinkedHashSet<String> contentDetectedMimeTypes = new LinkedHashSet<>();
    try (InputStream is = contentSupplier.get()) {
      contentDetectedMimeTypes.addAll(mimeSupport.detectMimeTypes(is, contentName));
    }
    adjustIfHtml(contentDetectedMimeTypes);
    log.debug("Mime support detects {} as {}", contentName, contentDetectedMimeTypes);

    if (strictContentTypeValidation && isUnknown(contentDetectedMimeTypes)) {
      throw new InvalidContentException("Content type could not be determined: " + contentName);
    }

    final LinkedHashSet<String> nameAssumedMimeTypes = new LinkedHashSet<>();
    if (contentName != null) {
      nameAssumedMimeTypes.addAll(
          mimeSupport.guessMimeTypesListFromPath(
              contentName,
              mimeRulesSource != null ? mimeRulesSource : MimeRulesSource.NOOP)
      );
      adjustIfHtml(nameAssumedMimeTypes);
      log.debug("Mime support assumes {} as {}", contentName, nameAssumedMimeTypes);
      if (strictContentTypeValidation && isUnknown(nameAssumedMimeTypes)) {
        throw new InvalidContentException("Content type could not be guessed: " + contentName);
      }

      Set<String> intersection = Sets.intersection(contentDetectedMimeTypes, nameAssumedMimeTypes);
      log.debug("content/name types intersection {}", intersection);
      if (strictContentTypeValidation && intersection.isEmpty()) {
        throw new InvalidContentException(
            String.format("Detected content type %s, but expected %s: %s",
                contentDetectedMimeTypes, nameAssumedMimeTypes, contentName)
        );
      }
    }

    if (declaredBaseContentType != null) {
      Set<String> union = Sets.union(contentDetectedMimeTypes, nameAssumedMimeTypes);
      log.debug("content/name types union {}", union);
      if (!union.contains(declaredBaseContentType)) {
        log.debug("Declared content type {}, discovered types {}", declaredContentType, union);
        if (strictContentTypeValidation) {
          throw new InvalidContentException(
              String.format("Declared content type %s not among discovered types %s: %s",
                  declaredContentType, union, contentName));
        }
      }
    }

    String finalContentType;
    if (mimeRulesSource != null && contentName != null) {
      // format implied contentType
      finalContentType = nameAssumedMimeTypes.iterator().next();
    }
    else if (declaredContentType != null) {
      finalContentType = declaredContentType;
    }
    else if (!nameAssumedMimeTypes.isEmpty()) {
      finalContentType = nameAssumedMimeTypes.iterator().next();
    }
    else {
      finalContentType = contentDetectedMimeTypes.iterator().next();
    }

    log.debug("Content {} declared as {}, determined as {}", contentName, declaredContentType, finalContentType);

    return finalContentType;
  }

  /**
   * Removes any parameter (like charset) for simpler matching.
   */
  @Nullable
  private String mediaTypeWithoutParameters(final String declaredMediaType) {
    if (Strings.isNullOrEmpty(declaredMediaType)) {
      return null;
    }
    try {
      MediaType mediaType = MediaType.parse(declaredMediaType);
      return mediaType.withoutParameters().toString();
    }
    catch (IllegalArgumentException e) {
      throw new InvalidContentException("Invalid declared contentType: " + declaredMediaType, e);
    }
  }

  /**
   * Returns {@code true} if list of mimeTypes is actually saying "unknown" content.
   */
  private boolean isUnknown(final Set<String> mimeTypes) {
    return mimeTypes.isEmpty() || (mimeTypes.size() == 1 && mimeTypes.contains(ContentTypes.APPLICATION_OCTET_STREAM));
  }

  /**
   * Circumvention if mime type covers HTML: Reason for exceptional handling HTML is due to XHTML (augmented with +xml,
   * basically subtype of XML) and "plain" HTML. They are not related in any way (supertype or alias) in registry but
   * still both represent "html" (as general term) in what we are interested to perform validation.
   */
  private void adjustIfHtml(final Set<String> mimeTypes) {
    if (mimeTypes.contains(MediaType.HTML_UTF_8.withoutParameters().toString()) ||
        mimeTypes.contains(MediaType.XHTML_UTF_8.withoutParameters().toString())) {
      // to circumvent on xhtml vs html, we will treat both as text/html which is okay for content validation purposes
      mimeTypes.add(ContentTypes.TEXT_HTML);
    }
  }
}
