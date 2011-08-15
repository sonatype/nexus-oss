package org.sonatype.nexus.proxy.repository.charger;

import org.sonatype.sisu.charger.Charger;

public interface ChargerHolder
{
    Charger getCharger();
}
