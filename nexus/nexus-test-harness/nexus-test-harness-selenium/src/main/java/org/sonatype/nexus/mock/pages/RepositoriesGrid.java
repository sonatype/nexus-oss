package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Grid;

import com.thoughtworks.selenium.Selenium;

public class RepositoriesGrid
    extends Grid
{

    public RepositoriesGrid( Selenium selenium )
    {
        super( selenium, RepositoriesTab.REPOSITORIES_ST + ".gridPanel" );
    }

}
