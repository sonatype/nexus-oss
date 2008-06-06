package org.sonatype.nexus.ext.gwt.ui.client;

public abstract class Action<D> {
    
    private String caption;
    
    private boolean enabled;
    
    public Action() {
    }
    
    public Action(String caption) {
        this.caption = caption;
    }
    
    public String getCaption() {
        return caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;
    }

    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }
    
    public boolean supports(D data) {
        return true;
    }

    public abstract void execute(D data);
    
}
