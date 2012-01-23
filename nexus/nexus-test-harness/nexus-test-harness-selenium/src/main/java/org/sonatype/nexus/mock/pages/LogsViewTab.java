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
import org.sonatype.nexus.mock.components.Checkbox;
import org.sonatype.nexus.mock.components.Component;
import org.sonatype.nexus.mock.components.Menu;
import org.sonatype.nexus.mock.components.TextArea;

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

        logContent.waitToLoad();
    }

    public TextArea getLogContent()
    {
        return logContent;
    }

}
