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
