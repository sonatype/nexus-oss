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

}
