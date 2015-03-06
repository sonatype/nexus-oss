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
package org.sonatype.nexus.repository.maven.internal.maven2;

import java.util.regex.Pattern;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.M2GavCalculator;
import org.sonatype.nexus.repository.maven.internal.ArtifactCoordinates;
import org.sonatype.nexus.repository.maven.internal.ArtifactCoordinates.SignatureType;
import org.sonatype.nexus.repository.maven.internal.Coordinates;
import org.sonatype.nexus.repository.maven.internal.Coordinates.HashType;
import org.sonatype.nexus.repository.maven.internal.PathParser;

import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Maven 2 path parser.
 *
 * @since 3.0
 */
@Named(Maven2Format.NAME)
@Singleton
public class Maven2PathParser
    implements PathParser
{
  private static final String SNAPSHOT_VERSION = "SNAPSHOT";

  private static final Pattern VERSION_FILE_PATTERN =
      Pattern.compile(
          "^(.*)-([0-9]{8}.[0-9]{6})-([0-9]+)$|^([0-9]{8}.[0-9]{6})-([0-9]+)$|^(.*)([0-9]{8}.[0-9]{6})-([0-9]+)$");

  // TODO: TokenParser not capable to do this (yet)
  // TODO: this is legacy, but here only to make it work
  // TODO: consider moving/updating/fleshing out it from nx2 codebase, or improve TokenParser
  private final M2GavCalculator m2GavCalculator = new M2GavCalculator();

  @Override
  public Coordinates parsePath(final String path) {
    final String fileName = path.substring(path.lastIndexOf('/') + 1);
    HashType hashType = null;
    for (HashType ht : HashType.values()) {
      if (fileName.endsWith("." + ht.getExt())) {
        hashType = ht;
        break;
      }
    }
    final Gav gav = m2GavCalculator.pathToGav(path);
    if (gav == null) {
      return new Coordinates(path, fileName, hashType);
    }
    else {
      SignatureType signatureType = null;
      if (gav.getSignatureType() != null) {
        if (gav.getSignatureType() == Gav.SignatureType.gpg) {
          signatureType = SignatureType.GPG;
        }
        else {
          throw new RuntimeException("Unknown signature type: " + gav.getSignatureType());
        }
      }

      String classifier = gav.getClassifier();
      if (classifier != null) {
        classifier = classifier.trim();
        if (Strings.isNullOrEmpty(classifier)) {
          classifier = null;
        }
      }

      return new ArtifactCoordinates(
          path,
          fileName,
          hashType,
          gav.getGroupId(),
          gav.getArtifactId(),
          gav.getVersion(),
          gav.getBaseVersion(),
          classifier,
          gav.getExtension(),
          signatureType
      );
    }
  }

  @Override
  public boolean isSnapshotVersion(final String version) {
    checkNotNull(version);
    return VERSION_FILE_PATTERN.matcher(version).matches() || version.endsWith(SNAPSHOT_VERSION);
  }
}
