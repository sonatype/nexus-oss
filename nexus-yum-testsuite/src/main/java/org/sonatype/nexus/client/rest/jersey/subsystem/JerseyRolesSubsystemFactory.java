package org.sonatype.nexus.client.rest.jersey.subsystem;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.client.core.Condition;
import org.sonatype.nexus.client.core.condition.NexusStatusConditions;
import org.sonatype.nexus.client.core.spi.SubsystemFactory;
import org.sonatype.nexus.client.core.subsystem.security.Roles;
import org.sonatype.nexus.client.internal.rest.jersey.subsystem.security.JerseyRoles;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;

@Named
@Singleton
public class JerseyRolesSubsystemFactory
    implements SubsystemFactory<Roles, JerseyNexusClient>
{

    @Override
    public Condition availableWhen()
    {
        return NexusStatusConditions.anyModern();
    }

    @Override
    public Class<Roles> getType()
    {
        return Roles.class;
    }

    @Override
    public Roles create( JerseyNexusClient nexusClient )
    {
        return new JerseyRoles( nexusClient );
    }

}
