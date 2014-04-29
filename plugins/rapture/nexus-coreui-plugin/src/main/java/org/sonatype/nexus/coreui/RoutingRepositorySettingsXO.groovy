/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.coreui

import groovy.transform.ToString
import org.apache.bval.constraints.NotEmpty

import javax.validation.constraints.NotNull

/**
 * Routing Repository Settings exchange object.
 *
 * @since 3.0
 */
@ToString(includePackage = false, includeNames = true)
class RoutingRepositorySettingsXO
{
  @NotNull
  @NotEmpty
  String repositoryId

  String publishStatus
  String publishMessage
  Long publishTimestamp
  String publishUrl

  Boolean discoveryEnabled = false
  Integer discoveryInterval
  String discoveryStatus
  String discoveryMessage
  Long discoveryTimestamp

}
