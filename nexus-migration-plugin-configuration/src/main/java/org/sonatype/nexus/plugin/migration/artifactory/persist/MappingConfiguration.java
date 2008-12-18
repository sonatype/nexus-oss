package org.sonatype.nexus.plugin.migration.artifactory.persist;

import org.sonatype.nexus.plugin.migration.artifactory.persist.model.CMapping;

public interface MappingConfiguration
{

    void save();

    void addMapping(CMapping map);

}
