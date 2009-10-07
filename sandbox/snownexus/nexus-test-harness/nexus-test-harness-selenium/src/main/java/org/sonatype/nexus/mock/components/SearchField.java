package org.sonatype.nexus.mock.components;

import com.thoughtworks.selenium.Selenium;

public class SearchField
    extends TextField
{

    private Button search;
    private Button clear;

    public SearchField( Selenium selenium, String expression )
    {
        super( selenium, expression );

        clear = new Button( selenium, expression + ".triggers[0]" );
        clear.idFunction = ".id";
        search = new Button( selenium, expression + ".triggers[1]" );
        search.idFunction = ".id";
    }

    public SearchField clickSearch()
    {
         search.clickNoWait() ;

        return this;
    }

    public SearchField clickClear()
    {
        clear.clickNoWait() ;

        return this;
    }

}
