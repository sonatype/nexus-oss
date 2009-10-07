package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Combobox;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.Tree;
import org.sonatype.nexus.mock.components.Window;

import com.thoughtworks.selenium.Selenium;

public class RepositoryMirror
    extends Component
{

    private Button addButton;

    private Button cancel;

    private Combobox mirrorUrl;

    private Tree mirrorUrlsList;

    private Button removeAllButton;

    private Button removeButton;

    private Button save;

    public RepositoryMirror( Selenium selenium, String expression )
    {
        super( selenium, expression );

        this.mirrorUrl = new Combobox( selenium, expression + ".find('name', 'mirrorUrl')[0]" );
        this.mirrorUrlsList = new Tree( selenium, expression + ".find('name', 'mirror-url-list')[0]" );

        this.addButton = new Button( selenium, "window.Ext.getCmp('button-add')" );
        this.removeButton = new Button( selenium, "window.Ext.getCmp('button-remove')" );
        this.removeAllButton = new Button( selenium, "window.Ext.getCmp('button-remove-all')" );

        this.save = new Button( selenium, expression + ".buttons[0]" );
        this.save.idFunction = ".id";
        this.cancel = new Button( selenium, expression + ".buttons[1]" );
        this.cancel.idFunction = ".id";
    }

    public final Button getAddButton()
    {
        return addButton;
    }

    public final Button getCancel()
    {
        return cancel;
    }

    public final Combobox getMirrorUrl()
    {
        return mirrorUrl;
    }

    public final Tree getMirrorUrlsList()
    {
        return mirrorUrlsList;
    }

    public final Button getRemoveAllButton()
    {
        return removeAllButton;
    }

    public final Button getRemoveButton()
    {
        return removeButton;
    }

    public final Button getSave()
    {
        return save;
    }

    public RepositoryMirror addMirror( String mirrorId )
    {
        this.mirrorUrl.setValue( mirrorId );

        this.addButton.click();

        return this;
    }

    public RepositoryMirror save()
    {
        this.save.click();

        new Window( selenium ).waitFor();

        return this;
    }

    public RepositoryMirror cancel()
    {
        this.cancel.click();

        return this;
    }

    public RepositoryMirror removeAllMirrors()
    {
        this.removeAllButton.click();

        return this;
    }

    public RepositoryMirror removeMirror( String id )
    {
        this.mirrorUrlsList.select( id );

        this.removeButton.click();

        return this;
    }

}
