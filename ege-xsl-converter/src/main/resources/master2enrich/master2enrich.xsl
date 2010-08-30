<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns="http://www.tei-c.org/ns/1.0"
  exclude-result-prefixes="tei">

  <!-- 

Version 0.81-2009-02-24
  This script has been developed as part of the ENRICH project: http://enrich.manuscriptorium.com 
    and is available from the OUCS website at http://tei.oucs.ox.ac.uk/ENRICH/  ... to use it you should 
    prepare a valid MASTER file with a single msDescription element.  This should be run with something like:
    
    saxon -warnings:silent -o outputfile.xml inputfile.xml master2enrich.xsl 
  
  The warnings produced are simply ambiguous rule matches caused by importing a 
  default p4top5.xsl, which we haven't (yet) gone through and removed all duplicate rules.
  
  If you want advice or support in using this script, please contact enrich@oucs.ox.ac.uk
  
  This script is licensed under a Creative Commons Attribution license.  If you use it please 
  cite enrich@oucs.ox.ac.uk as the author and point to http://tei.oucs.ox.ac.uk/ENRICH/
  -->

  <xsl:output indent="yes" method="xml"/>

  <!-- First part is paste of p4top5.xsl which has then modified, but does result in the abiguous rule match warnings.  -->

  <xsl:variable name="processor">
    <xsl:value-of select="system-property('xsl:vendor')"/>
  </xsl:variable>

  <!-- <xsl:variable name="today">
    <xsl:choose>
      <xsl:when test="function-available('edate:date-time')">
	<xsl:value-of select="edate:date-time()"/>
      </xsl:when>
      <xsl:when test="contains($processor,'SAXON')">
	<xsl:value-of select="Date:toString(Date:new())"
		      xmlns:Date="/java.util.Date"/>
      </xsl:when>
      <xsl:otherwise>0000-00-00</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>-->

  <xsl:variable name="uc">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
  <xsl:variable name="lc">abcdefghijklmnopqrstuvwxyz</xsl:variable>

  <xsl:template match="*" priority="-1">
    <xsl:choose>
      <xsl:when test="namespace-uri()=''">
        <xsl:element namespace="http://www.tei-c.org/ns/1.0" name="{local-name(.)}">
          <xsl:apply-templates select="@*|*|processing-instruction()|comment()|text()"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|*|processing-instruction()|comment()|text()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template match="@*|processing-instruction()|comment()" priority="-2">
    <xsl:copy/>
  </xsl:template>


  <xsl:template match="text()">
    <xsl:value-of select="."/>
  </xsl:template>


  <!-- change of name, or replaced by another element -->
  <xsl:template match="teiCorpus.2">
    <teiCorpus>
      <xsl:apply-templates select="*|@*|processing-instruction()|comment()|text()"/>
    </teiCorpus>
  </xsl:template>

  <xsl:template match="witness/@sigil">
    <xsl:attribute name="xml:id">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="witList">
    <listWit>
      <xsl:apply-templates select="*|@*|processing-instruction()|comment()|text()"/>
    </listWit>
  </xsl:template>


  <!-- Vanish TEI.2, we are only outputting teiHeaders,  -->
  <xsl:template match="TEI.2">
    <xsl:apply-templates select="*|@*|processing-instruction()|comment()|text()"/>
  </xsl:template>

  <xsl:template match="xref">
    <xsl:element namespace="http://www.tei-c.org/ns/1.0" name="ref">
      <xsl:apply-templates select="*|@*|processing-instruction()|comment()|text()"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="xptr">
    <xsl:element namespace="http://www.tei-c.org/ns/1.0" name="ptr">
      <xsl:apply-templates select="*|@*|processing-instruction()|comment()|text()"/>
    </xsl:element>
  </xsl:template>


  <xsl:template match="figure[@url]">
    <figure>
      <graphic>
        <xsl:copy-of select="@*"/>
      </graphic>
      <xsl:apply-templates/>
    </figure>
  </xsl:template>


  <xsl:template match="figure/@url"/>

  <xsl:template match="figure/@entity"/>

  <xsl:template match="figure[@entity]">
    <figure>
      <graphic url="{unparsed-entity-uri(@entity)}">
        <xsl:apply-templates select="@*"/>
      </graphic>
      <xsl:apply-templates/>
    </figure>
  </xsl:template>

  <xsl:template match="event">
    <incident>
      <xsl:apply-templates select="@*|*|text()|comment()|processing-instruction()"/>
    </incident>
  </xsl:template>

  <xsl:template match="state">
    <refState>
      <xsl:apply-templates select="@*|*|text()|comment()|processing-instruction()"/>
    </refState>
  </xsl:template>


  <!-- lost elements -->
  <xsl:template match="dateRange">
    <date>
      <xsl:apply-templates select="*|@*|processing-instruction()|comment()|text()"/>
    </date>
  </xsl:template>


  <xsl:template match="dateRange/@from">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="dateRange/@to">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="language">
    <xsl:element namespace="http://www.tei-c.org/ns/1.0" name="language">
      <xsl:if test="@id">
        <xsl:attribute name="ident">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="*|processing-instruction()|comment()|text()"/>
    </xsl:element>
  </xsl:template>

  <!-- attributes lost -->
  <!-- dropped from TEI. Added as new change records later -->
  <xsl:template match="@date.created"/>

  <xsl:template match="@date.updated"/>

  <!-- dropped from TEI. No replacement -->
  <xsl:template match="refsDecl/@doctype"/>

  <!-- attributes changed name -->

  <xsl:template match="date/@value">
    <xsl:attribute name="when">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>


  <xsl:template match="@url">
    <xsl:attribute name="target">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>


  <xsl:template match="@doc">
    <xsl:attribute name="target">
      <xsl:value-of select="unparsed-entity-uri(.)"/>
    </xsl:attribute>
  </xsl:template>


  <xsl:template match="@id">
    <xsl:choose>
      <xsl:when test="parent::lang">
        <xsl:attribute name="ident">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="xml:id">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template match="@lang">
    <xsl:attribute name="xml:lang">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>


  <xsl:template match="change/@date"/>

  <xsl:template match="date/@certainty">
    <xsl:attribute name="cert">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

  <!-- all pointing attributes preceded by # -->

  <xsl:template match="variantEncoding/@location">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template
    match="@ana|@active|@adj|@adjFrom|@adjTo|@children|@children|@class|@code|@code|@copyOf|@corresp|@decls|@domains|@end|@exclude|@fVal|@feats|@follow|@from|@hand|@inst|@langKey|@location|@mergedin|@new|@next|@old|@origin|@otherLangs|@parent|@passive|@perf|@prev|@render|@resp|@sameAs|@scheme|@script|@select|@since|@start|@synch|@target|@targetEnd|@to|@to|@value|@value|@who|@wit">
    <xsl:attribute name="{name(.)}">
      <xsl:call-template name="splitter">
        <xsl:with-param name="val">
          <xsl:value-of select="."/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:attribute>
  </xsl:template>


  <xsl:template name="splitter">
    <xsl:param name="val"/>
    <xsl:choose>
      <xsl:when test="contains($val,' ')">
        <xsl:text>#</xsl:text>
        <xsl:value-of select="substring-before($val,' ')"/>
        <xsl:text> </xsl:text>
        <xsl:call-template name="splitter">
          <xsl:with-param name="val">
            <xsl:value-of select="substring-after($val,' ')"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>#</xsl:text>
        <xsl:value-of select="$val"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- fool around with selected elements -->


  <!-- imprint is no longer allowed inside bibl -->
  <xsl:template match="bibl/imprint">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="editionStmt/editor">
    <respStmt>
      <resp>
        <xsl:value-of select="@role"/>
      </resp>
      <name>
        <xsl:apply-templates/>
      </name>
    </respStmt>
  </xsl:template>



  <!-- Vanish text element -->
  <xsl:template match="text"/>


  <!-- header -->

  <xsl:template match="teiHeader">
    <xsl:processing-instruction name="oxygen">RNGSchema="../../../ODD/RomaResults/enrich.rng"
      type="xml"</xsl:processing-instruction>
    <teiHeader>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()"/>

      <xsl:if test="not(revisionDesc) and (@date.created or @date.updated)">
        <revisionDesc>
          <xsl:if test="@date.updated">
            <change>> <label>updated</label>
              <date>
                <xsl:value-of select="@date.updated"/>
              </date>
              <label>Date edited</label>
            </change>
          </xsl:if>
          <xsl:if test="@date.created">
            <change>
              <label>created</label>
              <date>
                <xsl:value-of select="@date.created"/>
              </date>
              <label>Date created</label>
            </change>
          </xsl:if>
        </revisionDesc>
      </xsl:if>

    </teiHeader>
  </xsl:template>

  <xsl:template match="revisionDesc">
    <revisionDesc>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()"/>
    </revisionDesc>
  </xsl:template>

  <xsl:template match="publicationStmt">
    <publicationStmt>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()"/>
    </publicationStmt>
  </xsl:template>

  <!-- space does not have @extent any more -->
  <xsl:template match="space/@extent">
    <xsl:attribute name="quantity">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

  <!-- tagsDecl has a compulsory namespace child now -->
  <xsl:template match="tagsDecl">
    <xsl:if test="*">
      <tagsDecl>
        <namespace name="http://www.tei-c.org/ns/1.0">
          <xsl:apply-templates select="*|comment()|processing-instruction"/>
        </namespace>
      </tagsDecl>
    </xsl:if>
  </xsl:template>

  <!-- orgTitle inside orgName? redundant -->
  <xsl:template match="orgName/orgTitle">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- no need for empty <p> in sourceDesc -->
  <xsl:template match="sourceDesc/p[string-length(.)=0]"/>

  <!-- start creating the new choice element -->
  <xsl:template match="corr[@sic]">
    <choice>
      <corr>
        <xsl:value-of select="text()"/>
      </corr>
      <sic>
        <xsl:value-of select="@sic"/>
      </sic>
    </choice>
  </xsl:template>

  <xsl:template match="gap/@desc">
    <desc>
      <xsl:value-of select="."/>
    </desc>
  </xsl:template>

  <xsl:template match="sic[@corr]">
    <choice>
      <sic>
        <xsl:apply-templates/>
      </sic>
      <corr>
        <xsl:value-of select="@corr"/>
      </corr>
    </choice>
  </xsl:template>

  <xsl:template match="abbr[@expan]">
    <choice>
      <abbr>
        <xsl:apply-templates/>
      </abbr>
      <expan>
        <xsl:value-of select="@expan"/>
      </expan>
    </choice>
  </xsl:template>

  <xsl:template match="expan[@abbr]">
    <choice>
      <expan>
        <xsl:apply-templates/>
      </expan>
      <abbr>
        <xsl:value-of select="@abbr"/>
      </abbr>
    </choice>
  </xsl:template>

  <!-- special consideration for <change> element -->
  <xsl:template match="change">
    <change>

      <xsl:apply-templates select="date"/>

      <xsl:if test="respStmt/resp">
        <label>
          <xsl:value-of select="respStmt/resp/text()"/>
        </label>
      </xsl:if>
      <xsl:for-each select="respStmt/name">
        <name type="person">
          <xsl:apply-templates select="@*|*|comment()|processing-instruction()|text()"/>
        </name>
      </xsl:for-each>
      <xsl:for-each select="item">
        <xsl:apply-templates select="@*|*|comment()|processing-instruction()|text()"/>
      </xsl:for-each>
    </change>
  </xsl:template>


  <xsl:template match="respStmt[resp]">
    <respStmt>
      <xsl:choose>
        <xsl:when test="resp/name">
          <resp>
            <xsl:value-of select="resp/text()"/>
          </resp>
          <xsl:for-each select="resp/name">
            <name type="person">
              <xsl:apply-templates/>
            </name>
          </xsl:for-each>
        </xsl:when>
        <xsl:when test="resp and name">
          <resp>
            <xsl:value-of select="resp"/>
          </resp>
          <xsl:for-each select="name">
            <name type="person">
              <xsl:apply-templates/>
            </name>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates/>
          <name>
            <xsl:comment>Conversion added blank name element</xsl:comment>
          </name>
        </xsl:otherwise>
      </xsl:choose>
    </respStmt>
  </xsl:template>

  <xsl:template match="q/@direct"/>

  <xsl:template match="q">
    <!-- q deleted in ENRICH -->
    <quote>
      <xsl:apply-templates select="@*|*|comment()|processing-instruction()|text()"/>
    </quote>
  </xsl:template>


  <!-- if we are reading the P4 with a DTD,
       we need to avoid copying the default values
       of attributes -->

  <xsl:template match="@targOrder">
    <xsl:if test="not(translate(.,$uc,$lc) ='u')">
      <xsl:attribute name="targOrder">
        <xsl:value-of select="."/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>


  <xsl:template match="@opt">
    <xsl:if test="not(translate(.,$uc,$lc) ='n')">
      <xsl:attribute name="opt">
        <xsl:value-of select="."/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>


  <xsl:template match="@to">
    <xsl:if test="not(translate(.,$uc,$lc) ='ditto')">
      <xsl:attribute name="to">
        <xsl:value-of select="."/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>


  <xsl:template match="@default">
    <xsl:choose>
      <xsl:when test="translate(.,$uc,$lc)= 'no'"/>
      <xsl:otherwise>
        <xsl:attribute name="default">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template match="@part">
    <xsl:if test="not(translate(.,$uc,$lc) ='n')">
      <xsl:attribute name="part">
        <xsl:value-of select="."/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>


  <xsl:template match="@full">
    <xsl:if test="not(translate(.,$uc,$lc) ='yes')">
      <xsl:attribute name="full">
        <xsl:value-of select="."/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>


  <xsl:template match="@from">
    <xsl:if test="not(translate(.,$uc,$lc) ='root')">
      <xsl:attribute name="from">
        <xsl:value-of select="."/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>


  <xsl:template match="@status">
    <xsl:choose>
      <xsl:when test="parent::teiHeader">
        <xsl:if test="not(translate(.,$uc,$lc) ='new')">
          <xsl:attribute name="status">
            <xsl:value-of select="."/>
          </xsl:attribute>
        </xsl:if>
      </xsl:when>
      <xsl:when test="parent::del">
        <xsl:if test="not(translate(.,$uc,$lc) ='unremarkable')">
          <xsl:attribute name="status">
            <xsl:value-of select="."/>
          </xsl:attribute>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="status">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template match="@place">
    <xsl:if test="not(translate(.,$uc,$lc) ='unspecified')">
      <xsl:attribute name="place">
        <xsl:value-of select="."/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>


  <xsl:template match="@sample">
    <xsl:if test="not(translate(.,$uc,$lc) ='complete')">
      <xsl:attribute name="sample">
        <xsl:value-of select="."/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>


  <xsl:template match="@org">
    <xsl:if test="not(translate(.,$uc,$lc) ='uniform')">
      <xsl:attribute name="org">
        <xsl:value-of select="."/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template match="teiHeader/@type">
    <xsl:if test="not(translate(.,$uc,$lc) ='text')">
      <xsl:attribute name="type">
        <xsl:value-of select="."/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <!-- yes|no to boolean -->

  <xsl:template match="@anchored">
    <xsl:attribute name="anchored">
      <xsl:choose>
        <xsl:when test="translate(.,$uc,$lc)='yes'">true</xsl:when>
        <xsl:when test="translate(.,$uc,$lc)='no'">false</xsl:when>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="sourceDesc/@default"/>

  <xsl:template match="@tei">
    <xsl:attribute name="tei">
      <xsl:choose>
        <xsl:when test="translate(.,$uc,$lc)='yes'">true</xsl:when>
        <xsl:when test="translate(.,$uc,$lc)='no'">false</xsl:when>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="@langKey"/>

  <xsl:template match="@TEIform"/>

  <!-- assorted atts -->
  <xsl:template match="@old"/>

  <xsl:template match="@mergedin">
    <xsl:attribute name="mergedIn">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

  <!-- deal with the loss of div0 -->

  <xsl:template match="div1|div2|div3|div4|div5|div6">
    <xsl:variable name="divName">
      <xsl:choose>
        <xsl:when test="ancestor::div0">
          <xsl:text>div</xsl:text>
          <xsl:value-of select="number(substring-after(local-name(.),'div')) + 1"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="local-name()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:element name="{$divName}" namespace="http://www.tei-c.org/ns/1.0">
      <xsl:apply-templates select="*|@*|processing-instruction()|comment()|text()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="div0">
    <div1>
      <xsl:apply-templates select="*|@*|processing-instruction()|comment()|text()"/>
    </div1>
  </xsl:template>

  <!-- from Conal Tuohy -->
  <xsl:template match="orig[@reg]">
    <choice>
      <orig>
        <xsl:apply-templates select="*|@*|processing-instruction()|
    comment()|text()"/>
      </orig>
      <reg>
        <xsl:value-of select="@reg"/>
      </reg>
    </choice>
  </xsl:template>

  <xsl:template match="reg[@orig]">
    <choice>
      <reg>
        <xsl:apply-templates select="*|@*|processing-instruction()|
    comment()|text()"/>
      </reg>
      <orig>
        <xsl:value-of select="@orig"/>
      </orig>
    </choice>
  </xsl:template>

  <xsl:template match="@orig|@reg"/>


  <xsl:param name="debug" select="'false'"/>
  <xsl:variable name="debugMsg" select="$debug"/>


  <!-- If in doubt, copy it. -->
  <xsl:template match="@*|node()|comment()|processing-instruction()" priority="-100">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|comment()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>

  <!-- msDescription to msDesc -->
  <xsl:template match="msDescription">
    <msDesc>
      <xsl:if test="not(@id)">
        <xsl:attribute name="xml:id">
          <xsl:choose>
            <xsl:when test="msIdentifier/idno">
              <xsl:value-of
                select="concat('msDesc-',translate(normalize-space(msIdentifier/idno), ' /(),','__'))"
              />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="generate-id()"/>
            </xsl:otherwise>
          </xsl:choose>


        </xsl:attribute>
      </xsl:if>
      <xsl:if test="not(@lang)">
        <xsl:attribute name="xml:lang">en</xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="msIdentifier"/>
      <xsl:apply-templates select="msHeading"/>
      <xsl:apply-templates select="msContents"/>
      <xsl:apply-templates select="physDesc"/>
      <xsl:apply-templates select="history"/>
      <xsl:apply-templates select="additional"/>
      <xsl:apply-templates select="msPart"/>
    </msDesc>
  </xsl:template>

  <xsl:template match="@id">
    <xsl:attribute name="xml:id">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>


  <!-- sex change -->
  <xsl:template match="attribute::sex">
    <xsl:variable name="sex">
      <xsl:choose>
        <xsl:when test=". = 'M' or 'm'">1</xsl:when>
        <xsl:when test=". = 'F' or 'f'">2</xsl:when>
        <xsl:when test=". = 'U' or 'u'">0</xsl:when>
        <xsl:otherwise>9</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:attribute name="sex">
      <xsl:value-of select="$sex"/>
    </xsl:attribute>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">@sex changed from <xsl:value-of select="."/>
        to <xsl:value-of select="$sex"/></xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>

  <!-- MASTER DTD default attributes -->
  <xsl:template
    match="	binding/@contemporary[.='unk'] |
	cell/@cols[.='1'] |
	cell/@rows[.='1'] |
	del/@status[.='unremarkable'] |
	delSpan/@status[.='unremarkable'] |
	dimensions/@units[.='mm'] |
	height/@units[.='mm'] | 
	depth/@units[.='mm'] | 
	width/@units[.='mm'] |
	editor/@role[.='editor'] |
	layout/@columns[.='1'] |
	l/@part[.='N'] |
	lg/@part[.='N'] |
	div/@part[.='N'] |
	decoNote/@figurative[.='na'] |
	decoNote/@illustrative[.='u'] |
	lg/@sample[.='complete'] |
	div/@sample[.='complete'] |
	lg/@org[.='uniform'] | 
	div/@org[.='uniform'] |
	list/@type[.='simple'] |
	msContents/@defective[.='no'] |
	msItem/@defective[.='no'] | 
	incipit/@defective[.='no'] |
	explicit/@defective[.='no'] |
	msDescription/@status |
	name/@type[.='person'] |
	note/@place[.='unspecified'] |
	note/@anchored[.='yes'] |
	origdate/@evidence[.='external'] | 
	birth/@evidence[.='external'] | 
	death/@evidence[.='external'] | 
	residence/@evidence[.='external'] | 
	occupation/@evidence[.='external'] |
	binding/@evidence[.='external'] | 
	origin/@evidence[.='external'] | 
	provenance/@evidence[.='external'] | 
	acquisition/@evidence[.='external'] | 
	custEvent/@evidence[.='external'] |
	placeName/@full[.='yes'] |
	ptr/@targOrder[.='U']  |
	ref/@targOrder[.='U']  |
	q/@direct[.='unspecified'] |
	row/@role[.='data'] |
	cell/@role[.='data'] |
	settlement/@full[.='yes'] | 
	region/@full[.='yes'] | 
	country/@full[.='yes'] |
	sourceDesc/@default[.='NO'] |
	bibl/@default[.='NO'] |
	listBibl/@default[.='NO'] | 
	projectDesc/@default[.='NO'] | 
	samplingDecl/@default[.='NO'] |
	editorialDecl/@default[.='NO'] | 
	stdVals/@default[.='NO'] | 
	langUsage/@default[.='NO'] | 
	textClass/@default[.='NO'] | 
	listPerson/@default[.='NO'] |
	teiHeader/@status[.='new'] |
	titlePart/@type[.='main'] |
	author/@accepted |
	author/@attested
	"/>


  <xsl:template match="profileDesc/listPerson"/>


  <xsl:template match="sourceDesc/p">
    <xsl:comment> bibl/note kludge used to allow mixed structured (msDesc) and unstructured (p)
      information in sourceDesc </xsl:comment>
    <bibl>
      <note>
        <p>
          <xsl:apply-templates/>
        </p>
      </note>
    </bibl>
  </xsl:template>

  <xsl:template match="sourceDesc">
    <sourceDesc>
      <xsl:apply-templates select="//msDescription[1]"/>
      <xsl:apply-templates select="*[not(name()='msDescription')]"/>
      <!-- listPerson moved from profileDesc to sourceDesc -->
      <xsl:if test="ancestor::teiHeader/profileDesc/listPerson/person">
        <listPerson type="converted">
          <xsl:apply-templates select="ancestor::teiHeader/profileDesc/listPerson/node()"/>
        </listPerson>
      </xsl:if>
    </sourceDesc>
  </xsl:template>



  <xsl:template match="date/@certainty[.='conjecture']" priority="20">
    <xsl:attribute name="cert">low</xsl:attribute>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">date/@certainty="conjecture" changed to
        @cert="low"</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>

  <xsl:template match="attribute::certainty" priority="10">
    <xsl:attribute name="cert">
      <xsl:choose>
        <xsl:when test=". = 'high' or 'medium' or 'low' or
        'unknown'">
          <xsl:value-of select="."/>
        </xsl:when>
        <xsl:when test=".='conjecture'">low</xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
    <!-- 
    <xsl:call-template name="message">
      <xsl:with-param name="reason">@certainty changed to @cert</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>

  <!-- Vanish these. -->
  <xsl:template
    match="binding/@certainty | binding/@evidence
		| custEvent/@certainty
		| custEvent/@evidence
		|provenance/@evidence 
		| acquisition/@evidence
		| acquisition/@certainty
		"
    priority="20"
    ><!--
    <xsl:call-template name="dumpAttrMessage">
      <xsl:with-param name="reason">Not in P5</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>


  <!-- I don't like either of these, but this is at least valid if
	semantically nonsense -->
  <xsl:template match="expan[@type='damage']">
    <damage type="expan">
      <xsl:apply-templates/>
    </damage>

  </xsl:template>



  <xsl:template match="person">
    <person>
      <xsl:if test="not(@sex)">
        <xsl:attribute name="sex">0</xsl:attribute>
        <!--<xsl:call-template name="message">
          <xsl:with-param name="reason">ENRICH mandatory @sex added to person
            element as default sex="0"</xsl:with-param>
        </xsl:call-template>-->
      </xsl:if>
      <xsl:apply-templates select="@*|node()|comment()|processing-instruction()"/>
    </person>
  </xsl:template>

  <!-- msHeading to head -->
  <xsl:template match="msHeading">
    <head type="msHeading">
      <xsl:apply-templates/>
    </head>
  </xsl:template>


  <!-- msHeading/textLang to just head/lang -->
  <xsl:template match="msHeading/textLang">
    <seg type="textLang">
      <xsl:if test="@langKey">
        <xsl:choose>
          <xsl:when test="@otherLangs">
      <xsl:attribute name="subtype">
        <xsl:variable name="otherLangs"><xsl:value-of select="translate(@otherLangs, ' ', '_')"/></xsl:variable>
        <xsl:variable name="subtype"><xsl:value-of select="concat(@langKey,'-',$otherLangs)"/></xsl:variable>
        <xsl:value-of select="$subtype"/>
      </xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="subtype">
        <xsl:value-of select="@langKey"/>
              </xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
        </xsl:if>
      <xsl:apply-templates/>
    </seg>
  </xsl:template>



  <!-- msHeading/author -->
  <xsl:template match="msHeading/author">
    <persName type="author">
      <xsl:apply-templates/>
    </persName>
  </xsl:template>


  <xsl:template match="region">
    <region>
      <xsl:choose>
        <xsl:when test="@type = ('compass' or 'county' or 'geo' or 'parish' or state)"/>
        <xsl:otherwise>
          <xsl:attribute name="type">unknown</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
    </region>
  </xsl:template>

  <!-- physDesc -->

  <xsl:template match="physDesc">
    <physDesc>
      <xsl:choose>
        <xsl:when test="p">
          <xsl:apply-templates/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:if test="form | support |extent | collation |foliation |condition">
            <objectDesc>
              <xsl:choose>
                <xsl:when test="contains(translate(form, $uc, $lc), 'codex')">
                  <xsl:attribute name="form">codex</xsl:attribute>
                </xsl:when>
                <xsl:when test="contains(translate(form, $uc, $lc), 'leaf')">
                  <xsl:attribute name="form">leaf</xsl:attribute>
                </xsl:when>
                <xsl:when test="contains(translate(form, $uc, $lc), 'scroll')">
                  <xsl:attribute name="form">scroll</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:attribute name="form">other</xsl:attribute>
                  <xsl:if test="normalize-space(@form) != ''">
                    <xsl:variable name="conv">
                      <xsl:value-of
                        select="concat(name(), '/@form = &quot;',@form, '&quot; now &quot;other&quot;')"
                      />
                    </xsl:variable>
                    <xsl:message>
                      <xsl:value-of select="$conv"/>
                    </xsl:message>
                  </xsl:if>
                </xsl:otherwise>
              </xsl:choose>

              <xsl:if test=" support |extent | collation |foliation |condition">
                <supportDesc>
                  <xsl:choose>
                    <xsl:when test="contains(translate(support, $uc, $lc), 'mix')">
                      <xsl:attribute name="material">mixed</xsl:attribute>
                    </xsl:when>
                    <xsl:when test="contains(translate(support, $uc, $lc), 'paper')">
                      <xsl:attribute name="material">chart</xsl:attribute>
                    </xsl:when>
                    <xsl:when test="contains(translate(support, $uc, $lc), 'chart')">
                      <xsl:attribute name="material">chart</xsl:attribute>
                    </xsl:when>
                    <xsl:when test="contains(translate(support, $uc, $lc), 'parch')">
                      <xsl:attribute name="material">perg</xsl:attribute>
                    </xsl:when>
                    <xsl:when test="contains(translate(support, $uc, $lc), 'perg')">
                      <xsl:attribute name="material">perg</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:attribute name="material">unknown</xsl:attribute>
                      <xsl:if test="normalize-space(@material) != ''">
                        <xsl:variable name="conv">
                          <xsl:value-of
                            select="concat(name(), '/@support = &quot;',@support, '&quot; now &quot;unknown&quot;')"
                          />
                        </xsl:variable>
                        <xsl:message>
                          <xsl:value-of select="$conv"/>
                        </xsl:message>
                      </xsl:if>
                    </xsl:otherwise>
                  </xsl:choose>
                  <xsl:apply-templates select="support"/>
                  <xsl:apply-templates select="extent"/>
                  <xsl:apply-templates select="foliation"/>
                  <xsl:apply-templates select="collation"/>
                  <xsl:apply-templates select="condition"/>
                </supportDesc>
                <!--
                <xsl:call-template name="message">
                  <xsl:with-param name="reason">supportDesc
                  created</xsl:with-param>
                </xsl:call-template>-->
              </xsl:if>
              <xsl:if test="layout">
                <layoutDesc>
                  <xsl:apply-templates select="layout"/>
                </layoutDesc>
                <!--
                <xsl:call-template name="message">
                  <xsl:with-param name="reason">layoutDesc
                  created</xsl:with-param>
                </xsl:call-template>-->
              </xsl:if>
            </objectDesc>
          </xsl:if>

          <xsl:apply-templates select="msWriting"/>

          <xsl:apply-templates select="musicNotation"/>

          <xsl:apply-templates select="decoration"/>

          <xsl:apply-templates select="additions"/>

          <xsl:apply-templates select="bindingDesc"/>


          <xsl:if test="../additional/accMat">
            <accMat>
              <xsl:apply-templates select="../additional/accMat/node()"/>
            </accMat>
            <!--
            <xsl:call-template name="message">
              <xsl:with-param name="reason">accMat moved from additional to
                physDesc</xsl:with-param>
            </xsl:call-template>-->
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </physDesc>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">physDesc reorganised to P5
      order</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>

  <!-- cancel accMat -->
  <xsl:template match="accMat"/>

  <!-- support might have paragraphs in but maybe only one -->
  <xsl:template match="support">
    <xsl:element name="{name()}">
      <xsl:choose>
        <xsl:when test="count(child::p) &gt; 1">
          <xsl:apply-templates/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="p/node()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>

  <xsl:template match="foliation">
    <foliation>
      <xsl:apply-templates select="@*|node()"/>
    </foliation>
  </xsl:template>

  <xsl:template match="p">
    <p>
      <xsl:apply-templates select="@*|node()"/>
    </p>
  </xsl:template>


  <!-- @units to @unit -->
  <xsl:template match="@units">
    <xsl:attribute name="unit">
      <xsl:value-of select="."/>
    </xsl:attribute>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason"><xsl:value-of select="name(parent::node())"
        />/@units changed to @unit</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>

  <!-- @contemporary -->
  <xsl:template match="binding/@contemporary">
    <xsl:attribute name="contemporary">
      <xsl:choose>
        <xsl:when test=".='unk'">unknown</xsl:when>
        <xsl:when test=".='no'">false</xsl:when>
        <xsl:when test=".='yes'">true</xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">binding/@contemporary changed to proper
        boolean values</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>

  <!-- msWriting to handDesc -->

  <xsl:template match="msWriting">
    <handDesc>
      <xsl:apply-templates/>
    </handDesc>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">msWriting changed to
      handDesc</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>



  <!-- 
		
		msWriting followed by p and several handDescs.  It should be
		handDesc with several handNotes inside.. should I change the p to a
		handNote, can't have both.
		
	-->

  <!-- handDesc to handNote-->

  <!-- vanish paragraphs for now while using ab. -->
  <xsl:template match="msWriting/handDesc/p">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="msWriting[handDesc]/p">
    <summary>
      <xsl:apply-templates/>
    </summary>
  </xsl:template>



  <xsl:template match="msWriting[handDesc]/p/bibl">
    <note>
      <bibl>
        <xsl:apply-templates/>
      </bibl>
    </note>
  </xsl:template>


  <xsl:template match="msWriting[not(handDesc)]/p">
    <p>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <xsl:template match="handDesc">
    <handNote>
      <xsl:apply-templates select="@*[name() !='medium' or name() !='scribe' or name() != 'script']"/>
      <xsl:choose>
        <xsl:when test="@scope">
          <xsl:attribute name="scope">
            <xsl:value-of select="@scope"/>
          </xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="scope">sole</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="@medium">
        <xsl:attribute name="medium">
          <xsl:value-of select="translate(@medium, '   ', '__')"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="not(@scribe='') and @scribe">
        <xsl:attribute name="scribe">
          <xsl:value-of select="translate(@scribe, ' \/,[].', '_-..')"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:choose>
        <xsl:when
          test="@script= 'carolmin' or @script= 'textualis' or @script= 'cursiva' or @script= 'hybrida' or @script= 'humbook' or script='humcursiva' or @script='other' ">
          <xsl:attribute name="script">
            <xsl:value-of select="@script"/>
          </xsl:attribute>
        </xsl:when>
        <xsl:when
          test="contains(translate(@script, $uc, $lc), 'goth') or contains(translate(@script, $uc, $lc), 'text')">
          <xsl:attribute name="script">textualis</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@script, $uc, $lc), 'carol')">
          <xsl:attribute name="script">carolmin</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@script, $uc, $lc), 'hyb')">
          <xsl:attribute name="script">hybrida</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@script, $uc, $lc), 'bastar')">
          <xsl:attribute name="script">hybrida</xsl:attribute>
        </xsl:when>
        <xsl:when
          test="contains(translate(@script, $uc, $lc), 'hum') and contains(translate(@script, $uc, $lc), 'book')">
          <xsl:attribute name="script">humbook</xsl:attribute>
        </xsl:when>
        <xsl:when
          test="contains(translate(@script, $uc, $lc), 'hand') and contains(translate(@script, $uc, $lc), 'book')">
          <xsl:attribute name="script">humbook</xsl:attribute>
        </xsl:when>
        <xsl:when
          test="contains(translate(@script, $uc, $lc), 'hum') and contains(translate(@script, $uc, $lc), 'cursiv')">
          <xsl:attribute name="script">humbook</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@script, $uc, $lc), 'cursiv' )">
          <xsl:attribute name="script">cursiva</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@script, $uc, $lc), 'unk' )">
          <xsl:attribute name="script">unknown</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="script">other</xsl:attribute>
          <xsl:if test="normalize-space(@script) != ''">
            <xsl:variable name="conv">
              <xsl:value-of
                select="concat(name(), '/@script = &quot;',@script, '&quot; now &quot;other&quot;')"
              />
            </xsl:variable>
            <xsl:message>
              <xsl:value-of select="$conv"/>
            </xsl:message>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates/>
    </handNote>

  </xsl:template>

  <xsl:template match="handDesc/@*"/>


  <!-- decoration to decoDesc -->
  <xsl:template match="decoration">
    <decoDesc>
      <xsl:apply-templates/>
    </decoDesc>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">decoration changed to
      decoDesc</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>

  <!-- decoDesc to decoNote-->
  <xsl:template match="decoDesc | decoNote">
    <decoNote>
      <!-- <xsl:apply-templates select="@*[not(name()='type')]"/>-->
      <xsl:choose>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'border')">
          <xsl:attribute name="type">border</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'diagram')">
          <xsl:attribute name="type">diagram</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'initial')">
          <xsl:attribute name="type">initial</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'marg')">
          <xsl:attribute name="type">marginal</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'map')">
          <xsl:attribute name="type">map</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'min')">
          <xsl:attribute name="type">miniature</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'mix')">
          <xsl:attribute name="type">mixed</xsl:attribute>
        </xsl:when>
        <xsl:when
          test="contains(translate(@type, $uc, $lc), 'para') or contains(translate(@type, $uc, $lc), 'rubr')">
          <xsl:attribute name="type">paratext</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'second')">
          <xsl:attribute name="type">secondary</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'illus')">
          <xsl:attribute name="type">illustration</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'printmark')">
          <xsl:attribute name="type">printmark</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'pub')">
          <xsl:attribute name="type">publishmark</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'vign')">
          <xsl:attribute name="type">vignette</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'frieze')">
          <xsl:attribute name="type">friezer</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'other;')">
          <xsl:attribute name="type">other</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">other</xsl:attribute>
          <xsl:if test="normalize-space(@type) !=''">
            <xsl:variable name="conv">
              <xsl:value-of
                select="concat(name(), '/@type = &quot;',@type, '&quot; now &quot;other&quot;')"
              />
            </xsl:variable>
            <xsl:message>
              <xsl:value-of select="$conv"/>
            </xsl:message>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates/>
    </decoNote>
    <!--<xsl:call-template name="message">
      <xsl:with-param name="reason">decoDesc changed to
      decoNote</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>

  <xsl:template match="altName">
    <msName>
      <xsl:apply-templates/>
    </msName>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">altName changed to msName</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>


  <!-- Don't copy extent -->
  <xsl:template match="gap/@extent"/>

  <xsl:template match="gap">

    <xsl:element name="gap">

      <xsl:choose>
        <!-- Maybe be more careful about this test?  -->
        <xsl:when test="@reason='irrelevant' or @reason='illegible' or reason='cancelled'">
          <xsl:attribute name="reason">
            <xsl:value-of select="@reason"/>
          </xsl:attribute>
        </xsl:when>
        <!-- This default is semantically icky. -->
        <xsl:otherwise>
          <xsl:attribute name="reason">irrelevant</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>

      <!-- Test if @extent contains a space, if it does, does it contain
        more than one?  If so, make it a desc, if not split it into @unit
        and @extent, otherwise, keep it how it is. -->
      <xsl:variable name="test">
        <xsl:choose>
          <xsl:when test="contains(@extent, ' ')">
            <xsl:choose>
              <xsl:when test="contains(substring-after(@extent, ' '), ' ')">desc</xsl:when>
              <xsl:otherwise>split</xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>keep</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:if test="@extent">
        <xsl:choose>
          <xsl:when test="$test='desc'">
            <xsl:element name="desc">
              <xsl:value-of select="@extent"/>
            </xsl:element>
          </xsl:when>
          <xsl:when test="$test='keep'"/>
          <xsl:when test="$test='split'">
            <xsl:variable name="unit">
              <xsl:value-of select="normalize-space(substring-after(@extent, ' '))"/>
            </xsl:variable>
            <xsl:attribute name="extent">
              <xsl:value-of select="normalize-space(substring-before(@extent, ' '))"/>
            </xsl:attribute>
            <!--
          <xsl:call-template name="message">
            <xsl:with-param name="reason">gap/@extent experimentally split to
              @extent and @unit</xsl:with-param>
          </xsl:call-template>-->
          </xsl:when>
          <xsl:otherwise>
            <xsl:message>Error in processing gap/@extent... value dumped in child 'desc'
              element.</xsl:message>
            <xsl:element name="desc">
              <xsl:value-of select="@extent"/>
            </xsl:element>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>

      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>


  <!-- translate if layout/@writtenLines etc.contains a hyphen -->
  <xsl:template match="layout/@writtenLines |layout/@columns
	|layout/@ruledLines">
    <xsl:choose>
      <xsl:when test=".=''"/>
      <xsl:when test="contains(., '-') or contains(., '/')">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="translate(.,
		'-/', '  ')"/>
        </xsl:attribute>
        <!--
        <xsl:call-template name="message">
          <xsl:with-param name="reason">'-' or '/' removed from <xsl:value-of
              select="concat(name(parent::node()), '/@', name())"
          /></xsl:with-param>
        </xsl:call-template>-->
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- don't copy add/@place -->
  <xsl:template match="add/@place"/>

  <xsl:template match="add[@place]" priority="10">
    <add>
      <xsl:choose>
        <xsl:when
          test="starts-with(@place, '1')  or starts-with(@place, '2')  or starts-with(@place, '3')  or starts-with(@place, '4')  or starts-with(@place, '5')  or starts-with(@place, '6')  or starts-with(@place, '7')  or starts-with(@place, '8')  or starts-with(@place, '9')">
          <xsl:element name="desc">
            <xsl:value-of select="@place"/>
          </xsl:element>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="place">
            <xsl:value-of select="@place"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates/>
    </add>
  </xsl:template>

  <!-- change msItem/q to quote -->
  <xsl:template match="msItem/q">
    <quote>
      <xsl:apply-templates/>
    </quote>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">msItem/q changed to quote</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>



  <!-- msitem/@defective true/false/etc. -->
  <xsl:template match="@defective[.='yes']">
    <xsl:attribute name="defective">true</xsl:attribute>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">
        <xsl:value-of
          select="concat(name(parent::node()), 
	'/@defective=&quot;yes&quot; changed to proper boolean true')"
        />
      </xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>


  <!-- replace spaces in @type and @subtype 
