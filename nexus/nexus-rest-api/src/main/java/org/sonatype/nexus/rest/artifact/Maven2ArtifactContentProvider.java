package org.sonatype.nexus.rest.artifact;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.rest.ArtifactViewProvider;

@Component(role = ArtifactViewProvider.class, hint = "maven2")
public class Maven2ArtifactContentProvider implements ArtifactViewProvider
{

    public Object retrieveView( ResourceStoreRequest storeRequest )
    {
        return "stub for maven2 view";
    }

}
