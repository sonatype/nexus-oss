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

package org.sonatype.appcontext.source;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * This is a "legacy" baseDir EntrySource that should not be used anymore. It was used mostly in Plexus applications,
 * that usually do depend on "baseDir".
 *
 * @author cstamas
 * @deprecated Do not rely on system properties for stuff like these, use AppContext better.
 */
public class LegacyBasedirEntrySource
    implements EntrySource, EntrySourceMarker
{
  private final String basedirKey;

  private final boolean failIfNotFound;

  /**
   * Constructs the instance using "standard" key used in Plexus Applications. The constructed instance will fail if
   * key is not found!
   */
  public LegacyBasedirEntrySource() {
    this("basedir", true);
  }

  /**
   * Constructs an instance with custom key.
   */
  public LegacyBasedirEntrySource(final String basedirKey, final boolean failIfNotFound) {
    this.basedirKey = Preconditions.checkNotNull(basedirKey);
    this.failIfNotFound = failIfNotFound;
  }

  public String getDescription() {
    return "legacyBasedir(key:\"" + basedirKey + "\")";
  }

  public EntrySourceMarker getEntrySourceMarker() {
    return this;
  }

  public Map<String, Object> getEntries(AppContextRequest request)
      throws AppContextException
  {
    try {
      final File baseDir = discoverBasedir(basedirKey);
      if (failIfNotFound && !baseDir.isDirectory()) {
        throw new AppContextException(
            "LegacyBasedirEntrySource was not able to find existing basedir! It discovered \""
                + baseDir.getAbsolutePath() + "\", but it does not exists or is not a directory!");
      }
      final HashMap<String, Object> result = new HashMap<String, Object>();
      result.put(basedirKey, baseDir);
      return result;
    }
    catch (IOException e) {
      throw new AppContextException("Could not discover base dir!", e);
    }
  }

  // ==

  /**
   * The essence how old Plexus application was expecting to have "basedir" discovered. Usually using system property
   * that contained a file path, or fall back to current working directory.
   */
  public File discoverBasedir(final String basedirKey)
      throws IOException
  {
    final String basedirPath = System.getProperty(basedirKey);

    if (basedirPath == null) {
      return new File("").getCanonicalFile();
    }
    else {
      return new File(basedirPath).getCanonicalFile();
    }
  }
}
