package org.sonatype.nexus.gwt.ui.client.form;

/**
 * 
 *
 * @author barath
 */
public interface FormModel {
    
    FormInput getInput(String name);
    
    void addInput(String name, FormInput input);
    
    void removeInput(String name);
    
    Object getFormData();

    void setFormData(Object formData);
    
    void reset();
    
}
