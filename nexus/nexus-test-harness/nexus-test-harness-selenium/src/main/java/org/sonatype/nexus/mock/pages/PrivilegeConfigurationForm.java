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
import org.sonatype.nexus.mock.components.Combobox;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.components.Window;

import com.thoughtworks.selenium.Selenium;

public class PrivilegeConfigurationForm
    extends Component
{

    private Button cancelButton;

    private TextField description;

    private TextField method;

    private TextField name;

    private TextField repositoryGroupId;

    private TextField repositoryId;

    private Combobox repositoryOrGroup;

    private Combobox repoTarget;

    private Button saveButton;

    private Combobox type;

    public PrivilegeConfigurationForm( Selenium selenium, String expression )
    {
        super( selenium, expression );

        name = new TextField( selenium, expression + ".find('name', 'name')[0]" );
        description = new TextField( selenium, expression + ".find('name', 'description')[0]" );
        type = new Combobox( selenium, expression + ".find('name', 'type')[0]" );
        repositoryOrGroup = new Combobox( selenium, expression + ".find('name', 'repositoryOrGroup')[0]" );
        repoTarget = new Combobox( selenium, expression + ".find('name', 'repositoryTargetId')[0]" );

        saveButton = new Button( selenium, expression + ".buttons[0]" );
        cancelButton = new Button( selenium, expression + ".buttons[1]" );

        repositoryId = new TextField( selenium, expression + ".find('name', 'repositoryId')[0]" );
        repositoryGroupId = new TextField( selenium, expression + ".find('name', 'repositoryGroupId')[0]" );
        method = new TextField( selenium, expression + ".find('name', 'method')[0]" );
    }

    public void cancel()
    {
        cancelButton.click();
    }

    public final Button getCancelButton()
    {
        return cancelButton;
    }

    public final TextField getDescription()
    {
        return description;
    }

    public final TextField getMethod()
    {
        return method;
    }

    public final TextField getName()
    {
        return name;
    }

    public final TextField getRepositoryGroupId()
    {
        return repositoryGroupId;
    }

    public final TextField getRepositoryId()
    {
        return repositoryId;
    }

    public final Combobox getRepositoryOrGroup()
    {
        return repositoryOrGroup;
    }

    public final Combobox getRepoTarget()
    {
        return repoTarget;
    }

    public final Button getSaveButton()
    {
        return saveButton;
    }

    public final Combobox getType()
    {
        return type;
    }

    public PrivilegeConfigurationForm populate( String name, String description, int repository, int target )
    {
        this.name.type( name );
        this.description.type( description );
        if ( repository != -1 )
        {
            this.repositoryOrGroup.select( repository );
        }
        if ( target != -1 )
        {
            this.repoTarget.select( target );
        }

        return this;
    }

    public PrivilegeConfigurationForm populate( String name, String description, String repoId, String targetId )
    {
        populate( name, description, -1, -1 );
        this.repoTarget.setValue( targetId );
        this.repositoryOrGroup.setValue( String.valueOf( repoId ) );

        return this;
    }

    public PrivilegeConfigurationForm save()
    {
        saveButton.click();

        new Window( selenium ).waitFor();

        return this;
    }

}
