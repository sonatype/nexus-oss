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

package org.sonatype.nexus.proxy.maven;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.mime.NexusMimeTypes;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.validator.AbstractMimeMagicFileTypeValidator;
import org.sonatype.nexus.proxy.repository.validator.XMLUtils;
import org.sonatype.nexus.util.SystemPropertiesHelper;

import com.google.common.annotations.VisibleForTesting;

/**
 * Maven specific FileTypeValidator that checks for "most common" Maven artifacts and metadatas, namely: JARs, ZIPs,
 * WARs, EARs, POMs and XMLs.
 *
 * @author cstamas
 */
@Named("maven")
@Singleton
public class MavenFileTypeValidator
    extends AbstractMimeMagicFileTypeValidator
{
  public static final String XML_DETECTION_LAX_KEY = MavenFileTypeValidator.class.getName() + ".relaxedXmlValidation";

  private static final boolean XML_DETECTION_LAX_DEFAULT = true;

  private static final boolean XML_DETECTION_LAX = SystemPropertiesHelper.getBoolean(XML_DETECTION_LAX_KEY,
      XML_DETECTION_LAX_DEFAULT);

  @Inject
  public MavenFileTypeValidator(final MimeSupport mimeSupport) {
    super(mimeSupport);
  }

  @VisibleForTesting
  public MavenFileTypeValidator(final NexusMimeTypes mimeTypes, final MimeSupport mimeSupport) {
    super(mimeTypes, mimeSupport);
  }

  @Override
  public FileTypeValidity isExpectedFileType(final StorageFileItem file) {
    // only check content from maven repositories
    if (file.getRepositoryItemUid().getRepository().adaptToFacet(MavenRepository.class) == null) {
      return FileTypeValidity.NEUTRAL;
    }

    final String filePath = file.getPath().toLowerCase();
    if (filePath.endsWith(".pom")) {
      log.debug("Checking if Maven POM {} is of the correct MIME type.", file.getRepositoryItemUid());

      try {
        return XMLUtils.validateXmlLikeFile(file, "<project");
      }
      catch (IOException e) {
        log.warn("Cannot access content of StorageFileItem: " + file.getRepositoryItemUid(), e);

        return FileTypeValidity.NEUTRAL;
      }
    }
    else if (filePath.endsWith("/maven-metadata.xml")) {
      log.debug("Checking if Maven Repository Metadata {} is of the correct MIME type.",
          file.getRepositoryItemUid());

      try {
        return XMLUtils.validateXmlLikeFile(file, "<metadata");
      }
      catch (IOException e) {
        log.warn("Cannot access content of StorageFileItem: " + file.getRepositoryItemUid(), e);

        return FileTypeValidity.NEUTRAL;
      }
    }
    else if (filePath.endsWith(".sha1") || filePath.endsWith(".md5")) {
      log.debug("Checking if Maven checksum {} is valid.", file.getRepositoryItemUid());

      try {
        final String digest = MUtils.readDigestFromFileItem(file);
        if (MUtils.isDigest(digest)) {
          if (filePath.endsWith(".sha1") && digest.length() == 40) {
            return FileTypeValidity.VALID;
          }
          if (filePath.endsWith(".md5") && digest.length() == 32) {
            return FileTypeValidity.VALID;
          }
        }
        return FileTypeValidity.INVALID;
      }
      catch (IOException e) {
        log.warn("Cannot access content of StorageFileItem: " + file.getRepositoryItemUid(), e);

        return FileTypeValidity.NEUTRAL;
      }

    }
    else {
      return super.isExpectedFileType(file);
    }
  }

  @Override
  protected boolean isXmlLaxValidation(final StorageFileItem file) {
    // if no maven specific key is present for lax xml validator, use the generic one
    if (SystemPropertiesHelper.getString(XML_DETECTION_LAX_KEY, null) == null
        && !file.getItemContext().containsKey(XML_DETECTION_LAX_KEY)) {
      return super.isXmlLaxValidation(file);
    }
    // Note: this here is an ugly hack: enables per-request control of
    // LAX XML validation: if key not present, "system wide" settings used.
    // If key present, it's interpreted as Boolean and it's value is used to
    // drive LAX XML validation enable/disable.
    boolean xmlLaxValidation = XML_DETECTION_LAX;
    if (file.getItemContext().containsKey(XML_DETECTION_LAX_KEY)) {
      xmlLaxValidation = Boolean.parseBoolean(
          String.valueOf(file.getItemContext().get(XML_DETECTION_LAX_KEY))
      );
    }
    return xmlLaxValidation;
  }

}
