package org.sonatype.nexus.bundle.launcher.internal;

import com.google.common.base.Preconditions;
import java.io.File;
import java.util.EnumMap;
import javax.inject.Inject;
import org.sonatype.nexus.bundle.launcher.ManagedNexusBundle;
import org.sonatype.nexus.bundle.launcher.NexusPort;

/**
 * Default implementation of {@link ManagedNexusBundle}
 * @author plynch
 */
public class DefaultManagedNexusBundle implements ManagedNexusBundle {
    private final String id;
    private final File nexusWorkDirectory;
    private final File nexusRuntimeDirectory;
    private final String host;
    private final String contextPath;
    private final String artifactCoordinates;
    private EnumMap<NexusPort, Integer> portMap;

    @Inject
    DefaultManagedNexusBundle(final String id, final String artifactCoordinates, final String host, EnumMap<NexusPort, Integer> portMap, final String contextPath, File nexusWorkDirectory, File nexusRuntimeDirectory) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(artifactCoordinates);
        Preconditions.checkNotNull(host);
        Preconditions.checkNotNull(portMap);
        Preconditions.checkNotNull(contextPath);
        Preconditions.checkNotNull(nexusWorkDirectory);
        Preconditions.checkNotNull(nexusRuntimeDirectory);
        this.id = id;
        this.artifactCoordinates = artifactCoordinates;
        this.host = host;
        if(!portMap.containsKey(NexusPort.HTTP)){
            throw new IllegalArgumentException("missing http port");
        }
        if(!"".equals(contextPath) && !contextPath.startsWith("/")){
            throw new IllegalArgumentException("Context path should be empty string or begin with a forward slash");
        }
        this.contextPath = contextPath;
        this.portMap = new EnumMap<NexusPort,Integer>(portMap);
        this.nexusWorkDirectory = nexusWorkDirectory;
        this.nexusRuntimeDirectory = nexusRuntimeDirectory;
    }

    @Override
    public int getHttpPort() {
        return portMap.get(NexusPort.HTTP);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getArtifactCordinates() {
        return artifactCoordinates;
    }

    @Override
    public int getPort(final NexusPort portType) {
        Integer port = portMap.get(portType);
        if(port == null){
            return -1;
        }
        return port;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public File getNexusWorkDirectory() {
        return nexusWorkDirectory;
    }

    @Override
    public File getNexusRuntimeDirectory() {
        return nexusRuntimeDirectory;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }


}
