package org.sonatype.nexus.gwt.ui.client.form;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 *
 * @author barath
 */
public abstract class AbstractFormModel implements FormModel {

    protected Map inputs = new HashMap();
    
    public FormInput getInput(String name) {
        return (FormInput) inputs.get(name);
    }

    public void addInput(String name, FormInput input) {
        inputs.put(name, input);
    }

    public void removeInput(String name) {
        inputs.remove(name);
    }

    public void reset() {
        for (Iterator i = inputs.values().iterator(); i.hasNext();) {
            FormInput input = (FormInput) i.next();
            input.reset();
        }
    }

}
