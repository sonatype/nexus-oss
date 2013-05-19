/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.configuration;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.security.configuration.model.SecurityConfiguration;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;
import org.sonatype.security.configuration.validator.SecurityConfigurationValidator;
import org.sonatype.security.configuration.validator.SecurityValidationContext;

@Singleton
@Typed( SecurityConfigurationManager.class )
@Named( "default" )
public class DefaultSecurityConfigurationManager
    implements SecurityConfigurationManager
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final SecurityConfigurationSource configurationSource;

    private final SecurityConfigurationValidator validator;

    /**
     * This will hold the current configuration in memory, to reload, will need to set this to null
     */
    private SecurityConfiguration configuration = null;

    private ReentrantLock lock = new ReentrantLock();

    @Inject
    public DefaultSecurityConfigurationManager( @Named( "file" ) SecurityConfigurationSource configurationSource,
                                                SecurityConfigurationValidator validator )
    {
        this.configurationSource = configurationSource;
        this.validator = validator;
    }

    public boolean isEnabled()
    {
        return this.getConfiguration().isEnabled();
    }

    public void setEnabled( boolean enabled )
    {
        this.getConfiguration().setEnabled( enabled );
    }

    public boolean isAnonymousAccessEnabled()
    {
        return this.getConfiguration().isAnonymousAccessEnabled();
    }

    public void setAnonymousAccessEnabled( boolean anonymousAccessEnabled )
    {
        this.getConfiguration().setAnonymousAccessEnabled( anonymousAccessEnabled );
    }

    public String getAnonymousPassword()
    {
        return this.getConfiguration().getAnonymousPassword();
    }

    public void setAnonymousPassword( String anonymousPassword )
        throws InvalidConfigurationException
    {
        ValidationResponse vr = validator.validateAnonymousPassword( this.initializeContext(), anonymousPassword );

        if ( vr.isValid() )
        {
            this.getConfiguration().setAnonymousPassword( anonymousPassword );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    public String getAnonymousUsername()
    {
        return this.getConfiguration().getAnonymousUsername();
    }

    public void setAnonymousUsername( String anonymousUsername )
        throws InvalidConfigurationException
    {
        ValidationResponse vr = validator.validateAnonymousUsername( this.initializeContext(), anonymousUsername );

        if ( vr.isValid() )
        {
            this.getConfiguration().setAnonymousUsername( anonymousUsername );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }
    
    public int getHashIterations()
    {
    	return this.getConfiguration().getHashIterations();
    }

    public List<String> getRealms()
    {
        return Collections.unmodifiableList( this.getConfiguration().getRealms() );
    }

    public void setRealms( List<String> realms )
        throws InvalidConfigurationException
    {
        ValidationResponse vr = validator.validateRealms( this.initializeContext(), realms );

        if ( vr.isValid() )
        {
            this.getConfiguration().setRealms( realms );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
    }

    private SecurityConfiguration getConfiguration()
    {
        if ( configuration != null )
        {
            return configuration;
        }

        lock.lock();

        try
        {
            this.configurationSource.loadConfiguration();

            configuration = this.configurationSource.getConfiguration();
        }
        catch ( IOException e )
        {
            this.logger.error( "IOException while retrieving configuration file", e );
        }
        catch ( ConfigurationException e )
        {
            this.logger.error( "Invalid Configuration", e );
        }
        finally
        {
            lock.unlock();
        }

        return configuration;
    }

    public void clearCache()
    {
        // Just to make sure we aren't fiddling w/ save/loading process
        lock.lock();
        configuration = null;
        lock.unlock();
    }

    public void save()
    {
        lock.lock();

        try
        {
            this.configurationSource.storeConfiguration();
        }
        catch ( IOException e )
        {
            this.logger.error( "IOException while storing configuration file", e );
        }
        finally
        {
            lock.unlock();
        }
    }

    private SecurityValidationContext initializeContext()
    {
        SecurityValidationContext context = new SecurityValidationContext();
        context.setSecurityConfiguration( this.getConfiguration() );

        return context;
    }

    public String getSecurityManager()
    {
        String sm = this.getConfiguration().getSecurityManager();
        if ( sm == null )
        {
            setSecurityManager( "default" );
            return this.getConfiguration().getSecurityManager();
        }
        return sm;
    }

    public void setSecurityManager( String securityManager )
    {
        this.getConfiguration().setSecurityManager( securityManager );
        save();
    }

}
