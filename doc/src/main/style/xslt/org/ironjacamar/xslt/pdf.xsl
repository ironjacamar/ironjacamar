<?xml version='1.0'?>
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns="http://www.w3.org/TR/xhtml1/transitional"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                exclude-result-prefixes="#default">

   <xsl:import href="classpath:/xslt/org/jboss/pressgang/pdf.xsl"/>
   <xsl:import href="common-base.xsl"/>
   <xsl:import href="fop1.xsl" />

    <xsl:import href="classpath:/xslt/org/jboss/xslt/fonts/pdf/fonts.xsl" />

    <!-- work around problems in the jboss.org styles wrt pdf & jhighlight -->
    <xsl:param name="programlisting.font" select="'monospace,fixed'" />
    <xsl:param name="programlisting.font.size" select="'75%'" />

	<xsl:param name="title.font.family">
		<xsl:variable name="font">
			<xsl:call-template name="pickfont-sans"/>
		</xsl:variable>
        <xsl:message>
            <xsl:text>Setting 'title.font.family' param =</xsl:text><xsl:copy-of select="$font"/>
        </xsl:message>
		<xsl:copy-of select="$font"/>
	</xsl:param>

	<xsl:param name="body.font.family">
		<xsl:variable name="font">
			<xsl:call-template name="pickfont-sans"/>
		</xsl:variable>
        <xsl:message>
            <xsl:text>Setting 'body.font.family' param =</xsl:text><xsl:copy-of select="$font"/>
        </xsl:message>
		<xsl:copy-of select="$font"/>
	</xsl:param>

	<xsl:param name="monospace.font.family">
		<xsl:variable name="font">
		    <xsl:call-template name="pickfont-mono"/>
		</xsl:variable>
        <xsl:message>
            <xsl:text>Setting 'monospace.font.family' param =</xsl:text><xsl:copy-of select="$font"/>
        </xsl:message>
		<xsl:copy-of select="$font"/>
	</xsl:param>

	<xsl:param name="sans.font.family">
		<xsl:variable name="font">
			<xsl:call-template name="pickfont-sans"/>
		</xsl:variable>
        <xsl:message>
            <xsl:text>Setting 'sans.font.family' param =</xsl:text><xsl:copy-of select="$font"/>
        </xsl:message>
		<xsl:copy-of select="$font"/>
	</xsl:param>

   <!-- Change the font color for titles to Hibernate.org one -->
   <xsl:param name="title.color">#4a5d75</xsl:param>
   <xsl:param name="titlepage.color">#4a5d75</xsl:param>
   <xsl:param name="chapter.title.color">#4a5d75</xsl:param>
   <xsl:param name="section.title.color">#4a5d75</xsl:param>

   <!-- Style tables to look like SeamFramework.org
   <xsl:param name="table.cell.border.color">#D3D2D1</xsl:param>
   <xsl:param name="table.frame.border.color">#D3D2D1</xsl:param>
   <xsl:param name="table.cell.border.thickness">0.6pt</xsl:param>

   <xsl:param name="table.frame.border.thickness">0.6pt</xsl:param>
   <xsl:param name="table.cell.border.right.color">white</xsl:param>
   <xsl:param name="table.cell.border.left.color">#D3D2D1</xsl:param>
   <xsl:param name="table.frame.border.right.color">white</xsl:param>
   <xsl:param name="table.frame.border.left.color">white</xsl:param>
