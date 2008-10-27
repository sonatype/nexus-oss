package org.sonatype.nexus.gwt.ui.client.data;

/**
 *
 * @author barath
 */
public interface ResourceParser {
    
    Object getValue(Object obj, String path);
    
    void setValue(Object obj, String path, Object value);

}
