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
package org.sonatype.nexus.atlas.internal

import com.google.inject.Key
import org.eclipse.sisu.Parameters
import org.eclipse.sisu.inject.BeanLocator
import org.sonatype.nexus.SystemStatus
import org.sonatype.nexus.atlas.SystemInformationGenerator
import org.sonatype.nexus.configuration.application.ApplicationDirectories
import org.sonatype.nexus.plugin.PluginIdentity
import org.sonatype.nexus.util.Tokens
import org.sonatype.sisu.goodies.common.ComponentSupport
import org.sonatype.sisu.goodies.common.Iso8601Date

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
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

  private final ApplicationDirectories applicationDirectories

  private final Provider<SystemStatus> systemStatusProvider

  private final Map<String, String> parameters;

  private final List<PluginIdentity> pluginIdentities

  @Inject
  SystemInformationGeneratorImpl(final BeanLocator beanLocator,
                                 final ApplicationDirectories applicationDirectories,
                                 final Provider<SystemStatus> systemStatusProvider,
                                 final @Parameters Map<String, String> parameters,
                                 final List<PluginIdentity> pluginIdentities)
  {
    this.beanLocator = checkNotNull(beanLocator)
    this.applicationDirectories = checkNotNull(applicationDirectories)
    this.systemStatusProvider = checkNotNull(systemStatusProvider)
    this.parameters = checkNotNull(parameters);
    this.pluginIdentities = checkNotNull(pluginIdentities)
  }

  @Override
  Map report() {
    log.info 'Generating system information report'

    // HACK: provide local references to prevent problems with Groovy BUG accessing private fields
    def beanLocator = this.beanLocator
    def applicationDirectories = this.applicationDirectories
    def systemStatus = this.systemStatusProvider.get()
    def parameters = this.parameters
    def pluginIdentities = this.pluginIdentities

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

    // helper to lookup a component dynamically by class-name
    def lookupComponent = { String className ->
      Class type
      try {
        log.trace 'Looking up component: {}', className
        type = getClass().classLoader.loadClass(className)
        def iter = beanLocator.locate(Key.get(type)).iterator()
        if (iter.hasNext()) {
          return iter.next().getValue()
        }
        else {
          log.trace 'Component not found: {}', className
        }
      }
      catch (Exception e) {
        log.trace 'Unable to load class: {}; ignoring', className, e
      }
      return null
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
        def plm = lookupComponent('org.sonatype.licensing.product.ProductLicenseManager')
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
        def fp = lookupComponent('org.sonatype.licensing.product.util.LicenseFingerprinter')
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

    def reportNexusPlugins = {
      def data = [:]
      pluginIdentities.each {
        def coordinates = it.coordinates
        def item = data[coordinates.artifactId] = [
            'groupId': coordinates.groupId,
            'artifactId': coordinates.artifactId,
            'version': coordinates.version
        ]
      }
      return data
    }

    // masks the value of any properties that look like passwords
    def reportObfuscatedProperties = { properties ->
      return properties.collectEntries { key, value ->
        if (key.toLowerCase(Locale.US).contains('password')) {
          value = Tokens.mask(value)
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
        'nexus-license': reportNexusLicense(),
        'nexus-properties': reportObfuscatedProperties(parameters),
        'nexus-configuration': reportNexusConfiguration(),
        'nexus-plugins': reportNexusPlugins()
    ]

    return sections
  }
}
