package org.sonatype.nexus.ext.gwt.ui.client.data;

import java.util.List;

public interface RepresentationParser {

    Entity parseEntity(String representation, EntityFactory factory);

    Entity parseEntity(String representation, Entity entity);

    List<Entity> parseEntityList(String representation, EntityFactory factory);

    String serializeEntity(String root, Entity entity);

}
