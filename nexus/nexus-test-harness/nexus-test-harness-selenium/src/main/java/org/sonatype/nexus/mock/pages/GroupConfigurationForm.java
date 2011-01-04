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
import org.sonatype.nexus.mock.components.TwinPanel;

import com.thoughtworks.selenium.Selenium;

public class GroupConfigurationForm
    extends Component
{

    private TextField id;

    private TextField name;

    private Combobox provider;

    private TextField format;

    private Combobox publishUrl;

    private TwinPanel repositories;

    private Button saveButton;

    private Button cancelButton;

    public GroupConfigurationForm( Selenium selenium, String expression )
    {
        super( selenium, expression );

        id = new TextField( selenium, expression + ".find('name', 'id')[0]" );
        name = new TextField( selenium, expression + ".find('name', 'name')[0]" );
        provider = new Combobox( selenium, expression + ".find('name', 'provider')[0]" );
        format = new TextField( selenium, expression + ".find('name', 'format')[0]" );
        publishUrl = new Combobox( selenium, expression + ".find('name', 'exposed')[0]" );
        repositories = new TwinPanel( selenium, expression + ".find('name', 'repositories')[0]" );

        saveButton = new Button( selenium, expression + ".buttons[0]" );
        saveButton.idFunction = ".id";
        cancelButton = new Button( selenium, expression + ".buttons[1]" );
        cancelButton.idFunction = ".id";
    }

    public GroupConfigurationForm populate( String id, String name, String provider, boolean publishUrl,
                                            String... repositories )
    {
        this.id.type( id );
        this.name.type( name );
        this.provider.setValue( provider );
        this.publishUrl.setValue( String.valueOf( publishUrl ) );
        for ( String repo : repositories )
        {
            this.repositories.add( repo );
        }

        return this;
    }

    public GroupConfigurationForm save()
    {
        saveButton.click();

        return this;
    }

    public TextField getIdField()
    {
        return id;
    }

    public TextField getName()
    {
        return name;
    }

    public Combobox getProvider()
    {
        return provider;
    }

    public TextField getFormat()
    {
        return format;
    }

    public Combobox getPublishUrl()
    {
        return publishUrl;
    }

    public TwinPanel getRepositories()
    {
        return repositories;
    }

    public Button getSaveButton()
    {
        return saveButton;
    }

    public Button getCancelButton()
    {
        return cancelButton;
    }

}
