package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.jsecurity.NexusSecurity;

@Component( role = SecurityConfigAdaptorPersistor.class )
public class DefaultSecurityConfigAdaptorPersistor
    implements SecurityConfigAdaptorPersistor
{

    @Requirement
    private NexusSecurity nexusSecurity;

    @Requirement
    private Nexus nexus;

    public void persistRepositoryTarget( CRepositoryTarget repoTarget )
    {
        try
        {
            nexus.createRepositoryTarget( repoTarget );
        }
        catch ( ConfigurationException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void persistSecurityPrivilege( SecurityPrivilege privilege )
    {
        try
        {
            nexusSecurity.createPrivilege( privilege );
        }
        catch ( InvalidConfigurationException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void persistSecurityRole( SecurityRole role )
    {
        try
        {
            nexusSecurity.createRole( role );
        }
        catch ( InvalidConfigurationException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void persistSecurityUser( SecurityUser user )
    {
        try
        {
            nexusSecurity.createUser( user );
        }
        catch ( InvalidConfigurationException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
