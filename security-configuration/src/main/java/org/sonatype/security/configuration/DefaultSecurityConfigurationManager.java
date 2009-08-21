package org.sonatype.security.configuration;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.security.configuration.model.SecurityConfiguration;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;
import org.sonatype.security.configuration.validator.SecurityConfigurationValidator;
import org.sonatype.security.configuration.validator.SecurityValidationContext;

@Component( role = SecurityConfigurationManager.class )
public class DefaultSecurityConfigurationManager
    implements SecurityConfigurationManager
{

    @Requirement( hint = "file" )
    private SecurityConfigurationSource configurationSource;

    @Requirement
    private SecurityConfigurationValidator validator;

    @Requirement
    private Logger logger;

    /**
     * This will hold the current configuration in memory, to reload, will need to set this to null
     */
    private SecurityConfiguration configuration = null;

    private ReentrantLock lock = new ReentrantLock();

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
        ValidationResponse vr = validator.validateAnonymousPassword( this
            .initializeContext(), anonymousPassword );

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
        ValidationResponse vr = validator.validateAnonymousUsername( this
            .initializeContext(), anonymousUsername );

        if ( vr.isValid() )
        {
            this.getConfiguration().setAnonymousUsername( anonymousUsername );
        }
        else
        {
            throw new InvalidConfigurationException( vr );
        }
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

}
