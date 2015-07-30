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
public final class DockerAttributes
{
  public static final String P_IN_PROGRESS = "inProgress";

  public static final String P_X_DOCKER_CHECKSUM_PAYLOAD = "X-Docker-Checksum-Payload";

  public static final String P_X_DOCKER_SIZE = "X-Docker-Size";

  public static final String P_DAEMON_METADATA = "daemonMetadata";

  public static final String P_LAYER_ID = "layerId";

  public static final String P_LAYER_ANCESTRY = "layerAncestry";

  public static final String P_IMAGE_NAME = "imageName";

  public static final String P_IMAGE_TAG = "imageTag";

  public static final String P_PARENT = "parent";

  public static final String P_ANCESTRY = "ancestry";

  public static final String P_DOCKER_VERSION = "docker_version";

  public static final String P_DOCKER_GO_VERSION = "docker_go_version";

  public static final String P_ARCH = "arch";

  public static final String P_OS = "os";

  public static final String P_KERNEL = "kernel";
}
