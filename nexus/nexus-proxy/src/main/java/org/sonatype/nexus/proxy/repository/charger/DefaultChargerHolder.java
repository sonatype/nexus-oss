package org.sonatype.nexus.proxy.repository.charger;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.sisu.charger.Charger;
import org.sonatype.sisu.charger.shiro.DefaultShiroAwareCharger;

/**
 * A simple component just to be able to have central place to switch from different kinds of Charger.
 * 
 * @author cstamas
 */
@Component( role = ChargerHolder.class )
public class DefaultChargerHolder
    implements ChargerHolder
{
    private final Charger charger;

    public DefaultChargerHolder()
    {
        this.charger = new DefaultShiroAwareCharger();
    }

    @Override
    public Charger getCharger()
    {
        return charger;
    }
}
