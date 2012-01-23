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
package org.sonatype.nexus.selenium.nexus1060;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RolesConfigurationForm;
import org.sonatype.nexus.mock.pages.RolesTab;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus1060InteralRolesTest.class )
public class Nexus1060InteralRolesTest
    extends SeleniumTest
{
    @Test
    public void internalRoles()
        throws InterruptedException
    {
        doLogin();

        RolesTab roles = main.openRoles();

        RolesConfigurationForm role = roles.select( "anonymous" ).selectConfiguration();

        Assert.assertTrue( role.getRoleId().isDisabled() );
        Assert.assertTrue( role.getName().isDisabled() );
        Assert.assertTrue( role.getDescription().isDisabled() );
    }
}
