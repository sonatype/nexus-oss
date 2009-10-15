package org.sonatype.nexus.proxy.target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.CRepositoryTargetCoreConfiguration;
import org.sonatype.nexus.configuration.validator.ApplicationConfigurationValidator;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The default implementation of target registry.
 * 
 * @author cstamas
 */
@Component( role = TargetRegistry.class )
public class DefaultTargetRegistry
    extends AbstractConfigurable
    implements TargetRegistry
{
    @Requirement
    private Logger logger;

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Requirement
    private ApplicationConfigurationValidator validator;

    private ArrayList<Target> targets;

    // ==

    protected Logger getLogger()
    {
        return logger;
    }

    // ==

    @Override
    protected void initializeConfiguration()
        throws ConfigurationException
    {
        if ( getApplicationConfiguration().getConfigurationModel() != null )
        {
            configure( getApplicationConfiguration() );
        }
    }

    @Override
    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    @Override
    protected Configurator getConfigurator()
    {
        return null;
    }

    @Override
    protected List<CRepositoryTarget> getCurrentConfiguration( boolean forWrite )
    {
        return ( (CRepositoryTargetCoreConfiguration) getCurrentCoreConfiguration() ).getConfiguration( forWrite );
    }

    @Override
    protected CoreConfiguration wrapConfiguration( Object configuration )
        throws ConfigurationException
    {
        if ( configuration instanceof ApplicationConfiguration )
        {
            return new CRepositoryTargetCoreConfiguration( (ApplicationConfiguration) configuration );
        }
        else
        {
            throw new ConfigurationException( "The passed configuration object is of class \""
                + configuration.getClass().getName() + "\" and not the required \""
                + ApplicationConfiguration.class.getName() + "\"!" );
        }
    }

    public boolean commitChanges()
        throws ConfigurationException
    {
        boolean wasDirty = super.commitChanges();

        if ( wasDirty )
        {
            targets = null;
        }

        return wasDirty;
    }

    // ==

    protected Target convert( CRepositoryTarget target )
    {
        ContentClass contentClass = getContentClassById( target.getContentClass() );

        // If content class is null, we have a target for a repo type that no longer exists
        // plugin was removed most likely, so we ignore in this case
        if ( contentClass != null )
        {
            return new Target( target.getId(), target.getName(), contentClass, target.getPatterns() );
    }

        return null;
    }

    protected CRepositoryTarget convert( Target target )
    {
        CRepositoryTarget result = new CRepositoryTarget();
        result.setId( target.getId() );
        result.setName( target.getName() );
        result.setContentClass( target.getContentClass().getId() );
        ArrayList<String> patterns = new ArrayList<String>( target.getPatternTexts().size() );
        patterns.addAll( target.getPatternTexts() );
        result.setPatterns( patterns );

        return result;
    }

    protected void validate( CRepositoryTarget target )
        throws InvalidConfigurationException
    {
        ValidationResponse response = this.validator.validateRepositoryTarget( null, target );
        if ( !response.isValid() )
        {
            throw new InvalidConfigurationException( response );
        }
    }

    protected ContentClass getContentClassById( String id )
    {
        return repositoryTypeRegistry.getContentClasses().get( id );
    }

    // ==

    public Collection<Target> getRepositoryTargets()
    {
        if ( targets == null )
        {
            List<CRepositoryTarget> ctargets = getCurrentConfiguration( false );

            targets = new ArrayList<Target>( ctargets.size() );

            for ( CRepositoryTarget ctarget : ctargets )
            {
                Target target = convert( ctarget );

                if ( target != null )
                {
                    targets.add( target );
            }
        }
        }

        // copy the list, since processing it may take longer
        ArrayList<Target> result = new ArrayList<Target>( targets );

        return Collections.unmodifiableCollection( result );
    }

    public Target getRepositoryTarget( String id )
    {
        List<CRepositoryTarget> targets = getCurrentConfiguration( false );

        for ( CRepositoryTarget target : targets )
        {
            if ( StringUtils.equals( id, target.getId() ) )
            {
                return convert( target );
            }
        }

        return null;
    }

    public boolean addRepositoryTarget( Target target )
        throws ConfigurationException
    {
        CRepositoryTarget cnf = convert( target );

        this.validate( cnf );

        removeRepositoryTarget( cnf.getId() );

        getCurrentConfiguration( true ).add( cnf );

        return true;
    }

    public boolean removeRepositoryTarget( String id )
    {
        List<CRepositoryTarget> targets = getCurrentConfiguration( true );

        for ( Iterator<CRepositoryTarget> ti = targets.iterator(); ti.hasNext(); )
        {
            CRepositoryTarget target = ti.next();

            if ( StringUtils.equals( id, target.getId() ) )
            {
                ti.remove();

                return true;
            }
        }

        return false;
    }

    public Set<Target> getTargetsForContentClass( ContentClass contentClass )
    {
        Set<Target> result = new HashSet<Target>();

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Resolving targets for contentClass='" + contentClass.getId() + "'" );
        }

        for ( Target t : getRepositoryTargets() )
        {
            if ( t.getContentClass().equals( contentClass ) )
            {
                result.add( t );
            }
        }

        return result;
    }

    public Set<Target> getTargetsForContentClassPath( ContentClass contentClass, String path )
    {
        Set<Target> result = new HashSet<Target>();

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "Resolving targets for contentClass='" + contentClass.getId() + "' for path='" + path + "'" );
        }

        for ( Target t : getRepositoryTargets() )
        {
            if ( t.isPathContained( contentClass, path ) )
            {
                result.add( t );
            }
        }

        return result;
    }

    public TargetSet getTargetsForRepositoryPath( Repository repository, String path )
    {
        TargetSet result = new TargetSet();

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Resolving targets for repository='" + repository.getId() + "' for path='" + path + "'" );
        }

        for ( Target t : getRepositoryTargets() )
        {
            if ( t.isPathContained( repository.getRepositoryContentClass(), path ) )
            {
                result.addTargetMatch( new TargetMatch( t, repository ) );
            }
        }

        return result;
    }

    public boolean hasAnyApplicableTarget( Repository repository )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Looking for any targets for repository='" + repository.getId() + "'" );
        }

        for ( Target t : getRepositoryTargets() )
        {
            if ( t.getContentClass().isCompatible( repository.getRepositoryContentClass() ) )
            {
                return true;
            }
        }

        return false;
    }

    public String getName()
    {
        return "Repository Target Configuration";
    }
}
