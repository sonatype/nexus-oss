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

package org.sonatype.nexus.mime.detectors;

import java.util.Collection;
import java.util.List;

import org.sonatype.nexus.mime.NexusMimeTypes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import eu.medsea.mimeutil.MimeException;
import eu.medsea.mimeutil.MimeUtil2;
import eu.medsea.mimeutil.detector.ExtensionMimeDetector;

/**
 * File extension detector using {@link NexusMimeTypes}. Falls back to the default MimeUtil behavior if not mime type
 * is
 * set.
 *
 * @since 2.3
 */
public class NexusExtensionMimeDetector
    extends ExtensionMimeDetector
{

  private NexusMimeTypes mimeTypes;

  public NexusExtensionMimeDetector() {
    this(new NexusMimeTypes());
  }

  public NexusExtensionMimeDetector(final NexusMimeTypes mimeTypes) {
    this.mimeTypes = mimeTypes;
  }

  @VisibleForTesting
  public void setNexusMimeTypes(NexusMimeTypes mimeTypes) {
    this.mimeTypes = mimeTypes;
  }

  @Override
  public Collection getMimeTypesFileName(final String fileName)
      throws MimeException
  {

    final List<String> detected = Lists.newArrayList();

    final String extension = MimeUtil2.getExtension(fileName);
    final NexusMimeTypes.NexusMimeType mimeType = mimeTypes.getMimeTypes(extension);
    if (mimeType != null) {
      if (mimeType.isOverride()) {
        detected.addAll(mimeType.getMimetypes());
        return detected;
      }
      else {
        final Collection defaultTypes = super.getMimeTypesFileName(fileName);

        // HACK we have to list additional mimetypes first, because MimeUtil2#getMostSpecificMimeType
        // is broken and will usually choose the last mimetype in the list.
        detected.addAll(mimeType.getMimetypes());
        detected.addAll(defaultTypes);
        return detected;
      }
    }

    return super.getMimeTypesFileName(fileName);
  }
}
