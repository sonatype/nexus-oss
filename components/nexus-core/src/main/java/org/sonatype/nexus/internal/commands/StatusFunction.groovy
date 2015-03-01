/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.internal.commands

import org.apache.felix.service.command.CommandSession
import org.apache.felix.service.command.Function
import org.apache.karaf.shell.commands.Command
import org.sonatype.nexus.SystemStatus

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Dump system status.
 *
 * @since 3.0
 */
@Named
@Singleton
@Command(scope='nexus', name='status', description = 'Nexus system status')
class StatusFunction
  implements Function
{
  @Inject
  Provider<SystemStatus> systemStatusProvider

  @Override
  def execute(CommandSession commandSession, List<Object> list) {
    def console = commandSession.console
    SystemStatus status = systemStatusProvider.get()
    console.println "Version: $status.version"
    console.println "Edition: $status.editionShort"
    console.println "State: $status.state"
    console.println "License installed: $status.licenseInstalled"
    console.println "License expired: $status.licenseExpired"
    console.println "License trial: $status.trialLicense"
    return null;
  }
}
