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
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.mime.detectors.NexusExtensionMimeDetector;
import org.sonatype.nexus.mime.detectors.NexusMagicMimeMimeDetector;
import org.sonatype.nexus.mime.detectors.NexusOpendesktopMimeDetector;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.util.SystemPropertiesHelper;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;
import eu.medsea.mimeutil.detector.MimeDetector;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Default implementation of {@link MimeSupport} component using MimeUtil2 library and the
 * {@link NexusExtensionMimeDetector}.
 *
 * @since 2.0
 */
@Component(role = MimeSupport.class)
public class DefaultMimeSupport
    extends AbstractLoggingComponent
    implements MimeSupport
{
  public static final String MIME_MAGIC_OPENDESKTOP_KEY = "org.sonatype.nexus.mime.DefaultMimeSupport.mimeMagicOpendesktop";

  public static final String MIME_MAGIC_FILE_KEY = "org.sonatype.nexus.mime.DefaultMimeSupport.mimeMagicFile";

  /**
   * Property to make Nexus consume the OpenDesktop formatted magic.mime database, used by some newer Linux based
   * OSes
   * (latest CentOS, RHEL, Ubuntu). This new switch by default read the file from {@code /usr/share/mime/mime.cache}
   * unless overridden.
   *
   * @see <a href="https://issues.sonatype.org/browse/NEXUS-5772">NEXUS-5772</a>
   * @see <a href="http://standards.freedesktop.org/shared-mime-info-spec/shared-mime-info-spec-latest.html">MIME
   *      info
   *      specification (latest)</a>
   * @since 2.7.0
   */
  public final boolean MIME_MAGIC_OPENDESKTOP = SystemPropertiesHelper.getBoolean(MIME_MAGIC_OPENDESKTOP_KEY, false);

  /**
   * Property to override the location of the mime.magic file to read up (the OS one). The default location depends
   * on
   * which "standard" is used (classic vs OpenDesktop), but also some OSes might have some customizations in this
   * area. If this property is specified, the specified file path will be used, but if not, the used mime detector
   * default will be used instead. For defaults, see {@link NexusMagicMimeMimeDetector} (the "classic") and
   * {@link NexusOpendesktopMimeDetector}.
   *
   * @since 2.7.0
   */
  public final String MIME_MAGIC_FILE = SystemPropertiesHelper.getString(MIME_MAGIC_FILE_KEY, null);

  private final MimeUtil2 nonTouchingMimeUtil;

  private final MimeUtil2 touchingMimeUtil;

  /**
   * A "cache" to be used with {@link #nonTouchingMimeUtil}. As that instance of MimeUtil2 uses only one mime
   * detector
   * registered by us, the {@link NexusExtensionMimeDetector}. Hence, even if the
   * {@link #guessMimeTypeFromPath(String)} and other methods talk about paths, we know they actually deal with file
   * extensions only (deduces the MIME type from file extension). This map simply caches the responses from
   * MimeUtil2,
   * as it's operation is a bit heavy weight (congestion happens on synchronized {@link Properties} instance deeply
   * buried in MimeUtil2 classes), and also, modifications to extension MIME type mapping is not possible without
   * restarting JVM where MimeUtil2. The cache is keyed with extensions, values are MIME types (represented as
   * strings).
   */
  private final LoadingCache<String, String> extensionToMimeTypeCache;

  /**
   * See {@link NexusMimeTypes} for customizations
   */
  public DefaultMimeSupport() {
    // MimeUtil2 by design will start (try to) read the file/stream if some "eager" detector is registered
    // so we follow the "private instance" pattern, and we handle two instances for now

    // uses Extension only for now (speed, no IO, but less accuracy)
    nonTouchingMimeUtil = new MimeUtil2();
    nonTouchingMimeUtil.registerMimeDetector(NexusExtensionMimeDetector.class.getName());

    // uses magic-mime (IO and lower speed but more accuracy)
    // See src/main/resources/magic.mime for customizations

    final String mimeMagicFilePath = getFilePathToUseIfExistsOrDie();
    touchingMimeUtil = new MimeUtil2();

    if (MIME_MAGIC_OPENDESKTOP) {
      // MIME_MAGIC_FILE override is handled by this class
      if (!Strings.isNullOrEmpty(mimeMagicFilePath)) {
        NexusOpendesktopMimeDetector.mimeCachePath = mimeMagicFilePath;
      }
      else {
        NexusOpendesktopMimeDetector.mimeCachePath = NexusOpendesktopMimeDetector.DEFAULT_MIME_CACHE_PATH;
      }
      final MimeDetector md =
          touchingMimeUtil.registerMimeDetector(NexusOpendesktopMimeDetector.class.getName());
      if (md == null) {
        throw new IllegalArgumentException("Failed to register NexusOpendesktopMimeDetector with MIME database "
            + NexusOpendesktopMimeDetector.mimeCachePath);
      }
    }
    else {
      // we need to handle MIME_MAGIC_FILE override here manually
      if (!Strings.isNullOrEmpty(mimeMagicFilePath)) {
        System.setProperty("magic-mime", mimeMagicFilePath);
      }
      final MimeDetector md = touchingMimeUtil.registerMimeDetector(NexusMagicMimeMimeDetector.class.getName());
      if (md == null) {
        throw new IllegalArgumentException("Failed to register NexusMagicMimeMimeDetector");
      }
    }

    // create the cache
    extensionToMimeTypeCache =
        CacheBuilder.newBuilder().maximumSize(500).build(new CacheLoader<String, String>()
        {
          @Override
          public String load(final String key)
              throws Exception
          {
            // FIXME (by replacing MimeUtil with something else?)
            // MimeUtil2#getMostSpecificMimeType is broken in 2.1.2/2.1.3, it will (in contrast to it's javadoc)
            // *usually*
            // return the last
            // mime type regardless of specificity. Which one is last depends on the impl of
            // HashSet<String>.iterator()
            // (which seems to have a fairly stable ordering on JVM: different order breaks unit tests.)
            //
            // TODO: Hack alert: as with introduction of cache, loading cache will be invoked with extension got from
            // the path only. As code reading showed, MimeUtil2 will do similarly, as only extension MimeDetector is registered
            // Still, we make a "fake" filename, just to not bork any existing logic or expectancies in MimeUtil2. Still
            // it is the extension that matters of this "dummy" file.
            return MimeUtil2.getMostSpecificMimeType(getNonTouchingMimeUtil2().getMimeTypes("dummyfile." + key))
                .toString();
          }
        });
  }

  /**
   * Method that returns user specified mime file path and also verifies it's existence. If user did not specify mime
   * magic file path, will return {@code null}, meaning that given detector should use it's own default paths, if
   * applicable.
   *
   * @return file specific for mime database specified by user and checked for existence, or {@code null} if user did
   *         not specify it explicitly.
   * @throws IllegalArgumentException if user did specify a MIME file path but that path does not exists or is not a
   *                                  file or is not readable.
   */
  private String getFilePathToUseIfExistsOrDie() {
    // chooses override path or default path. Default path copied from OpendesktopMimeDetector source.
    final String path = MIME_MAGIC_FILE;
    if (Strings.isNullOrEmpty(path)) {
      return null;
    }
    if (!new File(path).isFile()) {
      throw new IllegalArgumentException("Explicitly set MIME magic file not found on path " + path);
    }
    return path;
  }

  protected MimeUtil2 getNonTouchingMimeUtil2() {
    return nonTouchingMimeUtil;
  }

  protected MimeUtil2 getTouchingMimeUtil2() {
    return touchingMimeUtil;
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
    // even if we got path as param, the "non touching" mimeutil2 uses extensions only
    // see constructor how it is configured
    // Note: using same method to get extension as MimeUtil2's MimeDetectors would
    final String pathExtension = MimeUtil2.getExtension(path);
    try {
      return extensionToMimeTypeCache.get(pathExtension);
    }
    catch (ExecutionException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public Set<String> guessMimeTypesFromPath(final String path) {
    return toStringSet(getNonTouchingMimeUtil2().getMimeTypes(path));
  }

  @Override
  public Set<String> detectMimeTypesFromContent(final ContentLocator content)
      throws IOException
  {
    Set<String> magicMimeTypes = new HashSet<String>();
    try (final BufferedInputStream bis = new BufferedInputStream(content.getContent())) {
      magicMimeTypes.addAll(toStringSet(getTouchingMimeUtil2().getMimeTypes(bis)));
    }
    return magicMimeTypes;
  }

  // ==

  @SuppressWarnings("unchecked")
  private Set<String> toStringSet(final Collection<?> mimeTypes) {
    Set<String> result = new HashSet<String>();
    for (MimeType mimeType : (Collection<MimeType>) mimeTypes) {
      result.add(mimeType.toString());
    }
    return result;
  }
}
