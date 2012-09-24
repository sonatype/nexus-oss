package org.sonatype.nexus.client.rest.jersey.subsystem;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.client.core.Condition;
import org.sonatype.nexus.client.core.condition.NexusStatusConditions;
import org.sonatype.nexus.client.core.spi.SubsystemFactory;
import org.sonatype.nexus.client.core.subsystem.security.Users;
import org.sonatype.nexus.client.internal.rest.jersey.subsystem.security.JerseyUsers;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;

@Named
@Singleton
public class JerseyUsersSubsystemFactory
    implements SubsystemFactory<Users, JerseyNexusClient>
{

    @Override
    public Condition availableWhen()
    {
        return NexusStatusConditions.anyModern();
    }

    @Override
    public Class<Users> getType()
    {
        return Users.class;
    }

    @Override
    public Users create( JerseyNexusClient nexusClient )
    {
        return new JerseyUsers( nexusClient );
    }

}
