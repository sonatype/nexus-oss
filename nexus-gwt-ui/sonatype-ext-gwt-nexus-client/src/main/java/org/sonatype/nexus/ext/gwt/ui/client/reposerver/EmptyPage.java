package org.sonatype.nexus.ext.gwt.ui.client.reposerver;

import org.sonatype.nexus.ext.gwt.ui.client.ServerFunctionPanel;
import org.sonatype.nexus.ext.gwt.ui.client.ServerInstance;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;

public class EmptyPage  extends LayoutContainer implements ServerFunctionPanel {

    public void init(ServerInstance server) {
        setLayout(new BorderLayout());
        setWidth("100%");
        setHeight("100%");
        add(new Text("Not implemented yet"));
    }

}
