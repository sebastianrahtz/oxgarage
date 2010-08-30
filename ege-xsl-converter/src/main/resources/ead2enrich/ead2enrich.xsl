<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns="http://www.tei-c.org/ns/1.0"
    exclude-result-prefixes="tei tmp" xmlns:tmp="http://www.oucs.ox.ac.uk/tmp">
    <!-- 
  Version 0.92-2009-02-24
  This script has been developed as part of the ENRICH project: http://enrich.manuscriptorium.com 
    and is available from the OUCS website at http://tei.oucs.ox.ac.uk/ENRICH/  ... to use it you should 
    prepare a valid EAD file according to Bodleian cataloguing guidelines. 
    It may contain multiple 'c01' elements, each containing a manuscript description.   This should be run with something like:
    
    saxon inputfile.xml ead2enrich.xsl 

If you want advice or support in using this script, please contact enrich@oucs.ox.ac.uk
  
  This script is licensed under a Creative Commons Attribution license.  If you use it please 
  cite enrich@oucs.ox.ac.uk as the author and point to http://tei.oucs.ox.ac.uk/ENRICH/
  -->



    <xsl:strip-space elements="*"/>
    <xsl:output indent="yes" method="xml"/>


    <xsl:template match="/">
        <xsl:variable name="file">
            <xsl:value-of select="lower-case(/ead/@id)"/>
        </xsl:variable>

        <xsl:for-each select=".//c01">
            <xsl:variable name="c01" select="."/>

            <xsl:variable name="pos">
                <xsl:number format="0001"/>
            </xsl:variable>



            <xsl:result-document href="{concat($file, $pos, '.xml')}">
                <teiHeader xmlns="http://www.tei-c.org/ns/1.0" xml:id="{concat($file, $pos)}">
                    <fileDesc>
                        <titleStmt>
                            <title>
                                <xsl:value-of
                                    select="normalize-space(.//unitid[@type='shelfmark'][1])"/>
                            </title>
                            <title type="collection">
                                <xsl:value-of
                                    select="normalize-space(ancestor::ead/eadheader/filedesc/titlestmt/titleproper)"
                                />
                            </title>
                        </titleStmt>
                        <publicationStmt>
                            <p>Converted by the ENRICH project</p>
                        </publicationStmt>
                        <xsl:if test="ancestor::ead/archdesc/scopecontent/p">
                            <notesStmt>
                                <note type="collection">
                                    <xsl:for-each select="ancestor::ead/archdesc/scopecontent/p ">
                                        <p>
                                            <xsl:apply-templates/>
                                        </p>
                                    </xsl:for-each>
                                </note>
                            </notesStmt>
                        </xsl:if>
                        <sourceDesc>
                            <xsl:variable name="id"
                                select="concat('bod-', translate(did/unitid[@type='shelfmark'][1], ' (),', '____'))"/>
                            <xsl:variable name="shelfmark">
                                <xsl:value-of
                                    select="normalize-space(did/unitid[@type='shelfmark'])"/>
                            </xsl:variable>
                            <msDesc xml:id="{$id}" xml:lang="eng">
                                <msIdentifier>
                                    <country>United Kingdom</country>
                                    <region type="county">Oxfordshire</region>
                                    <settlement>Oxford</settlement>
                                    <institution>University of Oxford</institution>
                                    <repository>Bodleian Library</repository>
                                    <idno type="shelfmark">
                                        <xsl:value-of select="normalize-space($shelfmark)"/>
                                    </idno>
                                    <xsl:if test="did/unitid[@type='SCN']">
                                        <altIdentifier type="internal">
                                            <idno type="SCN">
                                                <xsl:value-of
                                                  select="normalize-space(did/unitid[@type='SCN'])"
                                                />
                                            </idno>
                                        </altIdentifier>
                                    </xsl:if>
                                </msIdentifier>
                                <xsl:choose>
                                    <xsl:when test="did/physdesc[@label='composite']">
                                        <xsl:call-template name="physDesc">
                                            <xsl:with-param name="root" select="$c01"/>
                                        </xsl:call-template>

                                        <xsl:call-template name="history">
                                            <xsl:with-param name="root" select="$c01"/>
                                        </xsl:call-template>

                                        <xsl:call-template name="additional">
                                            <xsl:with-param name="root" select="$c01"/>
                                        </xsl:call-template>

                                        <xsl:variable name="titles">
                                            <xsl:apply-templates select="did/unittitle/title"
                                                mode="separate-things"/>
                                        </xsl:variable>
                                        <xsl:variable name="dates">
                                            <xsl:apply-templates select="did/unittitle/unitdate"
                                                mode="separate-things"/>
                                        </xsl:variable>
                                        <xsl:variable name="decoration">
                                            <xsl:apply-templates
                                                select="scopecontent[contains(lower-case(head), 'decorat')]/p"
                                                mode="separate-things"/>
                                        </xsl:variable>
                                        <xsl:for-each select="$titles/tmp:item">
                                            <xsl:variable name="part" select="position()"/>
                                            <xsl:variable name="title" select="."/>
                                            <msPart xml:id="{concat($id, '-', $part)}">
                                                <altIdentifier type="partial">
                                                  <idno type="part">
                                                  <xsl:value-of
                                                  select="concat($shelfmark, ' - Part ', $part)"/>
                                                  </idno>
                                                </altIdentifier>
                                                <xsl:call-template name="msContents">
                                                  <xsl:with-param name="root" select="$c01"/>
                                                  <xsl:with-param name="title" select="$title"/>
                                                  <xsl:with-param name="dates" select="$dates"/>
                                                  <xsl:with-param name="part" select="$part"/>
                                                  <xsl:with-param name="decoration"
                                                  select="$decoration"/>
                                                </xsl:call-template>


                                            </msPart>
                                        </xsl:for-each>


                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:call-template name="msContents">
                                            <xsl:with-param name="root" select="$c01"/>
                                        </xsl:call-template>
                                        <xsl:call-template name="physDesc">
                                            <xsl:with-param name="root" select="$c01"/>
                                        </xsl:call-template>

                                        <xsl:call-template name="history">
                                            <xsl:with-param name="root" select="$c01"/>
                                        </xsl:call-template>

                                        <xsl:call-template name="additional">
                                            <xsl:with-param name="root" select="$c01"/>
                                        </xsl:call-template>


                                    </xsl:otherwise>
                                </xsl:choose>

                            </msDesc>



                        </sourceDesc>
                    </fileDesc>
                </teiHeader>
            </xsl:result-document>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="emph">
        <hi>
            <xsl:if test="@render">
                <xsl:attribute name="rend" select="@render"/>
            </xsl:if>
            <xsl:apply-templates/>
        </hi>
    </xsl:template>

    <!-- <xsl:template match="unittitle/title/emph"><title><xsl:if test="@render"><xsl:attribute name="rend" select="@render"/></xsl:if><xsl:apply-templates/></title></xsl:template>-->

    <xsl:template match="p">
        <p>
            <xsl:apply-templates/>
        </p>
    </xsl:template>



    <xsl:template name="msContents">
        <xsl:param name="root"/>
        <xsl:param name="part"/>
        <xsl:param name="dates" select="'NO'"/>
        <xsl:param name="title" select="'NO'"/>
        <xsl:param name="decoration" select="'NO'"/>
        <xsl:variable name="mainLang">
            <xsl:call-template name="guessLang">
                <xsl:with-param name="langtext">
                    <xsl:value-of select="normalize-space(lower-case($root/@langmaterial))"/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:variable>
        <msContents>
            <summary>
                <xsl:choose>
                    <xsl:when test="not($title='NO')">
                        <xsl:if test="$root/did/unittitle/title">
                            <seg type="unittitle">
                                <xsl:apply-templates select="$title"/>
                            </seg>
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:if test="$root/did/unittitle/title">
                            <seg type="unittitle">
                                <xsl:apply-templates select="$root/did/unittitle/title"/>
                            </seg>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="$root/did/unittitle/geogname">
                    <origPlace>
                        <xsl:value-of select="normalize-space($root/did/unittitle/geogname)"/>
                    </origPlace>
                </xsl:if>
                <xsl:choose>
                    <xsl:when test="not($dates='NO')">
                        <xsl:if test="$dates/tmp:item[$part]">
                            <origDate>
                                <!-- origDate note allowed to have 'hi' or similar -->
                                <xsl:value-of select="normalize-space($dates/tmp:item[$part])"/>
                            </origDate>
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:if test="$root/did/unittitle/unitdate">
                            <origDate>
                                <!-- origDate note allowed to have 'hi' or similar -->
                                <xsl:value-of select="normalize-space($root/did/unittitle/unitdate)"
                                />
                            </origDate>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>

            </summary>
            <!-- At this point guess what languages there are -->

            <textLang mainLang="{$mainLang}">
                <xsl:if test="contains(lower-case($root/@langmaterial), 'and ')">
                    <xsl:attribute name="otherLangs">
                        <xsl:for-each
                            select="tokenize(substring-after(lower-case($root/@langmaterial), 'and '), ' and ')">
                            <xsl:call-template name="guessLang">
                                <xsl:with-param name="langtext">
                                    <xsl:value-of select="normalize-space(.)"/>
                                </xsl:with-param>
                            </xsl:call-template>
                            <xsl:choose>
                                <xsl:when test="position()=last()"/>
                                <xsl:otherwise>
                                    <xsl:text> </xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                    </xsl:attribute>
                </xsl:if>
                <xsl:value-of select="normalize-space($root/@langmaterial)"/>
            </textLang>
        </msContents>
        <xsl:if
            test="not($decoration='NO') and not(normalize-space($decoration/tmp:item[$part])='-')">
            <xsl:if test="$decoration/tmp:item[$part]">
                <physDesc>
                    <xsl:comment> note: only decoDesc included in msPart physDesc </xsl:comment>
                    <decoDesc>
                        <decoNote>
                            <xsl:value-of select="normalize-space($decoration/tmp:item[$part])"/>
                        </decoNote>
                    </decoDesc>
                </physDesc>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <xsl:template name="guessLang">
        <xsl:param name="langtext"/>
        <xsl:choose>
            <xsl:when test="starts-with($langtext, 'english')">eng</xsl:when>
            <xsl:when test="starts-with($langtext, 'anglo-saxon')">ang</xsl:when>
            <xsl:when test="starts-with($langtext, 'latin')">lat</xsl:when>
            <xsl:when test="starts-with($langtext, 'french')">fre</xsl:when>
            <xsl:when test="starts-with($langtext, 'italian')">ita</xsl:when>
            <!-- add in more languages as we find them -->
            <xsl:otherwise>x-other</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="physDesc">
        <xsl:param name="root"/>
        <physDesc>
            <xsl:if test="$root/did/physdesc/physfacet[lower-case(@type)='material']">
                <xsl:variable name="material">
                    <xsl:choose>
                        <xsl:when
                            test="contains(lower-case($root/did/physdesc/physfacet[lower-case(@type)='material']), 'mix')"
                            >mixed</xsl:when>
                        <xsl:when
                            test="contains(lower-case($root/did/physdesc/physfacet[lower-case(@type)='material']), 'paper')"
                            >chart</xsl:when>
                        <xsl:when
                            test="contains(lower-case($root/did/physdesc/physfacet[lower-case(@type)='material']), 'chart')"
                            >chart</xsl:when>
                        <xsl:when
                            test="contains(lower-case($root/did/physdesc/physfacet[lower-case(@type)='material']), 'parch')"
                            >perg</xsl:when>
                        <xsl:when
                            test="contains(lower-case($root/did/physdesc/physfacet[lower-case(@type)='material']), 'perg')"
                            >perg</xsl:when>
                        <xsl:otherwise>UNKNOWN!!</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>


                <xsl:comment>No @form information available from bodley, marking all as
                    'codex'</xsl:comment>
                <objectDesc form="codex">
                    <supportDesc material="{$material}">
                        <support>
                            <xsl:value-of
                                select="normalize-space($root/did/physdesc/physfacet[lower-case(@type)='material'])"
                            />
                        </support>
                    </supportDesc>
                    <!-- Composite manuscript labels -->
                    <xsl:if test="$root/did/physdesc[@label='composite']">
                        <layoutDesc>
                            <p n="composite">
                                <xsl:apply-templates select="$root/did/physdesc[@label='composite']"
                                />
                            </p>
                        </layoutDesc>
                    </xsl:if>
                </objectDesc>
            </xsl:if>
            <xsl:if test="$root/scopecontent[contains(lower-case(head), 'decorat')]/p">
                <decoDesc>
                    <decoNote>
                        <xsl:apply-templates
                            select="$root/scopecontent[contains(lower-case(head), 'decorat')]/*"/>
                    </decoNote>
                </decoDesc>
            </xsl:if>
        </physDesc>
    </xsl:template>

    <xsl:template name="history">
        <xsl:param name="root"/>

        <history>
            <origin>
                <xsl:if test="$root/did/unittitle/unitdate">
                    <origDate>
                        <!-- origDate note allowed to have 'hi' or similar -->
                        <xsl:value-of select="normalize-space($root/did/unittitle/unitdate)"/>
                    </origDate>
                </xsl:if>

                <xsl:if test="$root/did/unittitle/geogname">
                    <origPlace>
                        <xsl:value-of select="normalize-space($root/did/unittitle/geogname)"/>
                    </origPlace>
                </xsl:if>
            </origin>
            <xsl:if test="$root/admininfo/custodhist">
                <acquisition>
                    <xsl:value-of select="normalize-space($root/admininfo/custodhist)"/>
                </acquisition>
            </xsl:if>
        </history>
    </xsl:template>


    <xsl:template name="additional">
        <xsl:param name="root"/>

        <additional>
            <xsl:if
                test="$root/odd//daogrp/daoloc[@role='SC'] or $root/odd//daogrp/daoloc[@role='QUARTO']">
                <adminInfo>
                    <recordHist>
                        <source>
                            <xsl:apply-templates
                                select="$root/odd//daogrp[daoloc/@role='SC'] | $root/odd//daogrp[daoloc/@role='QUARTO']"
                            />
                        </source>
                    </recordHist>
                </adminInfo>
            </xsl:if>
            <xsl:if test="$root/odd//daogrp/daoloc[@role='MS']">
                <surrogates>
                    <xsl:apply-templates select="$root/odd//daogrp[daoloc/@role='MS']"/>
                </surrogates>
            </xsl:if>
            <xsl:if test="$root/add/bibliography">
                <listBibl>
                    <xsl:apply-templates select="$root/add/bibliography"/>
                </listBibl>
            </xsl:if>
        </additional>

    </xsl:template>


    <xsl:template match="add/bibliography/p | add/bibliography/head"/>

    <xsl:template match="add/bibliography/bibref">
        <bibl>
            <xsl:apply-templates/>
        </bibl>
    </xsl:template>


    <xsl:template match="bibref/num[@type='date']">
        <date type="pub" when="{normalize-space(.)}">
            <xsl:value-of select="."/>
        </date>
    </xsl:template>

    <xsl:template match="bibref/num[@type='shelfmark']">
        <idno type="shelfmark">
            <xsl:value-of select="."/>
        </idno>
    </xsl:template>


    <xsl:template match="list[@type='deflist']">
        <list type="gloss">
            <xsl:apply-templates/>
        </list>
    </xsl:template>

    <xsl:template match="list[@type='deflist']/head">
        <head>
            <xsl:apply-templates/>
        </head>
    </xsl:template>

    <xsl:template match="list[@type='deflist']/defitem">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="list[@type='deflist']/defitem/label">
        <label>
            <xsl:apply-templates/>
        </label>
    </xsl:template>
    <xsl:template match="list[@type='deflist']/defitem/item">
        <item>
            <xsl:apply-templates/>
        </item>
    </xsl:template>

    <xsl:template match="genreform">
        <seg type="genreform">
            <xsl:apply-templates/>
        </seg>
    </xsl:template>
    <xsl:template match="subject">
        <seg type="subject">
            <xsl:apply-templates/>
        </seg>
    </xsl:template>

    <xsl:template match="daogrp">
        <listBibl>
            <xsl:apply-templates select="daoloc"/>
        </listBibl>
    </xsl:template>

    <xsl:template match="daoloc[@role='SC'] | daoloc[@role='QUARTO']">
        <bibl type="{@role}">
            <xsl:if test="@href">
                <xsl:attribute name="facs">
                    <xsl:value-of select="@href"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="@role='SC'"><title>Summary Catalogue</title>, </xsl:when>
                <xsl:when test="@role='QUARTO'"><title>Quarto Catalogue</title>, </xsl:when>
            </xsl:choose>
            <xsl:variable name="volume">
                <xsl:choose>
                    <xsl:when test="contains(@title, 'Vol.') and contains(@title, 'p.')">
                        <xsl:value-of select="substring-before(@title, 'p.')"/>
                    </xsl:when>
                    <xsl:otherwise/>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="pages">
                <xsl:choose>
                    <xsl:when test="contains(@title, 'Vol.') and contains(@title,'p.')">
                        <xsl:value-of select="concat('p. ', substring-after(@title, 'p.'))"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="@title"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:if test="$volume">
                <biblScope type="volume">
                    <xsl:value-of select="$volume"/>
                </biblScope>
            </xsl:if>
            <xsl:if test="$pages">
                <biblScope type="pages">
                    <xsl:value-of select="$pages"/>
                </biblScope>
            </xsl:if>
            <xsl:if test="daodesc">
                <note>
                    <xsl:apply-templates/>
                </note>
            </xsl:if>
        </bibl>
    </xsl:template>
    <!--
