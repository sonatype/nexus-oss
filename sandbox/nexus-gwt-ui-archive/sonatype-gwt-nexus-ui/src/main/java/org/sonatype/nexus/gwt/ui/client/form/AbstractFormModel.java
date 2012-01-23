/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
