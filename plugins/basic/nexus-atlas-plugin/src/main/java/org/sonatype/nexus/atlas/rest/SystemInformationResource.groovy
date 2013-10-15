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

package org.sonatype.nexus.atlas.rest

import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.appcontext.AppContext
import org.sonatype.nexus.configuration.application.ApplicationConfiguration
import org.sonatype.nexus.plugins.NexusPluginManager
import org.sonatype.sisu.goodies.common.ComponentSupport
import org.sonatype.sisu.goodies.common.Iso8601Date
import org.sonatype.sisu.siesta.common.Resource

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import java.nio.file.FileSystems

import static com.google.common.base.Preconditions.checkNotNull

/**
 * Renders system information.
 *
 * @since 2.7
 */
@Named
@Singleton
@Path(SystemInformationResource.RESOURCE_URI)
@Produces(MediaType.APPLICATION_JSON)
class SystemInformationResource
extends ComponentSupport
implements Resource
{
  static final String RESOURCE_URI = '/atlas/system-information'

  private final ApplicationConfiguration applicationConfiguration

  private final AppContext appContext

  private final NexusPluginManager pluginManager

  @Inject
  SystemInformationResource(final ApplicationConfiguration applicationConfiguration,
                            final AppContext appContext,
                            final NexusPluginManager pluginManager)
  {
    this.applicationConfiguration = checkNotNull(applicationConfiguration)
    this.appContext = checkNotNull(appContext)
    this.pluginManager = checkNotNull(pluginManager)
  }

  @GET
  @RequiresPermissions('nexus:atlas')
  Map report() {
    log.info 'Generating system information report'

    // HACK: provide local references to prevent problems with Groovy BUG accessing private fields
    def applicationConfiguration = this.applicationConfiguration
    def appContext = this.appContext
    def pluginManager = this.pluginManager

    def fileref = { File file ->
      if (file) {
        return file.canonicalPath
      }
      return null
    }

    def reportTime = {
      def now = new Date()
      return [
          'timezone': TimeZone.default.ID,
          'current': now.time,
          'iso8601': Iso8601Date.format(now)
      ]
    }

    def reportRuntime = {
      def runtime = Runtime.runtime

      return [
          'availableProcessors': runtime.availableProcessors(),
          'freeMemory': runtime.freeMemory(),
          'totalMemory': runtime.totalMemory(),
          'maxMemory': runtime.maxMemory(),
          'threads': Thread.activeCount()
      ]
    }

    // TODO: report root directories and how they map to file-stores

    def reportFileStores = {
      def data = [:]
      def fs = FileSystems.default
      fs.fileStores.each { store ->
        data[store.name()] = [
            'type': store.type(),
            'totalSpace': store.getTotalSpace(),
            'usableSpace': store.getUsableSpace(),
            'unallocatedSpace': store.getUnallocatedSpace()
        ]
      }
      return data
    }

    def reportNetwork = {
      def data = [:]
      NetworkInterface.networkInterfaces.each { intf ->
        data[intf.name] = [
            'displayName': intf.displayName,
            'up': intf.up,
            'virtual': intf.virtual,
            'multicast': intf.supportsMulticast(),
            'loopback': intf.loopback,
            'ptp': intf.pointToPoint,
            'mtu': intf.MTU,
            'addresses': intf.inetAddresses.collect { addr ->
              addr.toString()
            }.join(',')
        ]
      }
      return data
    }

    def reportNexusConfiguration = {
      return [
          'installDirectory': fileref(applicationConfiguration.installDirectory),
          'workingDirectory': fileref(applicationConfiguration.workingDirectory),
          'temporaryDirectory': fileref(applicationConfiguration.temporaryDirectory)
      ]
    }

    def reportNexusPlugins = {
      def data = [:]
      pluginManager.pluginResponses.each { gav, response ->
        def item = data[gav.artifactId] = [
            'groupId': gav.groupId,
            'artifactId': gav.artifactId,
            'version': gav.version,
            'successful': response.successful
        ]
        if (response.throwable) {
          item.throwable = response.throwable.toString()
        }
      }
      return data
    }

    // TODO: Report license (if we can resolve the components)

    def sections = [
        'system-time': reportTime(),
        'system-properties': System.properties.sort(),
        'system-environment': System.getenv().sort(),
        'system-runtime': reportRuntime(),
        'system-network': reportNetwork(),
        'system-filestores': reportFileStores(),
        'nexus-properties': appContext.flatten().sort(),
        'nexus-configuration': reportNexusConfiguration(),
        'nexus-plugins': reportNexusPlugins()
    ]

    return sections
  }
}