package org.sonatype.nexus.ext.gwt.ui.client.data;

import com.extjs.gxt.ui.client.data.ModelData;

public interface Entity extends ModelData {
    
    String getType();
    
    Class getFieldType(String fieldName);
    
    Entity createEntity(String fieldName);

}
