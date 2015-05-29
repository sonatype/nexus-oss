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

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.io.FileSupport;
import org.sonatype.nexus.mime.MimeRulesSource;
import org.sonatype.nexus.mime.NexusMimeType;
import org.sonatype.nexus.repository.maven.MavenPath;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.io.Files;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Maven 2 specific {@link MimeRulesSource} that specifies known and format specific file MIME types.
 *
 * @since 3.0
 */
@Named(Maven2Format.NAME)
@Singleton
public class Maven2MimeRulesSource
    extends ComponentSupport
    implements MimeRulesSource
{
  /**
   * Maven POMs.
   */
  private static final NexusMimeType POM_TYPE = new NexusMimeType(true, "application/xml");

  /**
   * SHA1 and MD5 hashes.
   */
  private static final NexusMimeType HASH_TYPE = new NexusMimeType(true, "text/plain");

  /**
   * Maven Repository Metadata.
   */
  private static final NexusMimeType METADATA_TYPE = new NexusMimeType(true, "application/xml");

  /**
   * Maven PGP signature.
   */
  private static final NexusMimeType SIGNATURE_TYPE = new NexusMimeType(true, "application/pgp-signature", "text/plain");

  private final Maven2MavenPathParser mavenPathParser;

  @Inject
  public Maven2MimeRulesSource(final Maven2MavenPathParser mavenPathParser) {
    this.mavenPathParser = checkNotNull(mavenPathParser);
  }

  @Nullable
  @Override
  public NexusMimeType getRuleForPath(final String path) {
    MavenPath mavenPath = mavenPathParser.parsePath(path);
    if (mavenPath.isPom()) {
      return POM_TYPE;
    }
    else if (mavenPath.isHash()) {
      return HASH_TYPE;
    }
    else if (mavenPath.isSignature()) {
      return SIGNATURE_TYPE;
    }
    else if (Constants.METADATA_FILENAME.equals(mavenPath.getFileName())) {
      return METADATA_TYPE;
    }
    // otherwise no format-specific rule, use common rules
    return null;
  }
}
