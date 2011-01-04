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
