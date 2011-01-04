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
import org.sonatype.nexus.mock.components.Window;

import com.thoughtworks.selenium.Selenium;

public class LogConfigTab
    extends Component
{

    private Combobox rootLoggerLevel;

    private TextField rootLoggerAppenders;

    private TextField fileAppenderPattern;

    private TextField fileAppenderLocation;

    private Button savebutton;

    private Button cancelbutton;

    public LogConfigTab( Selenium selenium )
    {
        super( selenium, "window.Ext.getCmp('log-config')" );

        rootLoggerLevel = new Combobox( selenium, expression + ".find('name', 'rootLoggerLevel')[0]" );
        rootLoggerAppenders = new TextField( selenium, expression + ".find('name', 'rootLoggerAppenders')[0]" );
        fileAppenderPattern = new TextField( selenium, expression + ".find('name', 'fileAppenderPattern')[0]" );
        fileAppenderLocation = new TextField( selenium, expression + ".find('name', 'fileAppenderLocation')[0]" );

        savebutton = new Button( selenium, expression + ".formPanel.buttons[0]" );
        cancelbutton = new Button( selenium, expression + ".formPanel.buttons[1]" );
    }

    public Combobox getRootLoggerLevel()
    {
        return rootLoggerLevel;
    }

    public TextField getRootLoggerAppenders()
    {
        return rootLoggerAppenders;
    }

    public TextField getFileAppenderPattern()
    {
        return fileAppenderPattern;
    }

    public TextField getFileAppenderLocation()
    {
        return fileAppenderLocation;
    }

    public Button getSavebutton()
    {
        return savebutton;
    }

    public Button getCancelbutton()
    {
        return cancelbutton;
    }

    public LogConfigTab save()
    {
        savebutton.click();

        new Window( selenium ).waitFor();

        return this;
    }

    public void cancel()
    {
        cancelbutton.click();
    }

}
