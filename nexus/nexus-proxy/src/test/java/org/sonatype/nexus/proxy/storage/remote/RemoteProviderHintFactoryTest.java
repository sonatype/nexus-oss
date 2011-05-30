/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.storage.remote;

import org.junit.Assert;
import org.junit.Test;

import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.CommonsHttpClientRemoteStorage;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

public class RemoteProviderHintFactoryTest
    extends PlexusTestCaseSupport
{
    private static final String FAKE_VALUE = "Foo-Bar";

    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        // clear the property
        System.clearProperty( DefaultRemoteProviderHintFactory.DEFAULT_HTTP_PROVIDER_KEY );
    }

    @Test
    public void testIt()
        throws Exception
    {
        RemoteProviderHintFactory hintFactory = this.lookup( RemoteProviderHintFactory.class );

        // clear the property
        System.clearProperty( DefaultRemoteProviderHintFactory.DEFAULT_HTTP_PROVIDER_KEY );

        // nothing set
        Assert.assertEquals( CommonsHttpClientRemoteStorage.PROVIDER_STRING, hintFactory.getDefaultHttpRoleHint() );

        System.setProperty( DefaultRemoteProviderHintFactory.DEFAULT_HTTP_PROVIDER_KEY, FAKE_VALUE );
        Assert.assertEquals( FAKE_VALUE, hintFactory.getDefaultHttpRoleHint() );
    }
}
