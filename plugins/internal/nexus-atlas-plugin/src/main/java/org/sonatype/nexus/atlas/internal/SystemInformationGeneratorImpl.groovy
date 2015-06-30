/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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

import java.nio.file.FileSystems

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

import org.sonatype.nexus.atlas.SystemInformationGenerator
import org.sonatype.nexus.common.app.ApplicationDirectories
import org.sonatype.nexus.common.app.GlobalComponentLookupHelper
import org.sonatype.nexus.common.app.SystemStatus
import org.sonatype.nexus.common.node.LocalNodeAccess
import org.sonatype.nexus.common.text.Strings2
import org.sonatype.sisu.goodies.common.ComponentSupport
import org.sonatype.sisu.goodies.common.Iso8601Date

import org.apache.karaf.bundle.core.BundleService
import org.eclipse.sisu.Parameters
import org.osgi.framework.BundleContext

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
  private final GlobalComponentLookupHelper componentLookupHelper

  private final ApplicationDirectories applicationDirectories

  private final Provider<SystemStatus> systemStatusProvider

  private final Map<String, String> parameters

  private final BundleContext bundleContext

  private final BundleService bundleService

  private final LocalNodeAccess localNodeAccess

  @Inject
  SystemInformationGeneratorImpl(final GlobalComponentLookupHelper componentLookupHelper,
                                 final ApplicationDirectories applicationDirectories,
                                 final Provider<SystemStatus> systemStatusProvider,
                                 final @Parameters Map<String, String> parameters,
                                 final BundleContext bundleContext,
                                 final BundleService bundleService,
                                 final LocalNodeAccess localNodeAccess)
  {
    this.componentLookupHelper = checkNotNull(componentLookupHelper)
    this.applicationDirectories = checkNotNull(applicationDirectories)
    this.systemStatusProvider = checkNotNull(systemStatusProvider)
    this.parameters = checkNotNull(parameters)
    this.bundleContext = checkNotNull(bundleContext)
    this.bundleService = checkNotNull(bundleService)
    this.localNodeAccess = checkNotNull(localNodeAccess)
  }

  @Override
  Map report() {
    log.info 'Generating system information report'

    // HACK: provide local references to prevent problems with Groovy BUG accessing private fields
    def componentLookupHelper = this.componentLookupHelper
    def applicationDirectories = this.applicationDirectories
    def systemStatus = this.systemStatusProvider.get()
    def parameters = this.parameters
    def bundleContext = this.bundleContext
    def bundleService = this.bundleService
    def localNodeAccess = this.localNodeAccess

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
          'edition': systemStatus.editionShort,
          'state': systemStatus.state
      ]

      return data
    }

    def reportNexusNode = {
      def data = [
          'node-id': localNodeAccess.id
      ]

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

        // Add license details if we can resolve the license manager component
        def plm = componentLookupHelper.lookup('org.sonatype.licensing.product.ProductLicenseManager')
        if (plm) {
          def license = plm.licenseDetails
          data += [
              'evaluation': license.evaluation,
              'licensedUsers': license.licensedUsers,
              'rawFeatures': license.rawFeatures.join(','),
              'featureSet': license.featureSet.collect { it.id }.join(','),
              'effectiveDate': Iso8601Date.format(license.effectiveDate),
              'expirationDate': Iso8601Date.format(license.expirationDate),
              'contactName': license.contactName,
              'contactEmail': license.contactEmailAddress,
              'contactCompany': license.contactCompany,
              'contactCountry': license.contactCountry
          ]
        }

        // Add license fingerprint details if we can resolve the license fingerprinter
        def fp = componentLookupHelper.lookup('org.sonatype.licensing.product.util.LicenseFingerprinter')
        if (fp) {
          data += [
              'fingerprint': fp.calculate()
          ]
        }
      }

      return data
    }

    def reportNexusConfiguration = {
      return [
          'installDirectory': fileref(applicationDirectories.installDirectory),
          'workingDirectory': fileref(applicationDirectories.workDirectory),
          'temporaryDirectory': fileref(applicationDirectories.temporaryDirectory)
      ]
    }

    def reportNexusBundles = {
      def data = [:]
      bundleContext.bundles.each { bundle ->
        def info = bundleService.getInfo(bundle)
        data[info.bundleId] = [
            'bundleId': info.bundleId,
            'name': info.name,
            'symbolicName': info.symbolicName,
            'location': info.updateLocation,
            'version': info.version,
            'state': info.state.name(),
            'startLevel': info.startLevel,
            'fragment': info.fragment
        ]
      }
      return data
    }

    // masks the value of any properties that look like passwords
    def reportObfuscatedProperties = { properties ->
      return properties.collectEntries { key, value ->
        if (key.toLowerCase(Locale.US).contains('password')) {
          value = Strings2.mask(value)
        }
        return [key, value]
      }.sort()
    }

    def sections = [
        'system-time': reportTime(),
        'system-properties': reportObfuscatedProperties(System.properties),
        'system-environment': System.getenv().sort(),
        'system-runtime': reportRuntime(),
        'system-network': reportNetwork(),
        'system-filestores': reportFileStores(),
        'nexus-status': reportNexusStatus(),
        'nexus-node': reportNexusNode(),
        'nexus-license': reportNexusLicense(),
        'nexus-properties': reportObfuscatedProperties(parameters),
        'nexus-configuration': reportNexusConfiguration(),
        'nexus-bundles': reportNexusBundles()
    ]

    return sections
  }
}
