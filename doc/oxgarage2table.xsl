<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    exclude-result-prefixes="xlink tei" version="2.0">
    <xsl:output indent="yes"/>
    <xsl:param name="serverToUse"
        >http://oxgarage.oucs.ox.ac.uk:8080/ege-webservice/</xsl:param>
    <xsl:variable name="server" select="$serverToUse"/>
    <xsl:variable name="conversions"
        select="document('http://oxgarage.oucs.ox.ac.uk:8080/ege-webservice/Conversions/')"/>
    
    <xsl:variable name="eachType" >
        <xsl:for-each
            select="distinct-values($conversions/input-data-types/input-data-type/@id/tokenize(.,
          ':')[1])">
            <xsl:variable name="conversionType" select="current()"/>
            <table xmlns="http://www.tei-c.org/ns/1.0">
                <head>
                    <xsl:value-of select="$conversionType"/>
                </head>
                <xsl:variable name="eachType" xmlns="">
                    <xsl:for-each
                        select="$conversions/input-data-types/input-data-type[@id/tokenize(.,
                  ':')[1] = current()]">
                        <input>
                            <xsl:value-of select="tokenize(@id, ':')[2]"/>
                        </input>

                        <xsl:variable name="outputTypesDoc"
                            select="document(@xlink:href)"/>
                        <xsl:variable name="outputTypes">
                            <xsl:for-each
                                select="$outputTypesDoc//conversion/tokenize(substring-after(@id, '/O:'), ':')[2]">
                                <output>
                                    <xsl:value-of select="."/>
                                </output>
                            </xsl:for-each>
                        </xsl:variable>
                        <xsl:copy-of select="$outputTypes"/>

                    </xsl:for-each>
                </xsl:variable>

                <row role="label">
                    <cell> Inputs:</cell>
                    <xsl:for-each select="distinct-values($eachType/input)">
                        <cell n="input" rend="label shaded">
                            <xsl:value-of select="."/>
                        </cell>
                    </xsl:for-each>
                </row>

                <xsl:for-each select="distinct-values($eachType//output)">
                    <xsl:variable name="currentOutputType" select="."/>
                    <row>
                        <cell n="output" role="label" rend="shaded label">
                            <xsl:value-of select="$currentOutputType"/>
                        </cell>

                        <xsl:for-each
                            select="distinct-values($eachType/input)">
                            <xsl:variable name="currentInputType" select="."/>
                            
                            <xsl:for-each
                                select="$conversions/input-data-types/input-data-type[@id/tokenize(.,
                          ':')[2] = $currentInputType]">
                                <xsl:variable name="outputTypesDoc"
                                    select="document(@xlink:href)"/>
                                <xsl:variable name="yes">
                                <xsl:for-each select="$outputTypesDoc//conversions-path">
                                    <xsl:choose>
                                    <xsl:when test="conversion[1][tokenize(substring-after(@id, 'I:'), ':')[2]=$currentInputType]">
                                        <xsl:choose>
                                            <xsl:when test="conversion[last()][tokenize(substring-after(@id, '/O:'), ':')[2]=$currentOutputType]">yes</xsl:when>
                                            <xsl:otherwise>no</xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                </xsl:choose>
                                    </xsl:for-each>
                                </xsl:variable>
                                <xsl:choose>
                                    <xsl:when test="contains($yes,
                                        'yes')"><cell rend="grade5 bold">&#x2713;</cell></xsl:when>
                                    <xsl:otherwise><cell><xsl:text>&#xa0;</xsl:text></cell></xsl:otherwise>
                                </xsl:choose>
                              </xsl:for-each>
                                
                        </xsl:for-each>
                        
                    </row>
                </xsl:for-each>
            </table>
        </xsl:for-each>
    </xsl:variable>

    <xsl:template name="main">
        <TEI
            xmlns="http://www.tei-c.org/ns/1.0">
            <teiHeader>
                <fileDesc>
                    <titleStmt>
                        <title>OxGarage Conversion Matrix</title>
                    </titleStmt>
                    <publicationStmt>
                        <authority>Sebastian.Rahtz@it.ox.ac.uk</authority>
                        <date
                            when="{format-date(current-date(),'[Y]-[M02]-[D02]')}"/>
                    </publicationStmt>
                    <sourceDesc>
                        <p>The server at <xsl:value-of select="$server"/></p>
                    </sourceDesc>
                </fileDesc>
                <html:style type="text/css">
                    table { border: 1px solid black; border-collapse: collapse; }
                    tbody td, thead th { border: 1px solid black; }
                    thead th { padding: 0px 2px 0px 2px }
                    tr.label { font-size: 90%; }
                    td.label { font-size: 80%; }
                    td { font-weight: bold; text-align: center }
                    td.self { background-color: #eee }
                </html:style>
            </teiHeader>
            <text>
                <body>
                    
        <xsl:copy-of select="$eachType"/>
                     
            </body>
        </text>
    </TEI>
    
    </xsl:template>

    <!-- copy everything not matched default template  -->
    <xsl:template match="@*|node()|comment()|processing-instruction()"
        priority="-5" mode="buildDoc">
        <xsl:copy>
            <xsl:apply-templates
                select="@*|node()|comment()|processing-instruction()"/>
        </xsl:copy>
    </xsl:template>






</xsl:stylesheet>
