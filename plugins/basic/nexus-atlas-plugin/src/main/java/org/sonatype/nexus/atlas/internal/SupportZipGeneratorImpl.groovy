/**
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

package org.sonatype.nexus.atlas.internal

import org.sonatype.nexus.atlas.SupportZipGenerator
import org.sonatype.nexus.atlas.SupportZipGenerator.Request
import org.sonatype.nexus.configuration.application.ApplicationConfiguration
import org.sonatype.sisu.goodies.common.ComponentSupport

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Default {@link SupportZipGenerator}.
 *
 * @since 2.7
 */
@Named
@Singleton
class SupportZipGeneratorImpl
extends ComponentSupport
implements SupportZipGenerator
{
  private final File supportDir

  @Inject
  SupportZipGeneratorImpl(final ApplicationConfiguration applicationConfiguration) {
    assert applicationConfiguration

    // resolve where support archives will be stored
    supportDir = applicationConfiguration.getWorkingDirectory('support')
    log.info 'Support directory: {}', supportDir
  }

  @Override
  File generate(final Request request) {
    assert request

    log.info 'Generating support zip: {}', request

    // TODO

    return new File('foo.zip')
  }
}