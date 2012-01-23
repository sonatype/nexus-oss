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
