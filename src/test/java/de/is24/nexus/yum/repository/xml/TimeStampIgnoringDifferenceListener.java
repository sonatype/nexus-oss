package de.is24.nexus.yum.repository.xml;

import static org.custommonkey.xmlunit.DifferenceConstants.ATTR_VALUE_ID;
import static org.custommonkey.xmlunit.DifferenceConstants.TEXT_VALUE_ID;
import org.apache.commons.lang.ArrayUtils;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.NodeDetail;
import org.w3c.dom.Node;


public class TimeStampIgnoringDifferenceListener implements DifferenceListener {
  public void skippedComparison(Node node1, Node node2) {
  }

  public int differenceFound(Difference difference) {
    if (difference.getTestNodeDetail().getXpathLocation().contains("/requires[") ||
        difference.getTestNodeDetail().getXpathLocation().contains("/provides[") ||
        difference.getTestNodeDetail().getXpathLocation().contains("/format[1]")) {
      return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
    }

    if (isNodeName(difference, "checksum", "timestamp", "packager", "open-checksum", "time@build", "time@file",
          "size@package", "summary",
          "description", "buildhost", "header-range@end")) {
      return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
    }

    if ((difference.getId() == TEXT_VALUE_ID) && hasSameText(difference)) {
      return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
    }

    return RETURN_ACCEPT_DIFFERENCE;
  }

  private boolean hasSameText(Difference difference) {
    if (difference.getControlNodeDetail().getValue() == null) {
      return difference.getTestNodeDetail().getValue() == null;
    }

    if (difference.getTestNodeDetail() == null) {
      return false;
    }

    return difference.getControlNodeDetail().getValue().trim().equals(difference.getTestNodeDetail().getValue().trim());
  }

  private boolean isNodeName(Difference difference, String... nodeNames) {
    if (difference.getId() == ATTR_VALUE_ID) {
      return isNodeName(attributeName(difference.getControlNodeDetail()), nodeNames) &&
        isNodeName(attributeName(difference.getTestNodeDetail()), nodeNames);
    }
    if (difference.getId() == TEXT_VALUE_ID) {
      return isNodeName(controlNode(difference).getParentNode().getLocalName(), nodeNames) &&
        isNodeName(testNode(difference).getParentNode().getLocalName(), nodeNames);
    }

    return isNodeName(controlNode(difference).getLocalName(), nodeNames) &&
      isNodeName(testNode(difference).getLocalName(), nodeNames);
  }

  private String attributeName(NodeDetail nodeDetail) {
    return nodeName(nodeDetail.getXpathLocation()) + "@" + nodeDetail.getNode().getLocalName();
  }

  private String nodeName(String attrXPathLocation) {
    return attrXPathLocation.replaceAll("^(.*\\/)([\\w\\-]+)(\\[\\d+\\]\\/@[\\w\\-]+)", "$2");
  }

  private Node testNode(Difference difference) {
    return difference.getTestNodeDetail().getNode();
  }

  private Node controlNode(Difference difference) {
    return difference.getControlNodeDetail().getNode();
  }

  private boolean isNodeName(String name, String... nodeNames) {
    return ArrayUtils.contains(nodeNames, name);
  }
}
