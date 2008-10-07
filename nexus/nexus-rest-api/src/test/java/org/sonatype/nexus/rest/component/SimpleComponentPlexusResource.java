package org.sonatype.nexus.rest.component;

/**
 * Allows testing of any Role.  Actual implementations should only expose a single role
 */
public class SimpleComponentPlexusResource extends AbstractComponentPlexusResource
{

    @Override
    public String getResourceUri()
    {
        return "/components/{" + ROLE_ID + "}";
    }

}
