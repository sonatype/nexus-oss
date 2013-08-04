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

package org.sonatype.nexus.plugins.siesta;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.log.LogConfigurationParticipant;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkState;

/**
 * Contributes "logback-siesta.xml" logging configuration.
 *
 * @since 2.4
 */
@Named
@Singleton
public class LogConfigurationParticipantImpl
    implements LogConfigurationParticipant
{
  private static final Logger log = LoggerFactory.getLogger(LogConfigurationParticipantImpl.class);

  @Override
  public String getName() {
    return "logback-siesta.xml";
  }

  @Override
  public InputStream getConfiguration() {
    URL resource = getClass().getResource(getName());
    log.debug("Using resource: {}", resource);
    checkState(resource != null);
    try {
      assert resource != null; // Keep IDEA happy
      return resource.openStream();
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }
}
