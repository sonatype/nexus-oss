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

import org.sonatype.appcontext.AppContext
import org.sonatype.guice.bean.locators.BeanLocator
import org.sonatype.nexus.ApplicationStatusSource
import org.sonatype.nexus.atlas.SystemInformationGenerator
import org.sonatype.nexus.configuration.application.ApplicationConfiguration
import org.sonatype.nexus.plugins.NexusPluginManager
import org.sonatype.sisu.goodies.common.ComponentSupport
import org.sonatype.sisu.goodies.common.Iso8601Date

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import java.nio.file.FileSystems

import static com.google.common.base.Preconditions.checkNotNull

/**
 * Default {@link SystemInformationGenerator}.
 *
 * @since 2.7
 */
@Named
@Singleton
class SystemInformationGeneratorImpl
extends ComponentSupport
implements SystemInformationGenerator
{
  private final BeanLocator beanLocator

  private final ApplicationConfiguration applicationConfiguration

  private final ApplicationStatusSource applicationStatusSource

  private final AppContext appContext

  private final NexusPluginManager pluginManager

  @Inject
  SystemInformationGeneratorImpl(final BeanLocator beanLocator,
                                 final ApplicationConfiguration applicationConfiguration,
                                 final ApplicationStatusSource applicationStatusSource,
                                 final AppContext appContext,
                                 final NexusPluginManager pluginManager)
  {
    this.beanLocator = checkNotNull(beanLocator)
    this.applicationConfiguration = checkNotNull(applicationConfiguration)
    this.applicationStatusSource = checkNotNull(applicationStatusSource)
    this.appContext = checkNotNull(appContext)
    this.pluginManager = checkNotNull(pluginManager)
  }

  @Override
  Map report() {
    log.info 'Generating system information report'

    // HACK: provide local references to prevent problems with Groovy BUG accessing private fields
    def applicationConfiguration = this.applicationConfiguration
    def systemStatus = this.applicationStatusSource.systemStatus
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

    def reportFileStores = {
      def data = [:]
      def fs = FileSystems.default
      fs.fileStores.each { store ->
        data[store.name()] = [
            'description': store.toString(), // seems to be the only place where mount-point is exposed
            'type': store.type(),
            'totalSpace': store.totalSpace,
            'usableSpace': store.usableSpace,
            'unallocatedSpace': store.unallocatedSpace,
            'readOnly': store.readOnly
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

    def reportNexusStatus = {
      def data = [
          'version': systemStatus.version,
          'apiVersion': systemStatus.apiVersion,
          'edition': systemStatus.editionShort,
          'state': systemStatus.state,
          'initializedAt': systemStatus.initializedAt,
          'startedAt': systemStatus.startedAt,
          'lastConfigChange': systemStatus.lastConfigChange,
          'firstStart': systemStatus.firstStart,
          'instanceUpgrade': systemStatus.instanceUpgraded,
          'configurationUpgraded': systemStatus.configurationUpgraded
      ]

      if (systemStatus.errorCause) {
        data['errorCause'] = systemStatus.errorCause.toString()
      }

      return data
    }

    def reportNexusLicense = {
      def data = [
          'licenseInstalled': systemStatus.licenseInstalled
      ]

      if (systemStatus.licenseInstalled) {
        data += [
            'licenseExpired': systemStatus.licenseExpired,
            'trialLicense': systemStatus.trialLicense
        ]
        // NOTE: We should be able to resolve required components if licenseInstalled is true
        // TODO: report license key (via lookup of ProductLicenseManager)
        // TODO: report active users (via lookup of NexusAccessManager)
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

    def sections = [
        'system-time': reportTime(),
        'system-properties': System.properties.sort(),
        'system-environment': System.getenv().sort(),
        'system-runtime': reportRuntime(),
        'system-network': reportNetwork(),
        'system-filestores': reportFileStores(),
        'nexus-status': reportNexusStatus(),
        'nexus-license': reportNexusLicense(),
        'nexus-properties': appContext.flatten().sort(),
        'nexus-configuration': reportNexusConfiguration(),
        'nexus-plugins': reportNexusPlugins()
    ]

    return sections
  }
}