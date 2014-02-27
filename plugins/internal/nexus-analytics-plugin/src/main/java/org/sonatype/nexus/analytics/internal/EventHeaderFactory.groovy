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
package org.sonatype.nexus.analytics.internal

import com.google.inject.Key
import org.eclipse.sisu.inject.BeanLocator
import org.sonatype.nexus.ApplicationStatusSource
import org.sonatype.nexus.SystemStatus
import org.sonatype.nexus.analytics.EventHeader
import org.sonatype.sisu.goodies.common.ComponentSupport

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Factory to create {@link EventHeader} instances.
 *
 * @since 2.8
 */
@Named
@Singleton
class EventHeaderFactory
    extends ComponentSupport
{
  static final String FORMAT = 'zip-bundle/1'

  private final BeanLocator beanLocator

  private final String product

  private String node

  @Inject
  EventHeaderFactory(final BeanLocator beanLocator,
                      final ApplicationStatusSource applicationStatusSource)
  {
    assert beanLocator != null
    this.beanLocator = beanLocator

    // calculate the product identifier
    assert applicationStatusSource != null
    SystemStatus status = applicationStatusSource.systemStatus
    this.product = "nexus/${status.editionShort}/${status.version}"
    log.info("Product: {}", product);
  }

  void setNode(final String node) {
    this.node = node
  }

  EventHeader create() {
    assert node != null : 'Missing node identifier'

    EventHeader header = new EventHeader(
        format: FORMAT,
        product: product,
        node: node
    )

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

    // Maybe add organization detail as license fingerprint
    def fp = lookupComponent('org.sonatype.licensing.product.util.LicenseFingerprinter')
    if (fp) {
      try {
        header.organization = fp.calculate()
      }
      catch (Exception e) {
        log.warn('Unable to determine license fingerprint', e)
      }
    }

    return header
  }
}
