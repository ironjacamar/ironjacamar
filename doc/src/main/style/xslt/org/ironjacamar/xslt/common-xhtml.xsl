<?xml version="1.0"?>
<!--
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:d="http://docbook.org/ns/docbook">

    <xsl:import href="common-base.xsl"/>

    <xsl:param name="siteHref" select="'http://www.ironjacamar.org/'"/>
    <xsl:param name="docHref" select="'http://www.ironjacamar.org/documentation.html'"/>
    <xsl:param name="siteLinkText" select="'www.ironjacamar.org'"/>

    <xsl:param name="legalnotice.filename">legalnotice.html</xsl:param>

    <xsl:template match="d:legalnotice" mode="chunk-filename">
        <xsl:value-of select="$legalnotice.filename"/>
    </xsl:template>

    <xsl:template name="user.footer.content">
        <hr/>
        <a>
            <xsl:attribute name="href">
                <xsl:value-of select="$legalnotice.filename"/>
            </xsl:attribute>
            <xsl:choose>
                <xsl:when test="//d:book/d:bookinfo/d:copyright[1]">
                    <xsl:apply-templates select="//d:book/d:bookinfo/d:copyright[1]" mode="titlepage.mode"/>
                </xsl:when>
                <xsl:when test="//d:legalnotice[1]">
                    <xsl:apply-templates select="//d:legalnotice[1]" mode="titlepage.mode"/>
                </xsl:when>
            </xsl:choose>
        </a>
    </xsl:template>

</xsl:stylesheet>