and locus/@from locus/@to
-->
  <xsl:template match="@type | @subtype |locus/@from | locus/@to">
    <xsl:attribute name="{name()}">
      <xsl:value-of select="translate(., ' /?', '___')"/>
    </xsl:attribute>
    <xsl:if test="contains(., ' ') or  contains(., '/') or contains(.,'?')"
      ><!--
      <xsl:call-template name="message">
        <xsl:with-param name="reason">
          <xsl:value-of
            select="concat(name(parent::node()), 
					'/@', name(), ' translated space, / or ? to _')"
          />
        </xsl:with-param>
      </xsl:call-template>-->
    </xsl:if>
  </xsl:template>


  <xsl:template
    match="decoNote/@technique | decoNote/@figurative |
		decoNote/@illustrative | decoNote/@size |decoNote/@quality"
    ><!--
    <xsl:call-template name="dumpAttrMessage">
      <xsl:with-param name="reason">Not in P5</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>

  <xsl:template match="msPart/idno">
    <altIdentifier>
      <xsl:choose>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'form')">
          <xsl:attribute name="type">former</xsl:attribute>
          <idno>
            <xsl:apply-templates/>
          </idno>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'sys')">
          <xsl:attribute name="type">system</xsl:attribute>
          <idno>
            <xsl:apply-templates/>
          </idno>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'par')">
          <xsl:attribute name="type">partial</xsl:attribute>
          <idno>
            <xsl:apply-templates/>
          </idno>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'int')">
          <xsl:attribute name="type">internal</xsl:attribute>
          <idno>
            <xsl:apply-templates/>
          </idno>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">other</xsl:attribute>
          <xsl:if test="normalize-space(@type) != ''">
            <xsl:variable name="conv">
              <xsl:if test="@type">
                <xsl:value-of
                  select="concat('msPart/idno/@type = &quot;',@type, '&quot; now altidentifier/@type=&quot;other&quot;')"
                />
              </xsl:if>
            </xsl:variable>
            <xsl:message>
              <xsl:value-of select="$conv"/>
            </xsl:message>
          </xsl:if>
          <idno>
            <xsl:apply-templates/>
          </idno>
        </xsl:otherwise>
      </xsl:choose>
    </altIdentifier>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">changed msPart/idno to altIdentifier/idno
        with altIdentifier/@type='other'</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>

  <xsl:template match="msPart">
    <msPart>
      <xsl:apply-templates select="@*"/>
      <xsl:choose>
        <xsl:when test="idno"/>
        <!-- don't do anything different -->
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="@id">
              <altIdentifier type="other">
                <idno>
                  <xsl:value-of select="@id"/>
                </idno>
              </altIdentifier>
            </xsl:when>
            <xsl:when test="@n">
              <altIdentifier type="other">
                <idno>
                  <xsl:value-of select="@n"/>
                </idno>
              </altIdentifier>
            </xsl:when>
            <xsl:when test="msContents/@id">
              <altIdentifier type="other">
                <idno>
                  <xsl:value-of select="msContents/@id"/>
                </idno>
              </altIdentifier>
            </xsl:when>
            <xsl:when test="msContents/@n">
              <altIdentifier type="other">
                <idno>
                  <xsl:value-of select="msContents/@n"/>
                </idno>
              </altIdentifier>
            </xsl:when>

            <xsl:otherwise>
              <altIdentifier type="other">
                <idno>
                  <xsl:value-of select="ancestor::msDescription/msIdentifier/idno"/>
                </idno>
              </altIdentifier>
              <!--
              <xsl:message>altIdentifier added in conversion, no existing
              one</xsl:message>-->
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="node()|comment()"/>
    </msPart>
  </xsl:template>

  <xsl:template match="note/msIdentifier | note/msIdentifier/repository
	| note/msIdentifier/idno">
    <seg type="{name()}">
      <xsl:apply-templates/>
    </seg>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">note/msIdentifier (and child repository and
        idno) changed to 'seg', not allowed in P5</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>



  <!-- otherLangs='#DAN' -->
  <xsl:template match="textLang">
    <textLang>
      <xsl:choose>
        <xsl:when test="@langKey">
          <xsl:attribute name="mainLang">
            <xsl:value-of select="@langKey"/>
          </xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="textLang">
            <xsl:value-of select="."/>
          </xsl:variable>
          <xsl:choose>
            <!-- Try to find an @id to use for @mainLang if it exists -->
            <xsl:when test="//language[@id][contains(., $textLang)]">
              <xsl:attribute name="mainLang">
                <xsl:value-of select="//language[@id][contains(., $textLang)]/@id"/>
              </xsl:attribute>
            </xsl:when>
            <!-- on failure use semantically icky guess of english...which is silly really. -->
            <xsl:otherwise>
              <xsl:attribute name="mainLang">en</xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*[name() != 'langKey'] | node()"/>
    </textLang>
  </xsl:template>

  <xsl:template match="textLang/@langKey">
    <xsl:attribute name="mainLang">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="textLang/@otherLangs" priority="5">
    <xsl:attribute name="otherLangs">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>


  <xsl:template match="msItem/summary">
    <note type="summary">
      <xsl:apply-templates/>
    </note>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">msItem/summary changed to
        note/@type="summary"</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>


  <!-- zap @creator on teiHeader -->

  <xsl:template match="teiHeader/@creator"
    ><!--
    <xsl:call-template name="dumpAttrMessage">
      <xsl:with-param name="reason">Not in P5</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>


  <!-- msContents/overview now summary -->
  <xsl:template match="msContents/overview">
    <summary>
      <xsl:apply-templates/>
    </summary>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">msContents/overview changed to
      summary</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>
  <xsl:template match="msContents/overview/p">
    <xsl:apply-templates/>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">msContents/overview/p incorporated into
        summary</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>

  <xsl:template match="@defective[.='unk']">
    <xsl:attribute name="defective">unknown</xsl:attribute>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">
        <xsl:value-of
          select="concat(name(parent::node()), 
				'/@defective=&quot;unk&quot; changed to unknown')"
        />
      </xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>




  <!-- msItem was allowed to just have locus, but now must have one of
		the below at least -->
  <xsl:template
    match="msItem[not(author | respStmt | title | rubric | summary | 
		incipit | explicit | colophon | textLang | q | decoNote 
		| bibl | listBibl | note | msItem)]">
    <msItem>
      <xsl:apply-templates/>
      <note type="addedByConversion"/>
    </msItem>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">msItem must have one non-locus child, adding
        empty note.</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>

  <!-- respStmt was allowed to not have resp, but now must-->
  <xsl:template match="respStmt[not(resp)]">
    <respStmt>
      <xsl:apply-templates/>
      <resp n="addedByConversion"/>
    </respStmt>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">respStmt must have a resp, adding an empty
        one</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>





  <!-- Bug in wellcome files. -->
  <xsl:template match="listBibl/head">
    <xsl:choose>
      <xsl:when test="not(parent::listBibl/bibl)">
        <bibl>
          <xsl:apply-templates/>
        </bibl>
      </xsl:when>
      <xsl:otherwise>
        <head>
          <xsl:apply-templates/>
        </head>
      </xsl:otherwise>
    </xsl:choose>
    <!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">listBibl requires at least one bibl element,
        changing head to bibl</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>


  <!-- name/@role temporarily removed until added to ODD 
  <xsl:template match="name/@role | author/@key | author/@ref">
    <xsl:call-template name="dumpAttrMessage">
      <xsl:with-param name="reason">Not in P5 (but to-be-added to ENRICH
      ODD)</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  -->


  <xsl:template match="dimensions[height and width]">
    <dimensions>
      <xsl:choose>
        <xsl:when
          test="@type = 'binding' or @type='boxed' or  @type='leaf' or  @type='slip' or  @type='written'">
          <xsl:attribute name="type">
            <xsl:value-of select="@type"/>
          </xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">unknown</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*[not(name()='type')]|*"/>
    </dimensions>
  </xsl:template>

  <xsl:template match="dimensions[text()]" priority="-1"
    ><!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">dimensions element not allowed text(),
        throwing it away!</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>


  <!-- BRM doesn't use xref/@doc properly as entities -->
  <xsl:template match="@doc" priority="5">
    <xsl:variable name="target">
      <xsl:choose>
        <xsl:when test="unparsed-entity-uri(.)='' and
					not(.='')">
          <xsl:value-of select="."/>
        </xsl:when>
        <xsl:when test="not(unparsed-entity-uri(.)='')">
          <xsl:value-of select="unparsed-entity-uri(.)"/>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="to">
      <xsl:if test="../@to">
        <xsl:value-of select=" ../@to"/>
      </xsl:if>
    </xsl:variable>
    <xsl:variable name="from">
      <xsl:if test="../@from">
        <xsl:value-of select="../@from"/>
      </xsl:if>
    </xsl:variable>
    <xsl:if test="$target">
      <xsl:attribute name="target">
        <xsl:choose>
          <xsl:when test="../@from">
            <xsl:value-of select="concat($target, '#', $from)"/>
            <!--
            <xsl:call-template name="message">
              <xsl:with-param name="reason">@doc renamed @target, appending
                @from</xsl:with-param>
            </xsl:call-template>-->
          </xsl:when>
          <xsl:when test="../@to">
            <xsl:value-of select="concat($target, '#', $to)"/>
            <!--
            <xsl:call-template name="message">
              <xsl:with-param name="reason">@doc renamed @target, appending
              @to</xsl:with-param>
            </xsl:call-template>-->
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$target"/>
            <!--
            <xsl:call-template name="message">
              <xsl:with-param name="reason">@doc renamed
              @target</xsl:with-param>
            </xsl:call-template>-->
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>
  <xsl:template match="xref/@to"/>
  <xsl:template match="xref/@from"/>

  <!-- Just for BRM since they have @ids that are the same in
