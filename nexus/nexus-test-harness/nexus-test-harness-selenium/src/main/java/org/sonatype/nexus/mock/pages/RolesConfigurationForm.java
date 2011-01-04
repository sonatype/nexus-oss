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
package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.components.TwinPanel;

import com.thoughtworks.selenium.Selenium;

public class RolesConfigurationForm
    extends Component
{

    private TextField roleId;

    private TextField name;

    private TextField description;

    private TwinPanel privileges;

    private Button saveButton;

    private Button cancelButton;

    public RolesConfigurationForm( Selenium selenium, String expression )
    {
        super( selenium, expression );

        roleId = new TextField( selenium, expression + ".find('name', 'id')[0]" );
        name = new TextField( selenium, expression + ".find('name', 'name')[0]" );
        description = new TextField( selenium, expression + ".find('name', 'description')[0]" );
        privileges = new TwinPanel( selenium, expression + ".find('name', 'privileges')[0]" );

        saveButton = new Button( selenium, expression + ".buttons[0]" );
        cancelButton = new Button( selenium, expression + ".buttons[1]" );
    }

    public final TextField getRoleId()
    {
        return roleId;
    }

    public final TextField getName()
    {
        return name;
    }

    public final TextField getDescription()
    {
        return description;
    }

    public final TwinPanel getPrivileges()
    {
        return privileges;
    }

    public RolesConfigurationForm save()
    {
        saveButton.click();

        return this;
    }

    public RolesConfigurationForm cancel()
    {
        cancelButton.click();

        return this;
    }

    public RolesConfigurationForm populate( String roleId, String name, String... privs )
    {
        this.roleId.type( roleId );
        this.name.type( name );
        for ( String priv : privs )
        {
            this.privileges.add( priv );
        }

        return this;
    }

}
