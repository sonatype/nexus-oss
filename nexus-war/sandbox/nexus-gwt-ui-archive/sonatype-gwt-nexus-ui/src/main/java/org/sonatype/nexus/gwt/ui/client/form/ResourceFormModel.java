package org.sonatype.nexus.gwt.ui.client.form;

import java.util.Iterator;
import java.util.Map;

import org.sonatype.nexus.gwt.ui.client.data.ResourceParser;

import com.google.gwt.user.client.Window;

/**
 *
 * @author barath
 */
public class ResourceFormModel extends AbstractFormModel {
    
    private ResourceParser parser;

    private Object resource;
    
    public ResourceFormModel(ResourceParser parser) {
        this.parser = parser;
    }

    public Object getFormData() {
        String name = null;
        try {
            for (Iterator i = inputs.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                name = (String) entry.getKey();
                FormInput input = (FormInput) entry.getValue();
                parser.setValue(resource, name, input.getValue());
            }
            return resource;
        } catch (Exception e) {
            e.printStackTrace();
            Window.alert("Wrong value in '" + name + "' field!");
        }
        return null;
    }

    public void setFormData(Object formData) {
        this.resource = formData;
        for (Iterator i = inputs.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            FormInput input = (FormInput) entry.getValue();
            Object obj = parser.getValue(resource, (String) entry.getKey());
            input.setValue(obj);
        }
    }

}