different msDescriptions -->

  <xsl:template match="msHeading/note/listBibl/bibl/@id"
    ><!--
    <xsl:call-template name="message">
      <xsl:with-param name="reason">BRM @id hack:
        msHeading/note/listBibl/bibl/@id are duplicated,
      removing</xsl:with-param>
    </xsl:call-template>-->
  </xsl:template>




  <!-- Utility functions -->

  <!-- replace all instances of X in a string with Y -->
  <xsl:template name="replace-string">
    <xsl:param name="text"/>
    <xsl:param name="replace"/>
    <xsl:param name="with"/>
    <xsl:choose>
      <xsl:when test="contains($text,$replace)">
        <xsl:value-of select="substring-before($text,$replace)"/>
        <xsl:value-of select="$with"/>
        <xsl:call-template name="replace-string">
          <xsl:with-param name="text" select="substring-after($text,$replace)"/>
          <xsl:with-param name="replace" select="$replace"/>
          <xsl:with-param name="with" select="$with"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="dumpAttrMessage"
    ><!--
    <xsl:param name="reason"/>
    <xsl:variable name="parent">
      <xsl:value-of select="name(parent::node())"/>
    </xsl:variable>
    <xsl:variable name="attribute">
      <xsl:value-of select="name()"/>
    </xsl:variable>
    <xsl:variable name="value">
      <xsl:value-of select="."/>
    </xsl:variable>
    <xsl:message>dumped <xsl:value-of
        select="concat($parent,'/@',$attribute,'=&quot;',$value,'&quot;')"
        /><xsl:if test="$reason">(<xsl:value-of select="$reason"
    />)</xsl:if></xsl:message>-->
  </xsl:template>


  <xsl:template name="message"
    ><!--
    <xsl:param name="reason"/>
    <xsl:if test="$debugMsg='true'">
      <xsl:message>Debug: <xsl:value-of select="$reason"/></xsl:message>
    </xsl:if>-->
  </xsl:template>


  <!-- date stuff -->

  <!--
