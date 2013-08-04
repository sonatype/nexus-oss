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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * EntrySource that sources itself from a {@code java.util.Properties} file. It might be set to fail but also to keep
 * silent the fact that file to load is not found.
 *
 * @author cstamas
 */
public class PropertiesFileEntrySource
    extends AbstractMapEntrySource
{
  private final File propertiesFile;

  private final boolean failIfNotFound;

  private Map<String, Object> source;

  public PropertiesFileEntrySource(final File propertiesFile) {
    this(propertiesFile, true);
  }

  public PropertiesFileEntrySource(final File propertiesFile, final boolean failIfNotFound) {
    super(Preconditions.checkNotNull(propertiesFile).getAbsolutePath(), "propsFile");

    this.propertiesFile = Preconditions.checkNotNull(propertiesFile).getAbsoluteFile();

    this.failIfNotFound = failIfNotFound;
  }

  public synchronized Map<String, Object> getSource()
      throws AppContextException
  {
    if (source == null) {
      try {
        FileInputStream fis;

        if (propertiesFile.isFile()) {
          Properties properties = new Properties();

          fis = new FileInputStream(propertiesFile);

          try {
            if (propertiesFile.getName().endsWith(".xml")) {
              // assume it's new XML properties file
              properties.loadFromXML(fis);
            }
            else {
              // assume it's "plain old" properties file
              properties.load(fis);
            }
          }
          finally {
            fis.close();
          }

          final Map<String, Object> result = new HashMap<String, Object>();

          for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String key = String.valueOf(entry.getKey());

            result.put(key, entry.getValue());
          }

          source = result;
        }
        else if (failIfNotFound) {
          throw new AppContextException("Cannot load up properties file from \""
              + propertiesFile.getAbsolutePath() + "\", it does not exists!");
        }
        else {
          source = Collections.emptyMap();
        }
      }
      catch (IOException e) {
        throw new AppContextException("Cannot load up properties file from \""
            + propertiesFile.getAbsolutePath() + "\"!", e);
      }
    }

    return source;
  }
}
