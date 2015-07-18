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
package org.sonatype.nexus.coreui.fabric

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

import org.sonatype.nexus.extdirect.DirectComponentSupport

import com.hazelcast.core.HazelcastInstance
import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.shiro.authz.annotation.RequiresPermissions

/**
 * Node component.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = "coreui_Node")
class NodeComponent
    extends DirectComponentSupport
{
  @Inject
  HazelcastInstance hazelcastInstance

  @DirectMethod
  @RequiresPermissions('nexus:nodes:read')
  List<NodeXO> read() {
    def result = []
    hazelcastInstance.cluster.members.each {
      result << new NodeXO(
          uuid: it.uuid,
          local: it.localMember(),
          socketAddress: it.socketAddress.toString(),
          attributes: it.attributes
      )
    }
    return result
  }
}
