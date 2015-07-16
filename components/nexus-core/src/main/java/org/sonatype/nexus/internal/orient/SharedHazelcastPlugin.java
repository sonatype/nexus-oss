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
package org.sonatype.nexus.internal.orient;

import java.io.FileNotFoundException;
import java.util.Set;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.orientechnologies.orient.server.hazelcast.OHazelcastPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link OHazelcastPlugin} using a shared {@link HazelcastInstance}.
 *
 * @since 3.0
 */
public class SharedHazelcastPlugin
    extends OHazelcastPlugin
{
  private static final Logger log = LoggerFactory.getLogger(SharedHazelcastPlugin.class);

  @Override
  protected HazelcastInstance configureHazelcast() throws FileNotFoundException {
    Set<HazelcastInstance> instances = Hazelcast.getAllHazelcastInstances();
    log.info("Instances: {}", instances);

    // TODO: Should probably expose the instance-name in config (is that possible),
    // TODO: ... and then look up return that, and configure the cellar to set an appopriate instance-name
    // TODO: ... may need to customize HazelcastConfigurationManager to do this?
    //HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName("nexus");
    //checkState(instance != null);

    // HACK: For now pick the first instance, pending something saner
    return instances.iterator().next();
  }
}
