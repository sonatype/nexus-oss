package org.sonatype.nexus.proxy.registry;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = ContentClass.class, hint = RootContentClass.ID )
public class RootContentClass
    extends AbstractIdContentClass
{
    public static final String ID = "any";
    public static final String NAME = "Any Content";

    public String getId()
    {
        return ID;
    }
    
    @Override
    public String getName() 
    {
        return NAME;
    };
    
    @Override
    public boolean isCompatible( ContentClass contentClass )
    {
        //root is compatible with all !
        return true;
    }
    
    @Override
    public boolean isGroupable()
    {
        //you can't create repos w/ 'root' type content, so groupable isn't an option
        return false;
    }
}