Trying to cope with date problems:

Problems to deal with include
- 3 numeral years
- 00 as month
- 00 as date
- date format as DD.MM.YYYY
- date format as YYYY.MM.DD
- date format as 'c. YYYY'
and many more...
*ARGH*
  -->


  <xsl:template
    match="attribute::notBefore | attribute::notAfter |
		dateRange/@from | dateRange/@to">

    <!-- I'm doing the dates this long expanded way around because I'm not
sure at the moment what other date forms we'll encounter.  Near the
end we could rationalise this to be more efficient. (For example
processing of Dot.Dates and Hyphen-Dates could be done in the same
place if the incoming attribute value is translate()d to one form (-)
first, and I could do the choosing inside a variable rather than the
other way around.
    
    
    -->

    <xsl:choose>
      <!-- deal with '.' style dates but not circa ones -->
      <xsl:when test="contains(., '.') and not(contains(., 'c'))">
        <xsl:choose>
          <!-- When first bit has 4 digits, make that the year -->
          <xsl:when test="string-length(substring-before(.,'.'))=4">
            <xsl:variable name="year">
              <xsl:value-of select="substring-before(., '.')"/>
            </xsl:variable>
            <xsl:variable name="month">
              <xsl:if
                test="string-length(substring-before(substring-after(.,'.'),
                  '.'))=2
                  and not(. = '00')">
                <xsl:value-of
                  select="concat('-',substring-before(substring-after(.,'.'),
                  '.'))"
                />
              </xsl:if>
            </xsl:variable>
            <xsl:variable name="day">
              <xsl:if
                test="string-length(substring-after(substring-after(.,'.'),
                  '.'))=2
                  and not(. = '00')">
                <xsl:value-of
                  select="concat('-',substring-after(substring-after(.,'.'),
                    '.'))"
                />
              </xsl:if>
            </xsl:variable>
            <xsl:attribute name="{name()}">
              <xsl:value-of select="concat($year, $month, $day)"/>
            </xsl:attribute>
          </xsl:when>
          <!-- When it has 3 digits assume pre-1000 year -->
          <xsl:when test="string-length(substring-before(.,'.'))=3">
            <xsl:variable name="year">
              <xsl:value-of select="concat('0',substring-before(., '.'))"/>
            </xsl:variable>
            <xsl:variable name="month">
              <xsl:if
                test="string-length(substring-before(substring-after(.,'.'),
                  '.'))=2
                  and not(. = '00')">
                <xsl:value-of
                  select="concat('-',substring-before(substring-after(.,'.'),
                    '.'))"
                />
              </xsl:if>
            </xsl:variable>
            <xsl:variable name="day">
              <xsl:if
                test="string-length(substring-after(substring-after(.,'.'),
                  '.'))=2
                  and not(. = '00')">
                <xsl:value-of
                  select="concat('-',substring-after(substring-after(.,'.'),
                    '.'))"
                />
              </xsl:if>
            </xsl:variable>
            <xsl:attribute name="{name()}">
              <xsl:value-of select="concat($year, $month, $day)"/>
            </xsl:attribute>
          </xsl:when>
          <!-- When first bit has 2 digits and last bit has 4
              digits, make the last bit the year -->
          <xsl:when
            test="string-length(substring-before(.,'.'))=2 and
            string-length(substring-after(substring-after(., '.'),'.'))=4">
            <xsl:variable name="year">
              <xsl:value-of select="substring-after(substring-after(., '.'),'.')"/>
            </xsl:variable>
            <xsl:variable name="month">
              <xsl:if
                test="string-length(substring-before(substring-after(.,'.'),
                '.'))=2
                and not(. = '00')">
                <xsl:value-of
                  select="concat('-',substring-before(substring-after(.,'.'),
                  '.'))"
                />
              </xsl:if>
            </xsl:variable>
            <xsl:variable name="day">
              <xsl:if
                test="string-length(substring-before(.,'.'))=2
                  and not(. = '00')">
                <xsl:value-of select="concat('-',substring-before(.,'.'))"/>
              </xsl:if>
            </xsl:variable>
            <xsl:attribute name="{name()}">
              <xsl:value-of select="concat($year, $month, $day)"/>
            </xsl:attribute>
          </xsl:when>
          <!-- When first bit has 2 digits and last bit has 3
              digits, make the last bit the year with a leading zero -->
          <xsl:when
            test="string-length(substring-before(.,'.'))=2 and
            string-length(substring-after(substring-after(.,
            '.'),'.'))=3">
            <xsl:variable name="year">
              <xsl:value-of
                select="concat('0', substring-after(substring-after(.,
                '.'),'.'))"/>
            </xsl:variable>
            <xsl:variable name="month">
              <xsl:if
                test="string-length(substring-before(substring-after(.,'.'),
                '.'))=2
                and not(. = '00')">
                <xsl:value-of
                  select="concat('-',substring-before(substring-after(.,'.'),
                  '.'))"
                />
              </xsl:if>
            </xsl:variable>
            <xsl:variable name="day">
              <xsl:if
                test="string-length(substring-before(.,'.'))=2
                and not(. = '00')">
                <xsl:value-of select="concat('-',substring-before(.,'.'),
                  '.')"/>
              </xsl:if>
            </xsl:variable>
            <xsl:attribute name="{name()}">
              <xsl:value-of select="concat($year, $month, $day)"/>
            </xsl:attribute>
          </xsl:when>

        </xsl:choose>
        <!--
        <xsl:call-template name="message">
          <xsl:with-param name="reason">date modified from dot separators to W3C
            style.</xsl:with-param>
        </xsl:call-template>
