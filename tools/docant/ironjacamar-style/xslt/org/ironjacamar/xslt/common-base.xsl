<?xml version="1.0"?>

<!--
  ~ Hibernate, Relational Persistence for Idiomatic Java
  ~
  ~ Copyright (c) 2008-2012, Red Hat Inc. or third-party contributors as
  ~ indicated by the @author tags or express copyright attribution
  ~ statements applied by the authors.  All third-party contributions are
  ~ distributed under license by Red Hat Inc.
  ~
  ~ This copyrighted material is made available to anyone wishing to use, modify,
  ~ copy, or redistribute it subject to the terms and conditions of the GNU
  ~ Lesser General Public License, as published by the Free Software Foundation.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  ~ or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  ~ for more details.
  ~
  ~ You should have received a copy of the GNU Lesser General Public License
  ~ along with this distribution; if not, write to:
  ~ Free Software Foundation, Inc.
  ~ 51 Franklin Street, Fifth Floor
  ~ Boston, MA  02110-1301  USA
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <!-- set this to avoid xslt failing of pressgang style 3.0.0    -->
    <xsl:param name="use.extensions">0</xsl:param>

    <!-- Admonitions -->
    <xsl:param name="admon.graphics" select="1"/>
    <xsl:param name="admon.graphics.path">
        <!-- AFAICT, this only works with the PDF xslt because the html ones use css to style admon graphics -->
        <xsl:if test="$img.src.path != ''">
            <xsl:value-of select="$img.src.path"/>
        </xsl:if>
        <xsl:text>images/community/docbook/</xsl:text>
    </xsl:param>
  
</xsl:stylesheet>

