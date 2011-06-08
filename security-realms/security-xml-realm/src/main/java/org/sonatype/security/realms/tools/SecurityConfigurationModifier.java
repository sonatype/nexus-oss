package org.sonatype.security.realms.tools;

import org.sonatype.security.model.Configuration;

public interface SecurityConfigurationModifier
{

    boolean apply( Configuration configuration );

}