-->
      </xsl:when>
      <!-- deal with 'c.' style dates -->
      <xsl:when test="contains(., 'c.') or contains(., 'ca')">
        <xsl:choose>
          <!-- 4 digits -->
          <xsl:when
            test="string-length(substring-after(., '.'))=4 or string-length(substring-after(.,'ca'))=4">
            <xsl:variable name="year">
              <xsl:choose>
                <xsl:when test="contains(., '.')">
                  <xsl:value-of select="substring-after(.,'.')"/>
                </xsl:when>
                <xsl:when test="contains(., 'ca')">
                  <xsl:value-of select="substring-after(.,'ca')"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="."/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <xsl:attribute name="{name()}">
              <xsl:value-of select="$year"/>
            </xsl:attribute>
          </xsl:when>
        </xsl:choose>
        <!--
        <xsl:call-template name="message">
          <xsl:with-param name="reason">date modified from c./ca style -
            warning imprecision lost!</xsl:with-param>
        </xsl:call-template>-->
      </xsl:when>

      <!-- deal with '-' style dates -->
      <xsl:when test="contains(., '-')">

        <xsl:choose>
          <!-- When first bit has 4 digits, make that the year -->
          <xsl:when test="string-length(substring-before(.,'-'))=4">
            <xsl:variable name="year">
              <xsl:value-of select="substring-before(., '-')"/>
            </xsl:variable>
            <xsl:variable name="month">
              <xsl:if
                test="string-length(substring-before(substring-after(.,'-'),
                '-'))=2
                and not(substring-before(substring-after(.,'-'),'-')='00')">
                <xsl:value-of
                  select="concat('-',substring-before(substring-after(.,'-'),
                  '-'))"
                />
              </xsl:if>
            </xsl:variable>
            <xsl:variable name="day">
              <xsl:if
                test="string-length(substring-after(substring-after(.,'-'),
                '-'))=2
                and not(substring-after(substring-after(.,'-'),
                '-') = '00')">
                <xsl:value-of
                  select="concat('-',substring-after(substring-after(.,'-'),
                  '-'))"
                />
              </xsl:if>
            </xsl:variable>
            <xsl:attribute name="{name()}">
              <xsl:value-of select="concat($year, $month, $day)"/>
            </xsl:attribute>
          </xsl:when>
          <!-- When it has 3 digits assume pre-1000 year -->
          <xsl:when test="string-length(substring-before(.,'-'))=3">
            <xsl:variable name="year">
              <xsl:value-of select="concat('0',substring-before(., '-'))"/>
            </xsl:variable>
            <xsl:variable name="month">
              <xsl:if
                test="string-length(substring-before(substring-after(.,'-'),
                '-'))=2
                and not(. = '00')">
                <xsl:value-of
                  select="concat('-',substring-before(substring-after(.,'-'),
                  '-'))"
                />
              </xsl:if>
            </xsl:variable>
            <xsl:variable name="day">
              <xsl:if
                test="string-length(substring-after(substring-after(.,'-'),
                '-'))=2
                and not(. = '00')">
                <xsl:value-of
                  select="concat('-',substring-after(substring-after(.,'-'),
                  '-'))"
                />
              </xsl:if>
            </xsl:variable>
            <xsl:attribute name="{name()}">
              <xsl:value-of select="concat($year, $month, $day)"/>
            </xsl:attribute>
          </xsl:when>
          <!-- When first bit has 2 digits and last bit has 4
            digits, make the last bit the year -->
          <xsl:when
            test="string-length(substring-before(.,'-'))=2 and
            string-length(substring-after(substring-after(., '-'),'-'))=4">
            <xsl:variable name="year">
              <xsl:value-of select="substring-after(substring-after(., '-'),'-')"/>
            </xsl:variable>
            <xsl:variable name="month">
              <xsl:if
                test="string-length(substring-before(substring-after(.,'-'),
                '-'))=2
                and not(. = '00')">
                <xsl:value-of
                  select="concat('-',substring-before(substring-after(.,'-'),
                  '-'))"
                />
              </xsl:if>
            </xsl:variable>
            <xsl:variable name="day">
              <xsl:if
                test="string-length(substring-before(.,'-'))=2
                and not(. = '00')">
                <xsl:value-of select="concat('-',substring-before(.,'-'))"/>
              </xsl:if>
            </xsl:variable>
            <xsl:attribute name="{name()}">
              <xsl:value-of select="concat($year, $month, $day)"/>
            </xsl:attribute>
          </xsl:when>
          <!-- When first bit has 2 digits and last bit has 3
            digits, make the last bit the year with a leading zero -->
          <xsl:when
            test="string-length(substring-before(.,'-'))=2 and
            string-length(substring-after(substring-after(.,
            '-'),'-'))=3">
            <xsl:variable name="year">
              <xsl:value-of
                select="concat('0', substring-after(substring-after(.,
                '-'),'-'))"/>
            </xsl:variable>
            <xsl:variable name="month">
              <xsl:if
                test="string-length(substring-before(substring-after(.,'-'),
                '-'))=2
                and not(. = '00')">
                <xsl:value-of
                  select="concat('-',substring-before(substring-after(.,'-'),
                  '-'))"
                />
              </xsl:if>
            </xsl:variable>
            <xsl:variable name="day">
              <xsl:if
                test="string-length(substring-before(.,'-'))=2
                and not(. = '00')">
                <xsl:value-of select="concat('-',substring-before(.,'-'),
                  '-')"/>
              </xsl:if>
            </xsl:variable>
            <xsl:attribute name="{name()}">
              <xsl:value-of select="concat($year, $month, $day)"/>
            </xsl:attribute>
          </xsl:when>
        </xsl:choose>
        <!-- 
        <xsl:call-template name="message">
          <xsl:with-param name="reason">date modified to W3C</xsl:with-param>
        </xsl:call-template>-->
      </xsl:when>

      <!-- Something else... try to guess?   -->
      <xsl:otherwise>
        <xsl:choose>
          <!-- unknown date -->
          <xsl:when test=".='unk'"/>
          <xsl:when test="string-length(.)=0"/>
          <xsl:when test="string-length(.)=4">
            <xsl:attribute name="{name()}">
              <xsl:value-of select="."/>
            </xsl:attribute>
          </xsl:when>
          <xsl:when test="string-length(.)=3">
            <xsl:attribute name="{name()}">
              <xsl:value-of select="concat('0',.)"/>
            </xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="{name()}">
              <xsl:value-of select="."/>
            </xsl:attribute>
            <!--
            <xsl:message>
              <xsl:value-of select="concat(name(), ' = ', .)"/>
            </xsl:message>-->
          </xsl:otherwise>
        </xsl:choose>
        <!-- 
        <xsl:call-template name="message">
          <xsl:with-param name="reason">date modified from uncertain
          format</xsl:with-param>
        </xsl:call-template>-->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- 

