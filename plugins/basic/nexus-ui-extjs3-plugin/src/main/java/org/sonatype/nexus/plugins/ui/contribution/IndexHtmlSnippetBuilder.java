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

package org.sonatype.nexus.plugins.ui.contribution;

import java.util.List;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper to build HTML snippets for use in {@link org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer}
 * implementations.
 *
 * @since 2.5
 */
public class IndexHtmlSnippetBuilder
    extends AbstractUiContributionBuilder<String>
{

  private final List<String> styleRefs = Lists.newArrayList();

  private final List<String> scriptRefs = Lists.newArrayList();

  public IndexHtmlSnippetBuilder(final Object owner, final String groupId, final String artifactId) {
    super(owner, groupId, artifactId);
  }

  public IndexHtmlSnippetBuilder styleRef(final String fileName) {
    checkNotNull(fileName);
    styleRefs.add(fileName);
    return this;
  }

  public IndexHtmlSnippetBuilder defaultStyleRef() {
    return styleRef(getDefaultPath("css"));
  }

  public IndexHtmlSnippetBuilder scriptRef(final String fileName) {
    checkNotNull(fileName);
    scriptRefs.add(fileName);
    return this;
  }

  public IndexHtmlSnippetBuilder defaultScriptRef() {
    return scriptRef(getDefaultPath("js"));
  }

  public IndexHtmlSnippetBuilder encoding(final String encoding) {
    this.encoding = checkNotNull(encoding);
    return this;
  }

  public String build() {
    StringBuilder buff = new StringBuilder();

    for (String style : styleRefs) {
      buff.append(String
          .format("<link rel='stylesheet' href='%s%s' type='text/css' media='screen' charset='%s'/>", style,
              getCacheBuster(style), encoding));
      buff.append("\n");
    }

    for (String script : scriptRefs) {
      buff.append(String
          .format("<script src='%s%s' type='text/javascript' charset='%s'></script>", script, getCacheBuster(script),
              encoding));
      buff.append("\n");
    }

    return buff.toString();
  }
}
