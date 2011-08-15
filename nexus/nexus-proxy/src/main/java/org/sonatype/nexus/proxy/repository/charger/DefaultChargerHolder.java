package org.sonatype.nexus.proxy.repository.charger;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.sonatype.sisu.charger.Charger;

/**
 * A simple component just to be able to apply lifecycle to the SISU Charger and shut it down cleanly.
 * 
 * @author cstamas
 */
@Component( role = ChargerHolder.class )
public class DefaultChargerHolder
    implements ChargerHolder, Disposable
{
    @Requirement( hint = "shiro" )
    private Charger charger;

    @Override
    public Charger getCharger()
    {
        return charger;
    }

    @Override
    public void dispose()
    {
        charger.shutdown();
    }
}
