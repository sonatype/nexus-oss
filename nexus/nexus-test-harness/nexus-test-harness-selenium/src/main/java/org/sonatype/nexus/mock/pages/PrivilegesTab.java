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
package org.sonatype.nexus.mock.pages;

import com.thoughtworks.selenium.Selenium;

public class PrivilegesTab
    extends AbstractTab
{

    public static final String PRIVS_ST = "window.Ext.getCmp('security-privileges')";

    public PrivilegesTab( Selenium selenium )
    {
        super( selenium, PRIVS_ST );
    }

    public PrivilegeConfigurationForm addPrivilege()
    {
        addButton.click();

        addMenu.click( "text", "Repository Target Privilege" );

        return new PrivilegeConfigurationForm( selenium, expression
            + ".cardPanel.getLayout().activeItem.getLayout().activeItem" );
    }

    public PrivilegeConfigurationForm select( String privId )
    {
        grid.select( privId );

        return new PrivilegeConfigurationForm( selenium, expression + ".cardPanel.getLayout().activeItem" );
        //return new PrivilegeEditTabs( selenium, expression + ".cardPanel.getLayout().activeItem.tabPanel" );
    }

}
