package org.sonatype.nexus.util.configurationreader;

import java.io.File;
import java.util.concurrent.locks.Lock;

import org.sonatype.configuration.upgrade.ConfigurationUpgrader;
import org.sonatype.nexus.configuration.validator.ConfigurationValidator;

@SuppressWarnings( "deprecation" )
public interface ConfigurationHelper
{
    public <E extends org.sonatype.configuration.Configuration> E load( E emptyInstance, String modelVersion,
                                                                        File configurationFile, Lock lock,
                                                                        ConfigurationReader<E> reader,
                                                                        ConfigurationValidator<E> validator,
                                                                        ConfigurationUpgrader<E> upgrader );

    public <E> void save( E configuration, File configurationFile,
                          ConfigurationWritter<E> configurationXpp3Writter, Lock lock );

}
