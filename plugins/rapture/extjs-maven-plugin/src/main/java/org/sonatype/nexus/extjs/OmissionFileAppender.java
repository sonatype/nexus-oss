/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.extjs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import org.apache.maven.plugin.logging.Log;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Omission token aware file appender.
 *
 * @since 3.0
 */
public class OmissionFileAppender
{
  public static final String IF_START_PREFIX = "//<if ";

  public static final String IF_START_SUFFIX = ">";

  public static final String IF_END = "//</if>";

  private final Log log;

  private final Writer output;

  private boolean enabled = true;

  private final Map<String,Boolean> flags = Maps.newHashMap();

  public OmissionFileAppender(final Log log, final Writer output) {
    this.log = checkNotNull(log);
    this.output = checkNotNull(output);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public Map<String, Boolean> getFlags() {
    return flags;
  }

  public void parseFlags(final Map<String,String> rawFlags) {
    for (Entry<String,String> entry : rawFlags.entrySet()) {
      boolean flag = Boolean.parseBoolean(entry.getValue());
      flags.put(entry.getKey(), flag);
    }
    log.debug("Omit flags: " + flags);
  }

  public void append(final File source) throws IOException {
    log.debug("Appending: " + source);

    BufferedReader input = new BufferedReader(new FileReader(source));
    try {
      String line;
      long lineno = 0;
      boolean omit = false;

      while ((line = input.readLine()) != null) {
        lineno++;

        // check for omission start and stop tokens
        if (enabled) {
          String trimmedLine = line.trim();
          if (isOmitStart(trimmedLine)) {
            if (omit) {
              // complain if already in omit mode and we found a start token (nesting is not allowed)
              log.warn("Unexpected omit start token: " + source + "#" + lineno);
            }
            omit = true;
          }
          else if (isOmitStop(trimmedLine)) {
            // unable to detect dangling stop token
            omit = false;
          }
          else if (omit) {
            // if omission enabled, then comment the line (include for reference, compressor will optimize out)
            line = "//" + line;
          }
        }

        output.write(line);
        output.write('\n');
      }

      // complain if still in omit mode at the end of the file
      if (omit) {
        log.warn("Missing omit end token: " + source);
      }
    }
    finally {
      input.close();
    }

    output.write('\n');
  }

  private boolean isOmitStart(final String line) {
    // assumes line is trimmed
    if (line.startsWith(IF_START_PREFIX) && line.endsWith(IF_START_SUFFIX)) {
      String flagName = line.substring(IF_START_PREFIX.length(), line.length() - IF_START_SUFFIX.length());
      Boolean flag = flags.get(flagName);
      return flag != null && flag;
    }
    return false;
  }

  private boolean isOmitStop(final String line) {
    // assumes line is trimmed
    return line.equals(IF_END);
  }
}
