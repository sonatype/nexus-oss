package org.sonatype.nexus.plugins.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Model;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Mirror;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.templates.TemplateManager;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;

@Component( role = MavenGate.class )
public class DefaultMavenGate
    implements MavenGate
{
    @Requirement
    private Logger logger;

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    @Requirement
    private TemplateManager templateManager;

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Requirement
    private NexusConfiguration nexusConfiguration;

    @Requirement
    private ProjectBuilder projectBuilder;

    @Requirement
    private RepositorySystem repositorySystem;

    protected Logger getLogger()
    {
        return logger;
    }

    public ProjectBuildingResult buildMavenProject( StorageFileItem pomItem, List<String> usedNexusRepositoryIds,
                                                    List<String> profileIds, Map<String, String> systemProperties,
                                                    Map<String, String> userProperties )
        throws ProjectBuildingException, IOException
    {
        ClassLoader old = Thread.currentThread().getContextClassLoader();

        // TODO: make it better :)
        String requestId = String.valueOf( System.currentTimeMillis() );

        File localRepository = getPerRequestLocalRepository( requestId );

        MavenGroupRepository targetGroup = null;

        try
        {
            // for Maven, we need to set TCCL explicitly to the realm where Maven Jars are
            // in this case, this is plugin's realm, hence the realm from where this class is loaded too
            Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

            Properties systemProps = new Properties();
            systemProps.putAll( System.getProperties() );
            if ( systemProperties != null )
            {
                systemProps.putAll( systemProperties );
            }

            Properties userProps = new Properties();
            if ( userProperties != null )
            {
                userProps.putAll( userProperties );
            }

            ProjectBuildingRequest config = new DefaultProjectBuildingRequest();

            config.setLocalRepository( repositorySystem.createArtifactRepository( "local", "file://"
                + localRepository.getAbsolutePath(), new DefaultRepositoryLayout(), null, null ) );

            ArrayList<ArtifactRepository> repositories = new ArrayList<ArtifactRepository>();

            if ( usedNexusRepositoryIds != null )
            {
                // user listed ones to be used
                for ( String nexusRepositoryId : usedNexusRepositoryIds )
                {
                    repositories.add( repositorySystem.createArtifactRepository( nexusRepositoryId, "nexus://"
                        + nexusRepositoryId, new DefaultRepositoryLayout(), null, null ) );
                }
            }
            else
            {
                // "nexus all"
                repositories.add( repositorySystem.createArtifactRepository( "nexus-all", "nexus://nexus-all",
                    new DefaultRepositoryLayout(), null, null ) );
            }

            config.setRemoteRepositories( repositories );

            if ( profileIds != null )
            {
                config.setActiveProfileIds( profileIds );
            }

            // create target group
            targetGroup = createRequestGroupRepository( requestId, usedNexusRepositoryIds );

            // to not let maven3 "wander off" to POM
            List<Mirror> mirrors = new ArrayList<Mirror>();
            Mirror mirror = new Mirror();
            mirror.setId( targetGroup.getId() );
            mirror.setMirrorOf( "*" );
            mirror.setUrl( "nexus://" + targetGroup.getId() );
            mirrors.add( mirror );
            config.setMirrors( mirrors );

            config.setSystemProperties( systemProps );
            config.setUserProperties( userProps );
            // nexus will handles caching
            config.setForceUpdate( true );
            // currently maven have problems to run in a child realm
            config.setProcessPlugins( false );

            return projectBuilder.build( new StorageFileItemModelSource( pomItem ), config );
        }
        catch ( ConfigurationException e )
        {
            throw new ProjectBuildingException( pomItem.getRepositoryItemUid().toString(),
                "Incompatible repository grouping to be used during Maven3 project building!", e );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ProjectBuildingException( pomItem.getRepositoryItemUid().toString(),
                "Nonexistent repository referenced to be used during Maven3 project building!", e );
        }
        finally
        {
            if ( old != null )
            {
                Thread.currentThread().setContextClassLoader( old );
            }

            if ( localRepository != null )
            {
                // delete local repository
                try
                {
                    FileUtils.forceDelete( localRepository );
                }
                catch ( IOException e )
                {
                    getLogger().warn(
                        "Cannot delete temporary local repository from path \"" + localRepository.getAbsolutePath()
                            + "\"!", e );
                }
            }

            if ( targetGroup != null )
            {
                // drop it
                try
                {
                    nexusConfiguration.deleteRepository( targetGroup.getId() );
                }
                catch ( NoSuchRepositoryException e )
                {
                    // nothing
                }
                catch ( ConfigurationException e )
                {
                    // nothing
                }

                nexusConfiguration.saveConfiguration();
            }
        }
    }

    public Model getEffectiveModel( StorageFileItem pomItem, List<String> usedNexusRepositoryIds,
                                    List<String> profileIds, Map<String, String> systemProperties,
                                    Map<String, String> userProperties )
        throws ProjectBuildingException, IOException
    {
        return buildMavenProject( pomItem, usedNexusRepositoryIds, profileIds, systemProperties, userProperties )
            .getProject().getModel();
    }

    // ==

    protected File getPerRequestLocalRepository( String requestId )
    {
        File tempLocalRepository =
            new File( applicationConfiguration.getTemporaryDirectory(), "m3-local-repository-" + requestId );

        return tempLocalRepository;
    }

    protected MavenGroupRepository createRequestGroupRepository( String requestId, List<String> usedNexusRepositoryIds )
        throws NoSuchRepositoryException, ConfigurationException, IOException
    {
        RepositoryTemplate template =
            (RepositoryTemplate) templateManager.getTemplates().getTemplates( Maven2ContentClass.class ).getTemplates(
                MavenGroupRepository.class ).pick();

        template.getConfigurableRepository().setId( "m3-exec-group-" + requestId );

        template.getConfigurableRepository().setName( "Temporary group for M3 request " + requestId );

        template.getConfigurableRepository().setExposed( false );

        template.getConfigurableRepository().setUserManaged( false );

        template.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );

        // we create an empty group
        MavenGroupRepository groupRepository = (MavenGroupRepository) template.create();

        if ( usedNexusRepositoryIds != null )
        {
            // user wanted ones
            for ( String memberId : usedNexusRepositoryIds )
            {
                groupRepository.addMemberRepositoryId( memberId );
            }
        }
        else
        {
            // "nexus all" -- actually all maven2 repositories defined in Maven (but only hosted proxy to avoid dupes)
            for ( MavenRepository mr : repositoryRegistry.getRepositoriesWithFacet( MavenRepository.class ) )
            {
                // XXX: for now, only maven2 layout plays
                if ( mr.getRepositoryContentClass().isCompatible( groupRepository.getRepositoryContentClass() )
                    && ( mr.getRepositoryKind().isFacetAvailable( MavenHostedRepository.class ) || mr
                        .getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) ) )
                {
                    groupRepository.addMemberRepositoryId( mr.getId() );
                }
            }
        }

        nexusConfiguration.saveConfiguration();

        return groupRepository;
    }

}
