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
package org.sonatype.nexus.mime;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ordered regular-expression {@link MimeRulesSource} implementation.
 *
 * @since 2.0
 */
public class RegexpMimeRulesSource
    implements MimeRulesSource
{
  private final LinkedHashMap<Pattern, String> rules = Maps.newLinkedHashMap();

  public void addRule(final String pattern, final String mimeType) {
    addRule(Pattern.compile(pattern), mimeType);
  }

  public void addRule(final Pattern pattern, final String mimeType) {
    rules.put(checkNotNull(pattern), checkNotNull(mimeType));
  }

  @Override
  public String getRuleForPath(final String path) {
    for (Map.Entry<Pattern, String> entry : rules.entrySet()) {
      if (entry.getKey().matcher(path).matches()) {
        return entry.getValue();
      }
    }

    return null;
  }
}
