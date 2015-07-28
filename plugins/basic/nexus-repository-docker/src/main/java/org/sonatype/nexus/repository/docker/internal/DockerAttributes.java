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
package org.sonatype.nexus.repository.docker.internal;

/**
 * Docker format specific attributes.
 *
 * @since 3.0
 */
public interface DockerAttributes
{
  String P_IN_PROGRESS = "inProgress";

  String P_X_DOCKER_CHECKSUM_PAYLOAD = "X-Docker-Checksum-Payload";

  String P_X_DOCKER_SIZE = "X-Docker-Size";

  String P_DAEMON_METADATA = "daemonMetadata";

  String P_LAYER_ID = "layerId";

  String P_IMAGE_NAME = "imageName";

  String P_IMAGE_TAG = "imageTag";

  String P_PARENT = "parent";

  String P_ANCESTRY = "ancestry";

  String P_DOCKER_VERSION = "docker_version";

  String P_DOCKER_GO_VERSION = "docker_go_version";

  String P_ARCH = "arch";

  String P_OS = "os";

  String P_KERNEL = "kernel";
}
