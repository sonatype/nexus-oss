package org.sonatype.nexus.proxy.repository.charger;

import org.apache.shiro.SecurityUtils;
import org.codehaus.plexus.component.annotations.Component;

@Component( role = Charger.class, hint = "shiro" )
public class DefaultShiroAwareCharger
    extends DefaultCharger
{
    @Override
    protected <E> Charge<E> getChargeInstance( final ChargeStrategy<E> strategy )
    {
        return new ShiroAwareCharge<E>( strategy, SecurityUtils.getSubject() );
    }
}
