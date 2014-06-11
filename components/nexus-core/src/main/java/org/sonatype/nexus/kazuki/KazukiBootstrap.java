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
package org.sonatype.nexus.kazuki;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import io.kazuki.v0.PackageVersion;
import org.eclipse.sisu.EagerSingleton;
import org.h2.engine.Constants;

/**
 * Helper to log Kazuki version on boot.
 *
 * @since 3.0
 */
@Named
@EagerSingleton
public class KazukiBootstrap
  extends ComponentSupport
{
  @Inject
  public KazukiBootstrap() throws Exception {
    log.info("Kazuki version: {}", PackageVersion.VERSION);
    log.info("H2 version: {}", Constants.getVersion());
  }
}