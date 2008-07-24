package org.sonatype.nexus.gwt.ui.client.form;

/**
 * 
 *
 * @author barath
 */
public class HiddenInput implements FormInput {
    
    private Object value;
    
    public HiddenInput(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        //do nothing
    }

    public void reset() {
        //do nothing
    }

}
