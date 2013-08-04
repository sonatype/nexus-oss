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

package org.sonatype.nexus.plugins.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @deprecated Use {@link org.sonatype.nexus.plugins.ui.contribution.UiContributor}
 */
public class AbstractNexusIndexHtmlCustomizer
    implements NexusIndexHtmlCustomizer
{
  @Override
  public String getPreHeadContribution(Map<String, Object> context) {
    return null;
  }

  @Override
  public String getPostHeadContribution(Map<String, Object> context) {
    return null;
  }

  @Override
  public String getPreBodyContribution(Map<String, Object> context) {
    return null;
  }

  @Override
  public String getPostBodyContribution(Map<String, Object> context) {
    return null;
  }

  // ==

  protected String getVersionFromJarFile(String path) {
    Properties props = new Properties();

    InputStream is = getClass().getResourceAsStream(path);

    if (is != null) {
      try {
        props.load(is);
      }
      catch (IOException e) {
        // no prop file ?? back out
        return null;
      }
    }

    return props.getProperty("version");
  }
}
