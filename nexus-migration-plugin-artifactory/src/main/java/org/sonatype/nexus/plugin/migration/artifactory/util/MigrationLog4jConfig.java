package org.sonatype.nexus.plugin.migration.artifactory.util;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.sonatype.nexus.log.SimpleLog4jConfig;

public class MigrationLog4jConfig
    extends SimpleLog4jConfig
{

    private File migrationLog;

    public MigrationLog4jConfig( SimpleLog4jConfig logConfig, File migrationLog )
    {
        super( logConfig.getRootLogger(), logConfig.getFileAppenderLocation(), logConfig.getFileAppenderPattern() );
        this.migrationLog = migrationLog;
    }

    @Override
    public Map<String, String> toMap()
    {
        // super.toMap(); Do not change default values
        Map<String, String> configs = new LinkedHashMap<String, String>();

        configs.put( " key", "value " );
        configs.put( "log4j.logger.org.sonatype.nexus.plugin.migration", "DEBUG, migrationlogfile" );

        configs.put( "log4j.appender.migrationlogfile", "org.apache.log4j.DailyRollingFileAppender" );
        configs.put( "log4j.appender.migrationlogfile.File", migrationLog.getAbsolutePath() );
        configs.put( "log4j.appender.migrationlogfile.Append", "true" );
        configs.put( "log4j.appender.migrationlogfile.DatePattern", "'.'yyyy-MM-dd" );
        configs.put( "log4j.appender.migrationlogfile.layout", "org.sonatype.nexus.log4j.ConcisePatternLayout" );
        configs.put( "log4j.appender.migrationlogfile.layout.ConversionPattern", "%4d{yyyy-MM-dd HH:mm:ss} %-5p [%-15.15t] - %c - %m%n" );

        return configs;
    }

}