<bibl facs="images/bar052203.jpg">
                        <title>manuscript image</title>
                        <biblScope type="pages">1r</biblScope>
                        <note>Whole page with initial O[mnibus] and border.</note>
                     </bibl>
        -->

    <xsl:template match="daoloc[@role='MS']" priority="10">
        <bibl type="MS">
            <xsl:if test="@href">
                <xsl:attribute name="facs">
                    <xsl:value-of select="@href"/>
                </xsl:attribute>
            </xsl:if>
            <title>Manuscript Image</title>, <xsl:if test="@title">
                <biblScope type="pages">
                    <xsl:value-of select="@title"/>
                </biblScope>
            </xsl:if>
            <xsl:if test="daodesc">
                <note>
                    <xsl:apply-templates/>
                </note>
            </xsl:if>
        </bibl>
    </xsl:template>


    <xsl:template match="daoloc[@role='MS']">
        <figure n="MS">
            <xsl:if test="@href">
                <graphic url="{@href}"/>
            </xsl:if>
            <xsl:if test="@title">
                <figDesc n="title">
                    <xsl:value-of select="@title"/>
                </figDesc>
            </xsl:if>
            <xsl:if test="daodesc">
                <figDesc n="daodesc">
                    <xsl:apply-templates/>
                </figDesc>
            </xsl:if>
        </figure>
    </xsl:template>

    <xsl:template match="daodesc/p">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="scopecontent[contains(lower-case(head), 'decorat')]/p"
        mode="separate-things">
        <xsl:variable name="separated-titles" as="node()*">
            <xsl:apply-templates mode="separate-things"/>
        </xsl:variable>

        <xsl:variable name="titles" as="element(tmp:item)*">
            <xsl:for-each-group select="$separated-titles" group-starting-with="tmp:tmp">
                <xsl:element name="tmp:item">
                    <xsl:copy-of select="current-group()[not(self::tmp:tmp)]"/>
                </xsl:element>
            </xsl:for-each-group>
        </xsl:variable>
        <xsl:copy-of select="$titles"/>
    </xsl:template>


    <xsl:template match="title|unitdate" mode="separate-things">
        <xsl:variable name="separated-titles" as="node()*">
            <xsl:apply-templates mode="separate-things"/>
        </xsl:variable>

        <xsl:variable name="titles" as="element(tmp:item)*">
            <xsl:for-each-group select="$separated-titles" group-starting-with="tmp:tmp">
                <xsl:element name="tmp:item">
                    <xsl:copy-of select="current-group()[not(self::tmp:tmp)]"/>
                </xsl:element>
            </xsl:for-each-group>
        </xsl:variable>
        <xsl:copy-of select="$titles"/>
    </xsl:template>

    <xsl:template match="*" mode="separate-things">
        <xsl:apply-templates select="."/>
    </xsl:template>

    <xsl:template match="text()" mode="separate-things">
        <xsl:analyze-string select="." regex="\|\|">
            <xsl:matching-substring>
                <tmp:tmp/>
            </xsl:matching-substring>
            <xsl:non-matching-substring>
                <xsl:value-of select="."/>
            </xsl:non-matching-substring>
        </xsl:analyze-string>
    </xsl:template>


</xsl:stylesheet>
