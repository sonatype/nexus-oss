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
package org.sonatype.nexus.internal.hazelcast;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import static com.google.common.base.Preconditions.checkState;

/**
 * Expose the shared {@link HazelcastInstance}.
 *
 * @since 3.0
 */
@Named(HazelcastInstanceProvider.INSTANCE_NAME)
@Singleton
public class HazelcastInstanceProvider
  extends ComponentSupport
  implements Provider<HazelcastInstance>
{
  public static final String INSTANCE_NAME = "nexus";

  @Override
  public HazelcastInstance get() {
    HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName(INSTANCE_NAME);
    checkState(instance != null, "Missing instance named: %s", INSTANCE_NAME);
    log.info("Instance: {}", instance);
    return instance;
  }
}
