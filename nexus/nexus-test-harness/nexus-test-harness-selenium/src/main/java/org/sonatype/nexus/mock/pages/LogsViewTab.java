package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.Checkbox;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.Menu;
import org.sonatype.nexus.mock.components.TextArea;
import org.sonatype.nexus.mock.components.TextField;

import com.thoughtworks.selenium.Selenium;

public class LogsViewTab
    extends Component
{

    private Button reload;

    private Button download;

    private Button selectDocument;

    private Checkbox tail;

    private Button tailUpdate;

    private Menu documents;

    private TextArea logContent;

    public LogsViewTab( Selenium selenium )
    {
        super( selenium, "window.Ext.getCmp('view-logs')" );

        reload = new Button( selenium, expression + ".topToolbar.items.items[0]" );
        download = new Button( selenium, expression + ".topToolbar.items.items[1]" );
        selectDocument = new Button( selenium, expression + ".topToolbar.items.items[2]" );
        documents = new Menu( selenium, selectDocument.getExpression() + ".menu" );
        tail = new Checkbox( selenium, expression + ".topToolbar.items.items[5]" );
        tailUpdate = new Button( selenium, expression + ".tailUpdateButton" );

        logContent = new TextArea( selenium, "window.Ext.getCmp('log-text')" );
    }

    public Button getReload()
    {
        return reload;
    }

    public Button getDownload()
    {
        return download;
    }

    public Button getSelectDocument()
    {
        return selectDocument;
    }

    public Checkbox getTail()
    {
        return tail;
    }

    public Button getTailUpdate()
    {
        return tailUpdate;
    }

    public Menu getDocuments()
    {
        return documents;
    }

    public String getContent()
    {
        this.logContent.waitToLoad();

        try
        {
            Thread.sleep( 1000 );
        }
        catch ( InterruptedException e )
        {
            // just ignore
        }

        return this.logContent.getValue();
    }

    public void selectFile( String fileName )
    {
        selectDocument.click();

        try
        {
            Thread.sleep( 1000 );
        }
        catch ( InterruptedException e )
        {
            // just ignore
        }

        documents.click( fileName );
    }

    public TextField getLogContent()
    {
        return logContent;
    }

}