-->

   <xsl:template name="table.cell.block.properties">
      <!-- highlight this entry? -->
      <xsl:if test="ancestor::thead or ancestor::tfoot">
         <xsl:attribute name="font-weight">bold</xsl:attribute>
         <xsl:attribute name="background-color">#EDE8DB</xsl:attribute>
         <xsl:attribute name="color">black</xsl:attribute>
      </xsl:if>
   </xsl:template>

   <!--
      From: fo/table.xsl
      Reason: Table Header format
      Version:1.72
   -->
   <!-- customize this template to add row properties -->
   <xsl:template name="table.row.properties">
      <xsl:variable name="bgcolor">
         <xsl:call-template name="dbfo-attribute">
            <xsl:with-param name="pis" select="processing-instruction('dbfo')" />
            <xsl:with-param name="attribute" select="'bgcolor'" />
         </xsl:call-template>
      </xsl:variable>
      <xsl:if test="$bgcolor != ''">
         <xsl:attribute name="background-color">
      <xsl:value-of select="$bgcolor" />
    </xsl:attribute>
      </xsl:if>
      <xsl:if test="ancestor::thead or ancestor::tfoot">
         <xsl:attribute name="background-color">#EDE8DB</xsl:attribute>
      </xsl:if>
   </xsl:template>

    <!--########## Custom Title Page -->
    <xsl:template name="book.titlepage.recto">
        <fo:block>
            <fo:table table-layout="fixed" width="150mm">
                <fo:table-column column-width="150mm"/>
                <fo:table-body>
                    <fo:table-row>
                        <fo:table-cell text-align="center">
                            <xsl:if test="bookinfo/mediaobject">
                                <fo:block>
                                    <fo:external-graphic>
                                        <xsl:attribute name="src">
        <xsl:if test="$img.src.path != ''">
            <xsl:value-of select="$img.src.path"/>
        </xsl:if>
                                            <xsl:value-of
                          select="bookinfo/mediaobject/imageobject/imagedata/@fileref" />
                                        </xsl:attribute>    
                                    </fo:external-graphic>
                                </fo:block>
                            </xsl:if>
                            <xsl:if test="bookinfo/title">
                                <fo:block font-family="Helvetica" font-size="22pt" padding-before="10mm">
                                    <xsl:value-of select="bookinfo/title"/>
                                </fo:block>
                            </xsl:if>
                            <xsl:if test="bookinfo/subtitle">
                                <fo:block font-family="Helvetica" font-size="18pt" padding-before="10mm">
                                    <xsl:value-of select="bookinfo/subtitle"/>
                                </fo:block>
                            </xsl:if>
                            <xsl:if test="bookinfo/releaseinfo">
                                <fo:block font-family="Helvetica" font-size="12pt"
                                    padding="10mm"><xsl:value-of select="bookinfo/releaseinfo"/>
                                </fo:block>
                            </xsl:if>
                            <xsl:if test="bookinfo/copyright">
                                <fo:block font-family="Helvetica" font-size="12pt"
                                    padding="10mm">                                    
                                  
                                    <xsl:apply-templates select="bookinfo/copyright"
                                                         mode="titlepage.mode"/>
                                </fo:block>
                            </xsl:if>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block>
    </xsl:template>

   <!-- preface charter font -->
   <xsl:attribute-set name="preface.titlepage.recto.style">
	<xsl:attribute name="font-family">
		<xsl:value-of select="$title.fontset"/>
	</xsl:attribute>
	<xsl:attribute name="color">#4a5d75</xsl:attribute>
	<xsl:attribute name="font-size">24pt</xsl:attribute>
	<xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="text-align">right</xsl:attribute>
   </xsl:attribute-set>

   <!-- charter title -->
   <xsl:attribute-set name="chapter.titlepage.recto.style">
	<xsl:attribute name="color"><xsl:value-of select="$chapter.title.color"/></xsl:attribute>
	<xsl:attribute name="background-color">white</xsl:attribute>
	<xsl:attribute name="font-size">
		<xsl:choose>
			<xsl:when test="$l10n.gentext.language = 'ja-JP'">
				<xsl:value-of select="$body.font.master * 1.7"/>
				<xsl:text>pt</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>24pt</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:attribute>
	<xsl:attribute name="font-weight">bold</xsl:attribute>
	<xsl:attribute name="text-align">right</xsl:attribute>
	<!--xsl:attribute name="wrap-option">no-wrap</xsl:attribute-->
	<xsl:attribute name="padding-left">1em</xsl:attribute>
	<xsl:attribute name="padding-right">1em</xsl:attribute>
    </xsl:attribute-set>

    <xsl:template name="chapter.titlepage.before.recto">
        <xsl:param name="node" select="ancestor-or-self::chapter[1]"/> 
        <fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format"
            text-align="right"
            font-size="72pt" font-weight="bold" color="#4a5d75">
            <xsl:number from="book" format="1"/>
        </fo:block>
    </xsl:template>
    <xsl:template match="title" mode="chapter.titlepage.recto.auto.mode">
        <xsl:variable name="titleabbrev">
            <xsl:apply-templates select="ancestor-or-self::chapter[1]"
                mode="titleabbrev.markup"/>
        </xsl:variable>

        <fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format"
            xsl:use-attribute-sets="chapter.titlepage.recto.style">
            <xsl:value-of select="$titleabbrev" />
        </fo:block>
    </xsl:template>

  <!--
  From: fo/pagesetup.xsl
  Reason: Custom Header
  Version: 1.76.1
  -->
  <xsl:template name="header.content">
    <xsl:param name="pageclass" select="''"/>
    <xsl:param name="sequence" select="''"/>
    <xsl:param name="position" select="''"/>
    <xsl:param name="gentext-key" select="''"/>
    <xsl:param name="title-limit" select="'30'"/>
    <!--
      <fo:block>
        <xsl:value-of select="$pageclass"/>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="$sequence"/>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="$position"/>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="$gentext-key"/>
      </fo:block>
    body, blank, left, chapter
    -->
    <!-- sequence can be odd, even, first, blank -->
    <!-- position can be left, center, right -->
    <xsl:choose>
      <!--xsl:when test="($sequence='blank' and $position='left' and $gentext-key='chapter')">
      <xsl:variable name="text">
        <xsl:call-template name="component.title.nomarkup"/>
      </xsl:variable>
        <fo:inline keep-together.within-line="always" font-weight="bold">
          <xsl:choose>
          <xsl:when test="string-length($text) &gt; '33'">
          <xsl:value-of select="concat(substring($text, 0, $title-limit), '...')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$text"/>
        </xsl:otherwise>
        </xsl:choose>
      </fo:inline>
      </xsl:when-->
      <xsl:when test="$confidential = 1 and (($sequence='odd' and $position='left') or ($sequence='even' and $position='right'))">
        <fo:inline keep-together.within-line="always" font-weight="bold">
          <xsl:text>RED HAT CONFIDENTIAL</xsl:text>
        </fo:inline>
      </xsl:when>
      <xsl:when test="$sequence = 'blank'">
        <!-- nothing -->
      </xsl:when>
      <!-- Extracting 'Chapter' + Chapter Number from the full Chapter title, with a dirty, dirty hack -->
      <xsl:when test="($sequence='first' and $position='left' and $gentext-key='chapter')">
        <xsl:variable name="text">
          <xsl:call-template name="component.title.nomarkup"/>
        </xsl:variable>
        <xsl:variable name="chapt">
          <xsl:value-of select="substring-before($text, '&#xA0;')"/>
        </xsl:variable>
        <xsl:variable name="remainder">
          <xsl:value-of select="substring-after($text, '&#xA0;')"/>
        </xsl:variable>
        <xsl:variable name="chapt-num">
          <xsl:value-of select="substring-before($remainder, '&#xA0;')"/>
        </xsl:variable>
        <xsl:variable name="text1">
          <xsl:value-of select="concat($chapt, '&#xA0;', $chapt-num)"/>
        </xsl:variable>
        <fo:inline keep-together.within-line="always" font-weight="bold">
          <xsl:value-of select="$text1"/>
        </fo:inline>
      </xsl:when>
      <!--xsl:when test="($sequence='odd' or $sequence='even') and $position='center'"-->
      <xsl:when test="($sequence='even' and $position='left')">
        <!--xsl:if test="$pageclass != 'titlepage'"-->
        <xsl:variable name="text">
          <xsl:call-template name="component.title.nomarkup"/>
        </xsl:variable>
        <fo:inline keep-together.within-line="always" font-weight="bold">
          <xsl:choose>
            <xsl:when test="string-length($text) &gt; '33'">
              <xsl:value-of select="concat(substring($text, 0, $title-limit), '...')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$text"/>
            </xsl:otherwise>
          </xsl:choose>
        </fo:inline>
        <!--xsl:if-->
      </xsl:when>
      <xsl:when test="($sequence='odd' and $position='right')">
        <!--xsl:if test="$pageclass != 'titlepage'"-->
        <fo:inline keep-together.within-line="always">
          <fo:retrieve-marker retrieve-class-name="section.head.marker" retrieve-position="first-including-carryover" retrieve-boundary="page-sequence"/>
        </fo:inline>
        <!--/xsl:if-->
      </xsl:when>
      <xsl:when test="$position='left'">
        <!-- Same for odd, even, empty, and blank sequences -->
        <xsl:call-template name="draft.text"/>
      </xsl:when>
      <xsl:when test="$position='center'">
        <!-- nothing for empty and blank sequences -->
      </xsl:when>
      <xsl:when test="$position='right'">
        <!-- Same for odd, even, empty, and blank sequences -->
        <xsl:call-template name="draft.text"/>
      </xsl:when>
      <xsl:when test="$sequence = 'first'">
        <!-- nothing for first pages -->
      </xsl:when>
      <xsl:when test="$sequence = 'blank'">
        <!-- nothing for blank pages -->
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="component.title.nomarkup">
    <xsl:param name="node" select="."/>

    <xsl:variable name="id">
      <xsl:call-template name="object.id">
        <xsl:with-param name="object" select="$node"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="title">
      <xsl:apply-templates select="$node" mode="object.title.markup">
        <xsl:with-param name="allow-anchors" select="1"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:copy-of select="$title"/>
  </xsl:template>

</xsl:stylesheet>

