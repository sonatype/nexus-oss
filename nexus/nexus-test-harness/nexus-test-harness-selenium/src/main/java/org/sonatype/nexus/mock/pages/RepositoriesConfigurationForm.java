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

import com.thoughtworks.selenium.Selenium;

public class RepositoriesConfigurationForm
    extends Component
{

    private TextField id;

    private TextField name;

    private TextField type;

    private Combobox provider;

    private TextField format;

    private Combobox repoPolicy;

    private TextField defaultLocalStorage;

    private TextField overrideLocalStorage;

    private Combobox allowWrite;

    private Combobox allowBrowsing;

    private Combobox includeInSearch;

    private TextField notFoundCache;

    private Button saveButton;

    private Button cancelButton;

    private Combobox shadowOf;

    private TextField remoteStorageUrl;

    private Combobox downloadRemoteIndexes;

    private Combobox checksumPolicy;

    public RepositoriesConfigurationForm( Selenium selenium, String expression )
    {
        super( selenium, expression );

        id = new TextField( selenium, expression + ".find('name', 'id')[0]" );
        name = new TextField( selenium, expression + ".find('name', 'name')[0]" );
        type = new TextField( selenium, expression + ".find('name', 'repoType')[0]" );
        provider = new Combobox( selenium, expression + ".find('name', 'provider')[0]" );
        format = new TextField( selenium, expression + ".find('name', 'format')[0]" );
        repoPolicy = new Combobox( selenium, expression + ".find('name', 'repoPolicy')[0]" );
        defaultLocalStorage = new TextField( selenium, expression + ".find('name', 'defaultLocalStorageUrl')[0]" );
        overrideLocalStorage = new TextField( selenium, expression + ".find('name', 'overrideLocalStorageUrl')[0]" );

        shadowOf = new Combobox( selenium, expression + ".find('name', 'shadowOf')[0]" );

        remoteStorageUrl = new TextField( selenium, expression + ".find('name', 'remoteStorage.remoteStorageUrl')[0]" );
        downloadRemoteIndexes = new Combobox( selenium, expression + ".find('name', 'downloadRemoteIndexes')[0]" );
        checksumPolicy = new Combobox( selenium, expression + ".find('name', 'checksumPolicy')[0]" );

        allowWrite = new Combobox( selenium, expression + ".find('name', 'allowWrite')[0]" );
        allowBrowsing = new Combobox( selenium, expression + ".find('name', 'browseable')[0]" );
        includeInSearch = new Combobox( selenium, expression + ".find('name', 'indexable')[0]" );

        notFoundCache = new TextField( selenium, expression + ".find('name', 'notFoundCacheTTL')[0]" );

        saveButton = new Button( selenium, expression + ".buttons[0]" );
        saveButton.idFunction = ".id";
        cancelButton = new Button( selenium, expression + ".buttons[1]" );
    }

    public RepositoriesConfigurationForm populate( String id, String name )
    {
        this.id.type( id );
        this.name.type( name );

        return this;
    }

    public RepositoriesConfigurationForm save()
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

    public TextField getType()
    {
        return type;
    }

    public Combobox getProvider()
    {
        return provider;
    }

    public TextField getFormat()
    {
        return format;
    }

    public Combobox getRepoPolicy()
    {
        return repoPolicy;
    }

    public TextField getDefaultLocalStorage()
    {
        return defaultLocalStorage;
    }

    public TextField getOverrideLocalStorage()
    {
        return overrideLocalStorage;
    }

    public Combobox getAllowWrite()
    {
        return allowWrite;
    }

    public Combobox getAllowBrowsing()
    {
        return allowBrowsing;
    }

    public Combobox getIncludeInSearch()
    {
        return includeInSearch;
    }

    public TextField getNotFoundCache()
    {
        return notFoundCache;
    }

    public Combobox getShadowOf()
    {
        return shadowOf;
    }

    public RepositoriesConfigurationForm populateVirtual( String repoId, String name, String provider, String shadowOf )
    {
        this.provider.setValue( provider );
        this.shadowOf.setValue( shadowOf );
        return populate( repoId, name );
    }

    public RepositoriesConfigurationForm populateProxy( String repoId, String name, String remoteStorageUrl )
    {
        this.remoteStorageUrl.type( remoteStorageUrl );
        this.downloadRemoteIndexes.setValue( "false" );
        this.checksumPolicy.setValue( "IGNORE" );
        return populate( repoId, name );
    }

}
