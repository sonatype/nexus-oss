package org.sonatype.nexus.proxy.router;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.registry.ContentClass;

@Component( role = RepositoryRouter.class, hint = "groups-m2" )
public class TestM2GroupIdBasedRouter
    extends GroupIdBasedRepositoryRouter
{

    public static final String ID = "groups-m2";

    @Requirement( hint = "maven2" )
    private ContentClass contentClass;

    public ContentClass getHandledContentClass()
    {
        return contentClass;
    }

    public String getId()
    {
        return ID;
    }

}
