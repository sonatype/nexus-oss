package org.sonatype.nexus.mock.components;


import com.thoughtworks.selenium.Selenium;

public class Fieldset
    extends Component
{

    private Checkbox checkbox;

    public Fieldset( Selenium selenium, String expression )
    {
        super( selenium, expression );

        checkbox = new Checkbox(selenium, expression+".checkbox");
        checkbox.idFunction=".id";
    }

    public Fieldset check( boolean enable )
    {
        checkbox.check( enable );

        return this;
    }

}
