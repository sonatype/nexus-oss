package org.sample.plugin;

import javax.inject.Named;

@Named( "simple" )
public class SimpleCustomComponent
    implements CustomComponent
{
    public String sayHello()
    {
        return "simple hello!";
    }
}
