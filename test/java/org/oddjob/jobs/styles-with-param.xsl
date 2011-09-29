<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

    <xsl:param name="text"/>
    
    <!-- the standard identity transformation -->
    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
   
   <xsl:template match="@text">
        <xsl:attribute name="text">
            <xsl:value-of select="$text"/>
        </xsl:attribute>
   </xsl:template>
   
</xsl:stylesheet>