<note type="general" place="unspecified" anchored="yes" TEIform="note"> Era confesor de S.M. la 
<name type="female"
reg="Isabel de Borbón, reina consorte de Felipe IV,  Rey de España">reina
Isabel</name>

-->
  <xsl:template match="name/@reg">
    <choice>
      <orig>
        <xsl:value-of select="normalize-space(parent::name)"/>
      </orig>
      <reg>
        <xsl:value-of select="normalize-space(.)"/>
      </reg>
    </choice>
  </xsl:template>

  <xsl:template match="name">
    <name>
      <xsl:attribute name="type">
        <xsl:choose>
          <xsl:when test="@type='person' or @type='org' or @type='place'">
            <xsl:value-of select="@type"/>
          </xsl:when>
          <xsl:when test="@type='female'">person</xsl:when>
          <xsl:otherwise>unknown</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:choose>
        <xsl:when test="@role and not(@key)">
          <xsl:attribute name="key">
            <xsl:value-of select="@role"/>
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="@role and not(@n)">
          <xsl:attribute name="key">
            <xsl:value-of select="@role"/>
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="not(@role)"/>
        <xsl:otherwise/>
      </xsl:choose>
      <xsl:apply-templates select="@*[name() != 'type'][name() !='role']|node()"/>
    </name>
  </xsl:template>

  <xsl:template match="supplied">
    <supplied>
      <xsl:attribute name="reason">
        <xsl:choose>
          <xsl:when
            test="contains(translate(@reason, $uc, $lc), 'om') or contains(translate(@reason, $uc, $lc), 'lac')"
            >omitted</xsl:when>
          <xsl:when test="contains(translate(@reason, $uc, $lc), 'illeg')">illegible</xsl:when>
          <xsl:when test="contains(translate(@reason, $uc, $lc), 'dam')">damage</xsl:when>
          <xsl:when
            test="contains(translate(@reason, $uc, $lc), 'unk') or contains(translate(@reason, $uc, $lc), 'oth')"
            >unknown</xsl:when>
          <xsl:otherwise>unknown</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:apply-templates select="@*[name() != 'reason']|node()"/>
    </supplied>
  </xsl:template>


  <xsl:template match="availability">
    <availability>
      <xsl:attribute name="status">
        <xsl:choose>
          <xsl:when test="@reason='free'">free</xsl:when>
          <xsl:when test="@reason='restricted'">restricted</xsl:when>
          <xsl:otherwise>unknown</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:apply-templates select="@*[name() != 'status']|node()"/>
    </availability>
  </xsl:template>


  <xsl:template match="custEvent">
    <custEvent>
      <xsl:apply-templates select="@*[name() != 'type']"/>
      <xsl:choose>
        <xsl:when
          test="contains(translate(@type, $uc, $lc), 'check') or contains(translate(@type, $uc, $lc), 'chk')">
          <xsl:attribute name="type">check</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'cons')">
          <xsl:attribute name="type">conservation</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'desc')">
          <xsl:attribute name="type">description</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'ex')">
          <xsl:attribute name="type">exhibition</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'loan')">
          <xsl:attribute name="type">loan</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'photo')">
          <xsl:attribute name="type">photography</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="type">other</xsl:attribute>
          <xsl:if test="normalize-space(@type) != ''">
            <xsl:variable name="conv">
              <xsl:value-of
                select="concat(name(), '/@type = &quot;',@type, '&quot; now &quot;other&quot;')"
              />
            </xsl:variable>
            <xsl:message>
              <xsl:value-of select="$conv"/>
            </xsl:message>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates/>
    </custEvent>
  </xsl:template>



  <xsl:template match="biblScope">
    <biblScope>
      <xsl:apply-templates select="@*[name() !='type']"/>
      <xsl:choose>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'v')">
          <xsl:attribute name="type">volume</xsl:attribute>
        </xsl:when>
        <xsl:when test="contains(translate(@type, $uc, $lc), 'p')">
          <xsl:attribute name="type">pages</xsl:attribute>
        </xsl:when>
        <!-- This default is semantically icky. -->
        <xsl:otherwise>
          <xsl:attribute name="type">pages</xsl:attribute>
          <!--<xsl:choose>
          <xsl:when test="@type"><note type="conversion">@type changed to 'pages' from: <xsl:value-of select="@type"/></note></xsl:when>
          <xsl:otherwise><note type="conversion">@type added with default value of 'pages'</note></xsl:otherwise>
         </xsl:choose>-->
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates/>
    </biblScope>
  </xsl:template>


  <xsl:template match="hi">
    <hi>
      <xsl:attribute name="rend">
        <xsl:choose>
          <xsl:when test="@rend">
            <xsl:choose>
              <xsl:when test="contains(translate(@rend, $uc,$lc), 'hyph')">hyphenated</xsl:when>
              <xsl:when
                test="contains(translate(@rend, $uc,$lc), 'double') and contains(translate(@rend, $uc,$lc), 'under') "
                >double-underline</xsl:when>
              <xsl:when
                test="contains(translate(@rend, $uc,$lc), 'under') or contains(translate(@rend, $uc,$lc), 'ul') "
                >underline</xsl:when>
              <xsl:when test="contains(translate(@rend, $uc,$lc), 'bold')">bold</xsl:when>
              <xsl:when test="contains(translate(@rend, $uc,$lc), 'cap')">caps</xsl:when>
              <xsl:when
                test="contains(translate(@rend, $uc,$lc), 'ital') or translate(@rend, $uc, $lc)='it' "
                >italic</xsl:when>
              <xsl:when test="contains(translate(@rend, $uc,$lc), 'sup')">sup</xsl:when>
              <xsl:when test="contains(translate(@rend, $uc,$lc), 'rub')">rubric</xsl:when>
              <xsl:otherwise>bold</xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <!-- This default is semantically icky. -->
          <xsl:otherwise>
            <xsl:attribute name="rend">bold</xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
    </hi>
  </xsl:template>



</xsl:stylesheet>
