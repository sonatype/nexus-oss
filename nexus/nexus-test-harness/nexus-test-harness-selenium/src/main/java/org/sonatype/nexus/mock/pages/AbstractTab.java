package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.Grid;
import org.sonatype.nexus.mock.components.Menu;

import com.thoughtworks.selenium.Selenium;

public class AbstractTab
    extends Component
{

    protected Grid grid;

    protected Button refreshButton;

    protected Button addButton;

    protected Button deleteButton;

    protected Menu addMenu;

    public AbstractTab( Selenium selenium, String expression )
    {
        super( selenium, expression );

        this.grid = new Grid( selenium, expression + ".gridPanel" );

        this.refreshButton = new Button( selenium, expression + ".refreshButton" );
        this.deleteButton = new Button( selenium, expression + ".toolbarDeleteButton" );

        this.addButton = new Button( selenium, expression + ".toolbarAddButton" );
        this.addMenu = new Menu( selenium, addButton.getExpression() + ".menu" );
    }

    public MessageBox delete()
    {
        this.deleteButton.click();

        return new MessageBox( selenium );
    }


    public Grid getGrid()
    {
        return grid;
    }

    public Button getRefreshButton()
    {
        return refreshButton;
    }

    public Button getAddButton()
    {
        return addButton;
    }

    public Button getDeleteButton()
    {
        return deleteButton;
    }

    public Menu getAddMenu()
    {
        return addMenu;
    }

}
