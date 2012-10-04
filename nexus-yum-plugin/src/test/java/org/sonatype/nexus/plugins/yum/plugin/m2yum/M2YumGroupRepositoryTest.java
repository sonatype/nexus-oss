/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.yum.plugin.m2yum;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import org.sonatype.nexus.plugins.yum.AbstractRepositoryTester;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;

public class M2YumGroupRepositoryTest
    extends AbstractRepositoryTester
{

    @Inject
    private RepositoryTypeRegistry typeRegistry;

    @Test
    public void shouldRetrieveNewYumGroupRepositoryType()
        throws Exception
    {
        RepositoryTypeDescriptor desc = typeRegistry.getRepositoryTypeDescriptor( GroupRepository.class, "maven2yum" );
        assertThat( desc, notNullValue() );
    }

    @Test
    public void shouldHasFacet()
        throws Exception
    {
        GroupRepository repo = getContainer().lookup( GroupRepository.class, "maven2yum" );
        assertThat( repo.getRepositoryKind().isFacetAvailable( M2YumGroupRepository.class ), is( true ) );
    }

}
