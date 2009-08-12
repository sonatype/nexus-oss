package org.sonatype.nexus.plugin.migration.artifactory.util;

import java.io.File;

import org.sonatype.nexus.log.SimpleLog4jConfig;

public class MigrationLog4jConfig
    extends SimpleLog4jConfig
{

    private static final long serialVersionUID = -4169205016852207375L;

    public MigrationLog4jConfig( SimpleLog4jConfig logConfig, File migrationLog )
    {
        super( logConfig.getRootLogger(), logConfig.getFileAppenderLocation(), logConfig.getFileAppenderPattern() );

        put( "log4j.logger.org.sonatype.nexus.plugin.migration", "DEBUG, migrationlogfile" );

        put( "log4j.appender.migrationlogfile", "org.apache.log4j.DailyRollingFileAppender" );
        put( "log4j.appender.migrationlogfile.File", migrationLog.getAbsolutePath().replace( '\\', '/' ) );
        put( "log4j.appender.migrationlogfile.Append", "true" );
        put( "log4j.appender.migrationlogfile.DatePattern", "'.'yyyy-MM-dd" );
        put( "log4j.appender.migrationlogfile.layout", "org.sonatype.nexus.log4j.ConcisePatternLayout" );
        put( "log4j.appender.migrationlogfile.layout.ConversionPattern",
             "%4d{yyyy-MM-dd HH:mm:ss} %-5p [%-15.15t] - %c - %m%n" );
    }

}
