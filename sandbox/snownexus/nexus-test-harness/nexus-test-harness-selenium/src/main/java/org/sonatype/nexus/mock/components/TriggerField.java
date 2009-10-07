package org.sonatype.nexus.mock.components;

import com.thoughtworks.selenium.Selenium;

public class TriggerField
    extends TextField
{

    private Button trigger;

    public TriggerField( Selenium selenium, String expression )
    {
        super( selenium, expression );

        trigger = new Button( selenium, expression + ".trigger" );
        trigger.idFunction = ".id";
    }

    public TriggerField clickTrigger()
    {
        selenium.click( trigger.getId() );
        selenium.click( trigger.getXPath() );

        return this;
    }

}
