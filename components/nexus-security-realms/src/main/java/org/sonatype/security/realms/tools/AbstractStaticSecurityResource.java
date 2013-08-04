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

package org.sonatype.security.realms.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.sonatype.security.model.Configuration;
import org.sonatype.security.model.io.xpp3.SecurityConfigurationXpp3Reader;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract class that removes the boiler plate code of reading in the security configuration.
 *
 * @author Brian Demers
 */
public abstract class AbstractStaticSecurityResource
    implements StaticSecurityResource
{
  private final Logger logger = LoggerFactory.getLogger(getClass());

  protected boolean dirty = false;

  public boolean isDirty() {
    return dirty;
  }

  protected void setDirty(boolean dirty) {
    this.dirty = dirty;
  }

  protected abstract String getResourcePath();

  public Configuration getConfiguration() {
    String resourcePath = this.getResourcePath();

    if (StringUtils.isNotEmpty(resourcePath)) {
      Reader fr = null;
      InputStream is = null;

      this.logger.debug("Loading static security config from " + resourcePath);

      try {
        is = getClass().getResourceAsStream(resourcePath);
        SecurityConfigurationXpp3Reader reader = new SecurityConfigurationXpp3Reader();

        fr = new InputStreamReader(is);
        return reader.read(fr);
      }
      catch (IOException e) {
        this.logger.error("IOException while retrieving configuration file", e);
      }
      catch (XmlPullParserException e) {
        this.logger.error("Invalid XML Configuration", e);
      }
      finally {
        IOUtil.close(fr);
        IOUtil.close(is);
      }
    }

    // any other time just return null
    return null;
  }
}
