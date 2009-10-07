package org.sonatype.nexus.plugins;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.repository.RequestProcessor;

@Component( role = MockComponent.class )
public class MockComponent
{
    @Requirement( role = RepositoryCustomizer.class )
    private Map<String, RepositoryCustomizer> customizers;

    @Requirement( role = RequestProcessor.class )
    private Map<String, RequestProcessor> processors;

    public Map<String, RepositoryCustomizer> getCustomizers()
    {
        return customizers;
    }

    public Map<String, RequestProcessor> getProcessors()
    {
        return processors;
    }
}
