<?xml version="1.0" encoding="UTF-8"?>
<!-- 
		Lists xslt parameters that have a description attribute (@dotify:desc).
		Roughly respects xslt include/import precedence.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
	xmlns:dotify="http://brailleapps.github.io/ns/dotify"
	exclude-result-prefixes="xs xd xsl dotify"
	version="2.0">
	<xsl:output indent="yes"  doctype-system="http://java.sun.com/dtd/properties.dtd" standalone="no"/>

	<xsl:template match="/">
		<properties>
			<xsl:variable name="rawlist">
				<xsl:apply-templates/>
			</xsl:variable>
			<xsl:for-each select="distinct-values($rawlist/*/@name)">
				<xsl:variable name="name" select="."/>
				<!-- inherit attributes/values for this element from ancestors -->
				<xsl:variable name="item">
					<!-- change context -->
					<xsl:for-each select="$rawlist/*[@name=$name][last()]">
						<xsl:copy>
							<!-- copy information from attributes in the whole list where the name matches -->
							<xsl:copy-of select="$rawlist/*[@name=$name]/@*"/>
							<!-- override with information from this element -->
							<xsl:copy-of select="@*"/>
						</xsl:copy>
					</xsl:for-each>
				</xsl:variable>
				<!-- transform this element into a result element -->
				<xsl:apply-templates select="$item/*" mode="asEntry"/>
			</xsl:for-each>
		</properties>
	</xsl:template>
	<xsl:template match="/*">
		<!-- The root element -->
		<xsl:apply-templates select="xsl:import"/>
		<xsl:apply-templates select="xsl:include"/>
		<xsl:apply-templates select="xsl:param"/>
	</xsl:template>
	<xsl:template match="xsl:import|xsl:include">
		<xsl:apply-templates select="document(@href)/node()"/>
	</xsl:template>
	<xsl:template match="xsl:param">
		<xsl:copy-of select="."/>
	</xsl:template>
	<xsl:template match="xsl:param" mode="asEntry">
		<xsl:if test="@dotify:desc">
			<!-- using tab as field separator, any tabs inside values will be converted to a regular space -->
			<entry key="{@name}"><xsl:value-of select="concat(normalize-space(@dotify:default), '&#0009;', normalize-space(@dotify:values), '&#0009;', normalize-space(@dotify:desc))"/></entry>
		</xsl:if>
	</xsl:template>
	<xsl:template match="node()">
		<xsl:apply-templates/>
	</xsl:template>
</xsl:stylesheet>