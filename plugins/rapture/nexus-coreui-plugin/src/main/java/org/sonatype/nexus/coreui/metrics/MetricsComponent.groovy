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

package org.sonatype.nexus.coreui.metrics

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import com.yammer.metrics.core.MetricsRegistry
import com.yammer.metrics.core.VirtualMachineMetrics
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport

import javax.inject.Named
import javax.inject.Singleton
import javax.inject.Inject

/**
 * Metrics {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_Metrics')
class MetricsComponent
    extends DirectComponentSupport
{
  @Inject
  VirtualMachineMetrics vm

  @Inject
  MetricsRegistry registry

  @DirectMethod
  @RequiresPermissions('nexus:metrics-endpoints')
  MemoryXO memory() {
    return new MemoryXO(
        totalInit: vm.totalInit(),
        totalUsed: vm.totalUsed(),
        totalMax: vm.totalMax(),
        totalCommitted: vm.totalCommitted(),
        heapInit: vm.heapInit(),
        heapUsed: vm.heapUsed(),
        heapMax: vm.heapMax(),
        heapCommitted: vm.heapCommitted(),
    )
  }
}
