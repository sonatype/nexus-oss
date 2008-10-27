package org.sonatype.nexus.gwt.ui.client.form;

/**
 * 
 *
 * @author barath
 */
public interface FormInput {
    
    Object getValue();
    
    void setValue(Object value);
    
    void reset();

}
