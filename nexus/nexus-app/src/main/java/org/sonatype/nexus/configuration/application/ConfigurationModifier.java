package org.sonatype.nexus.configuration.application;

import org.sonatype.nexus.configuration.model.Configuration;

public interface ConfigurationModifier
{

    boolean apply( Configuration configuration );

}
