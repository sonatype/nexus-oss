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
package org.sonatype.nexus.repository.yum.internal.m2yum;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.test.NexusTestSupport;

public class M2ContentClassTest
    extends NexusTestSupport
{

    @Test
    public void shouldOverrideDefaultMaven2ContentClass()
        throws Exception
    {
        getContainer().addComponent( new M2ContentClass(), ContentClass.class, M2ContentClass.ID );
        Repository repo = getContainer().lookup( Repository.class, M2Repository.ID );
        Assert.assertTrue( repo.getRepositoryContentClass() instanceof M2ContentClass );
    }

}
