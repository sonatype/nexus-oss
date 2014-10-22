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
package org.sonatype.nexus.component.services.internal.id;

import java.util.Set;

import org.sonatype.nexus.component.model.ComponentId;
import org.sonatype.nexus.component.services.id.ComponentIdFactory;

import com.google.common.collect.Sets;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DefaultComponentIdFactoryTest
{
  @Test
  public void restoredIdsAreEqual() {
    final ComponentIdFactory factory = new DefaultComponentIdFactory();

    final ComponentId id = factory.newId();

    final ComponentId restored = factory.fromUniqueString(id.asUniqueString());

    assertThat(id, is(equalTo(restored)));
  }

  @Test
  public void subsequentIdsAreNotEqual() {
    final ComponentIdFactory factor = new DefaultComponentIdFactory();

    Set<ComponentId> ids = Sets.newHashSet();

    final int NUMBER_OF_IDS = 100;

    for (int i = 0; i < NUMBER_OF_IDS; i++) {
      ids.add(factor.newId());
    }

    assertThat(ids.size(), is(equalTo(NUMBER_OF_IDS)));
  }
}
