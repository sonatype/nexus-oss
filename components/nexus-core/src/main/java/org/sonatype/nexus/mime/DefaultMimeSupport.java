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

package org.sonatype.nexus.mime;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypes;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link MimeSupport} component using MimeUtil2 library and the
 * {@link NexusMimeTypes}.
 *
 * @since 2.0
 */
@Named
@Singleton
public class DefaultMimeSupport
    extends ComponentSupport
    implements MimeSupport
{
  /**
   * Aoache Tika instance.
   */
  private final Tika tika;

  private final NexusMimeTypes nexusMimeTypes;

  /**
   * A loading cache of extension to MIME type.
   */
  private final LoadingCache<String, List<String>> extensionToMimeTypeCache;

  @Inject
  public DefaultMimeSupport() {
    this(new NexusMimeTypes());
  }

  @VisibleForTesting
  public DefaultMimeSupport(final NexusMimeTypes nexusMimeTypes) {
    this.nexusMimeTypes = checkNotNull(nexusMimeTypes);
    this.tika = new Tika();

    // create the cache
    extensionToMimeTypeCache =
        CacheBuilder.newBuilder().maximumSize(500).build(new CacheLoader<String, List<String>>()
        {
          @Override
          public List<String> load(final String key)
              throws Exception
          {
            final List<String> detected = Lists.newArrayList();
            final NexusMimeTypes.NexusMimeType mimeType = nexusMimeTypes.getMimeTypes(key);
            if (mimeType != null) {
              // add Nexus matches first
              detected.addAll(mimeType.getMimetypes());
              if (!mimeType.isOverride()) {
                // unless no override, ask Tika too
                detected.add(tika.detect("dummy." + key));
              }
              return detected;
            }
            // no nexus matches, just ask Tika
            return Collections.singletonList(tika.detect("dummy." + key));
          }
        });
  }

  @Override
  public String guessMimeTypeFromPath(final MimeRulesSource mimeRulesSource, final String path) {
    if (mimeRulesSource != null) {
      final String hardRule = mimeRulesSource.getRuleForPath(path);
      if (!Strings.isNullOrEmpty(hardRule)) {
        return hardRule;
      }
    }
    return guessMimeTypeFromPath(path);
  }

  @Override
  public String guessMimeTypeFromPath(final String path) {
    final List<String> mimeTypes = guessMimeTypesListFromPath(path);
    if (mimeTypes.isEmpty()) {
      // what here?
      return MimeTypes.OCTET_STREAM;
    }
    else {
      return mimeTypes.get(0);
    }
  }

  public List<String> guessMimeTypesListFromPath(final String path) {
    final String pathExtension = getExtension(path);
    try {
      return extensionToMimeTypeCache.get(pathExtension);
    }
    catch (ExecutionException e) {
      throw Throwables.propagate(e);
    }
  }

  @Deprecated
  @Override
  public Set<String> guessMimeTypesFromPath(final String path) {
    return Sets.newHashSet(guessMimeTypesListFromPath(path));
  }

  @Override
  public Set<String> detectMimeTypesFromContent(final ContentLocator content)
      throws IOException
  {
    try (final BufferedInputStream bis = new BufferedInputStream(content.getContent())) {
      final Metadata metadata = new Metadata();
      return Collections.singleton(tika.detect(bis, metadata));
    }
  }

  // ==

  /**
   * Copied and sanitized from MimeUtil2 to retain same behaviour for extension discovery, needed not only here,
   * but also in {@link NexusMimeTypes} class.
   */
  public static String getExtension(final String fileName) {
    if (fileName == null || fileName.length() == 0) {
      return "";
    }
    int index = fileName.indexOf(".");
    return index < 0 ? "" : fileName.substring(index + 1);
  }
}
