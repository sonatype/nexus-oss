<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/">
    <html>
      <body>
        <h2>IT index</h2>
        <table border="0">
          <tr bgcolor="#9acd32">
            <th>Index</th>
            <th>Class</th>
            <th>Method</th>
          </tr>
          <xsl:for-each select="index/test">
            <tr>
              <td>
                <a>
                  <xsl:attribute name="href">
                    <xsl:value-of select="index"/>
                  </xsl:attribute>
                  <xsl:value-of select="index"/>
                </a>
              </td>
              <td>
                <xsl:value-of select="className"/>
              </td>
              <td>
                <xsl:value-of select="methodName"/>
              </td>
            </tr>
          </xsl:for-each>
        </table>
      </body>
    </html>
  </xsl:template>

</xsl:stylesheet>