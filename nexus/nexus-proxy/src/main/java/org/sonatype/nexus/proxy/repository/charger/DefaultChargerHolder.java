package org.sonatype.nexus.proxy.repository.charger;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.sisu.charger.Charger;

/**
 * A simple component just to be able to have central place to switch from different kinds of Charger.
 * 
 * @author cstamas
 */
@Component( role = ChargerHolder.class )
public class DefaultChargerHolder
    implements ChargerHolder
{
    @Requirement( hint = "shiro" )
    private Charger charger;

    @Override
    public Charger getCharger()
    {
        return charger;
    }
}
