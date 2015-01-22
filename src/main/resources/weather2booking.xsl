<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" indent="yes"/>

    <xsl:param name="name"/>

    <xsl:template match="/">
        <booking>
            <name><xsl:value-of select="$name"/></name>
            <city><xsl:value-of select="/cities/list/item/city/@name"/></city>
            <temp><xsl:value-of select="/cities/list/item/temperature/@max"/></temp>
        </booking>
    </xsl:template>

</xsl:stylesheet>