package org.sonatype.nexus.proxy.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.sonatype.nexus.configuration.ConfigurationPrepareForSaveEvent;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AbstractGroupRepository}
 *
 * @since 2.2
 */
public class AbstractGroupRepositoryTest
    extends TestSupport
{

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    private Repository repo;

    @Mock
    private Event basicEvent;

    @Mock
    private RepositoryRegistryEventRemove removeEvent;

    @Mock
    private ConfigurationPrepareForSaveEvent prepSaveEvent;

    @Mock
    private CRepository cRepo;

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    private AbstractGroupRepositoryConfiguration extConfig;

    @Spy
    private AbstractGroupRepository groupRepo = new AbstractGroupRepository()
        {
            @Override
            protected Configurator getConfigurator()
            {
                return null;
            }

            @Override
            protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
            {
                return null;
            }

            @Override
            public RepositoryKind getRepositoryKind()
            {
                return null;
            }

            @Override
            public ContentClass getRepositoryContentClass()
            {
                return null;
            }
        };

    @Test
    public void onEventNullEventShouldNotNPE()
    {
        groupRepo.onEvent( null );
    }

    @Test
    public void onEventNullExternalConfigurationShouldNotNPE()
        throws Exception
    {
        doReturn( null ).when( groupRepo ).getExternalConfiguration( anyBoolean() );
        groupRepo.onEvent( basicEvent );
        groupRepo.onEvent( removeEvent );
        groupRepo.onEvent( prepSaveEvent );
    }

    @Test
    public void onEventNullCurrentConfigurationShouldNotNPE()
    {
        doReturn( null ).when( groupRepo ).getCurrentConfiguration( anyBoolean() );
        groupRepo.onEvent( basicEvent );
        groupRepo.onEvent( removeEvent );
        groupRepo.onEvent( prepSaveEvent );
    }

    @Test
    public void onEventCurrentConfigurationShouldNotNPE()
    {
        doReturn( repo ).when( removeEvent ).getRepository(); // not expected to return null
        doReturn( extConfig ).when( groupRepo ).getExternalConfiguration( anyBoolean() );
        doReturn( cRepo ).when( groupRepo ).getCurrentConfiguration( anyBoolean() );
        groupRepo.onEvent( basicEvent );
        groupRepo.onEvent( removeEvent );
        groupRepo.onEvent( prepSaveEvent );

    }








}
