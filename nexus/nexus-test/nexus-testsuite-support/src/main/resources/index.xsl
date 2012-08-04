<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:index="http://sonatype.com/xsd/nexus-testsuite-support/index/1.0">

  <xsl:template match="/">
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="index.css"/>
      </head>
      <body>
        <table class="pane">
          <tr>
            <td class="pane-header">Index</td>
            <td class="pane-header">Class</td>
            <td class="pane-header">Method</td>
            <td class="pane-header">Duration</td>
            <td class="pane-header">Info</td>
          </tr>
          <xsl:for-each select="index:index/index:test">
            <tr>
              <td class="pane right">
                <a>
                  <xsl:attribute name="href">
                    <xsl:value-of select="index"/>
                  </xsl:attribute>
                  <xsl:value-of select="index"/>
                </a>
              </td>
              <td>
                <xsl:attribute name="class">
                  pane success-<xsl:value-of select="success"/>
                </xsl:attribute>
                <xsl:if test="throwableMessage">
                  <xsl:attribute name="title">
                    <xsl:value-of select="throwableMessage"/>
                  </xsl:attribute>
                </xsl:if>
                <xsl:value-of select="className"/>
              </td>
              <td class="pane">
                <xsl:value-of select="methodName"/>
              </td>
              <td class="pane">
                <xsl:value-of select="duration"/> sec
              </td>
              <td class="pane">
                <table>
                  <xsl:for-each select="index:testInfo">
                    <tr>
                      <td>
                        <xsl:value-of select="key"/>:
                      </td>
                      <xsl:if test="link='true'">
                        <td>
                          <a>
                            <xsl:attribute name="href">
                              <xsl:value-of select="value"/>
                            </xsl:attribute>
                            <xsl:value-of select="value"/>
                          </a>
                        </td>
                      </xsl:if>
                      <xsl:if test="link='false'">
                        <td>
                          <xsl:value-of select="value"/>
                        </td>
                      </xsl:if>
                    </tr>
                  </xsl:for-each>
                </table>
              </td>
            </tr>
          </xsl:for-each>
        </table>
      </body>
    </html>
  </xsl:template>

</xsl:stylesheet>