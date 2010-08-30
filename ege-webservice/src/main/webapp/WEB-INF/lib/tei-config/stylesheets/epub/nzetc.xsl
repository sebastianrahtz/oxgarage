<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:zip="http://apache.org/cocoon/zip-archive/1.0"
                xmlns:tei="http://www.tei-c.org/ns/1.0"
                xmlns:nzetc="http://www.nzetc.org/structure"
                xmlns:dc="http://purl.org/dc/elements/1.1/" >

<!--http://wiki.tei-c.org/index.php/Cocoon_epub_Compiler-->
  <!--
      - A stylesheet to generate a zip archive based on the worked example at
      - http://www.hxa.name/articles/content/epub-guide_hxa7241_2007.html
      - and the cocoon zip archive 
      - http://cocoon.apache.org/2.0/userdocs/serializers/ziparchive-serializer.html
      - 
      - This is not very polished and assumes lots of stuff about the NZETC website
      - 
  -->

  <xsl:key name="authors" match="//tei:author" use="normalize-space(.)"/>
  
  <xsl:template match="/">

    <xsl:variable name="pages" select="//*[@nzetc:id][/tei:TEI/@xml:id != @xml:id]"/>

    <!-- Remove macrons from document titles for interoperability -->
    <xsl:variable name="accented">ĀĒĪŌŪāēīōū </xsl:variable>
    <xsl:variable name="unaccented">AEIOUaeiou </xsl:variable>
    
    
    <zip:archive>
      <!-- this needs to be first in the archive -->
      <zip:entry name="mimetype" src="html-package/content/mimetype"/>

      <!-- Metadata / intro page -->
      <zip:entry name="tm/scholarly/metadata-{tei:TEI/@xml:id}.html" 
                 src="cocoon:/tm/scholarly/metadata-tei-{tei:TEI/@xml:id}.xhtml11" />
      <!-- raw TEI -->
      <!--
          <zip:entry name="tei-source/{tei:TEI/@xml:id}.xml" 
          src="cocoon:/tei-source/{tei:TEI/@xml:id}.xml" />
      -->
      <!-- main page -->
      <zip:entry name="tm/scholarly/tei-{/tei:TEI/@xml:id}.html"
                 src="cocoon:/tm/scholarly/tei-{/tei:TEI/@xml:id}.xhtml11" />

      <!-- all the subsequence pages includes both figures and texts -->
      <xsl:for-each select="$pages">
        <xsl:choose>
          <!-- figures -->
          <xsl:when test="./tei:graphic">
            <xsl:variable name="entity" select="tei:graphic/@url" />
            <xsl:if test="not(preceding::tei:figure[tei:graphic/@url = $entity])">
              
              <!-- Removed full-page images because of space constraints 
                  <zip:entry name="tm/scholarly/{/tei:TEI/@xml:id}-fig-{@xml:id}.html" 
                  src="cocoon:/tm/scholarly/{/tei:TEI/@xml:id}-fig-{@xml:id}.xhtml11" />
                  <zip:entry name="etexts/{/tei:TEI/@xml:id}/{$entity}" 
                  src="cocoon:/etexts/{/tei:TEI/@xml:id}/{$entity}" />
              -->
              <zip:entry name="{concat('etexts/',
                               /tei:TEI/@xml:id,
                               '/', 
                               substring-before($entity,'.'),
                               '(t100).',
                               substring-after($entity,'.'))}" 
                         src="{concat('cocoon:/etexts/',
                              /tei:TEI/@xml:id,
                              '/',
                              substring-before($entity,'.'),
                              '(t100).',
                              substring-after($entity,'.'))}" />
              <zip:entry name="{concat('etexts/',
                               /tei:TEI/@xml:id,
                               '/', 
                               substring-before($entity,'.'),
                               '(t150).',
                               substring-after($entity,'.'))}" 
                         src="{concat('cocoon:/etexts/',
                              /tei:TEI/@xml:id,
                              '/',
                              substring-before($entity,'.'),
                              '(t150).',
                              substring-after($entity,'.'))}" />
              <zip:entry name="{concat('etexts/',
                               /tei:TEI/@xml:id,
                               '/', 
                               substring-before($entity,'.'),
                               '(h280).',
                               substring-after($entity,'.'))}" 
                         src="{concat('cocoon:/etexts/',
                              /tei:TEI/@xml:id,
                              '/', 
                              substring-before($entity,'.'),
                              '(h280).',
                              substring-after($entity,'.'))}" />
              
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <!-- texts -->
            <zip:entry name="tm/scholarly/{@nzetc:id}.html" 
                       src="cocoon:/tm/scholarly/{@nzetc:id}.xhtml11" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
      
      
      <!-- extra static pages -->
      <zip:entry name="tm/main.css" src="cocoon:/tm/main.css" />
      <zip:entry name="tm/print.css" src="cocoon:/tm/print.css" />
      <zip:entry name="tm/referer.js" src="cocoon:/tm/referrer.js" />

      <!-- other images (nzetc logo etc) -->
      <!--
          <zip:entry name="tm/images/pdficon.gif" src="cocoon:/website/images/pdficon.gif" />
          <zip:entry name="tm/images/nzetc-logo.jpg" src="cocoon:/tm/images/nzetc-logo.jpg" />
          <zip:entry name="tm/images/margin-tile.jpg" src="cocoon:/tm/images/margin-tile.jpg" />
          <zip:entry name="tm/images/nzetc-watermark.gif" src="cocoon:/tm/images/nzetc-watermark.gif" />
          <zip:entry name="tm/images/related-tile.jpg" src="cocoon:/tm/images/related-tile.jpg" />
          <zip:entry name="tm/images/nzetc-title.gif" src="cocoon:/tm/images/nzetc-title.gif" />
      -->


      <!-- epub specific stuff -->
      <zip:entry name="META-INF/container.xml"  serializer="xml">
        <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
          <rootfiles>
            <rootfile full-path="content.opf" media-type="application/oebps-package+xml"/>
          </rootfiles>
        </container>
      </zip:entry>
      
      <!-- epub metadata  and manifest -->
      <zip:entry name="content.opf" serializer="xml">
        <package xmlns="http://www.idpf.org/2007/opf" unique-identifier="dcidid" 
                 version="2.0">
          
          <metadata xmlns:dc="http://purl.org/dc/elements/1.1/"
                    xmlns:dcterms="http://purl.org/dc/terms/"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xmlns:opf="http://www.idpf.org/2007/opf">
            <dc:title><xsl:value-of select="translate(normalize-space(//tei:title),$accented,$unaccented)"/></dc:title>
            <dc:language xsi:type="dcterms:RFC3066"><xsl:value-of select="normalize-space(/@xml:lang)"/></dc:language>
            <dc:identifier id="dcidid" opf:scheme="URI">http://www.nzetc.org/tm/scholarly/tei-<xsl:value-of select="/tei:TEI/@xml:id"/>.html</dc:identifier>
            <xsl:for-each select="//tei:textClass/tei:keywords//tei:rs">
              <dc:subject><xsl:value-of select="normalize-space(.)"/></dc:subject>
            </xsl:for-each>
            <!--<dc:description>A guide for making Epub ebooks/publications.</dc:description>-->
            <!--<dc:relation>http://www.hxa.name/</dc:relation>-->


            <!--
                <xsl:for-each select="//tei:author/">
                <xsl:variable name="author" select="." />
                <xsl:if test="not(preceding::*[author = $entity])">
                
                <dc:creator><xsl:value-of select="."/></dc:creator>
                </xsl:if>
                </xsl:for-each>
            -->
            <xsl:for-each select="//tei:author[generate-id() = generate-id(key('authors',normalize-space(.))[1])]">
              <dc:creator><xsl:value-of select="normalize-space(.)"/></dc:creator>
            </xsl:for-each>
            
            <dc:publisher>New Zealand Electronic Text Centre</dc:publisher>
            <dc:date xsi:type="dcterms:W3CDTF">2007-12-28</dc:date>
            <dc:rights>Creative Commons</dc:rights>
          </metadata>
          
          <manifest>
            <!-- Fixed items -->
            <!--
                <item id="logo"      
                href="tm/images/nzetc-logo.jpg"           
                media-type="image/jpeg" />
                <item id="watermark"
                href="tm/images/nzetc-watermark.gif"           
                media-type="image/gif" />
                <item id="margin-tile"      
                href="tm/images/margin-tile.jpg"           
                media-type="image/jpeg" />
                <item id="related-tile"      
                href="tm/images/related-tile.jpg"           
                media-type="image/jpeg" />

<item id="nzetc-title"      
href="tm/images/nzetc-title.gif"           
media-type="image/gif" />
            -->
            <item id="main.css"      
                  href="tm/main.css"           
                  media-type="text/css" />
            <item id="print.css"      
                  href="tm/print.css"           
                  media-type="text/css" />
            <item id="referer.js"      
                  href="tm/referer.js"           
                  media-type="text/javascript" />
            <item id="head"      
                  href="tm/scholarly/tei-{/tei:TEI/@xml:id}.html"           
                  media-type="application/xhtml+xml" />
            <item id="metadata"      
                  href="tm/scholarly/metadata-{/tei:TEI/@xml:id}.html"           
                  media-type="application/xhtml+xml" />


            <!-- this includes both figures and texts -->
            <xsl:for-each select="$pages">
              <xsl:choose>
                <!-- figures -->
                <xsl:when test="self::tei:figure">
                  <xsl:variable name="entity" select="tei:graphic/@url" />
                  <xsl:if test="not(preceding::tei:figure[tei:graphic/@url = $entity])">
                    
                    <!--
                        <item id="image-page-{@xml:id}"      
                        href="tm/scholarly/{/tei:TEI/@xml:id}-fig-{@xml:id}.html"           
                        media-type="application/xhtml+xml" />
                    -->

                    <xsl:variable name="mimetype">
                      <xsl:choose>
                        <xsl:when test="contains(tei:graphic/@url,'.gif')">image/gif</xsl:when>
                        <xsl:otherwise>image/jpeg</xsl:otherwise>
                      </xsl:choose>
                    </xsl:variable>
                    <!--
                        <item id="image-{@xml:id}"      
                        href="etexts/{/tei:TEI/@xml:id}/{$entity}"           
                        media-type="{$mimetype}" />
                    -->
                    <!-- all three kinds of thumbnail -->
                    <item id="image-h280-{@xml:id}"      
                          href="{concat('etexts/',
                                /tei:TEI/@xml:id,
                                '/',
                                substring-before(tei:graphic/@url,'.'),
                                '(h280).',
                                substring-after(tei:graphic/@url,'.'))}"           
                          media-type="{$mimetype}" />
                    <item id="image-t100-{@xml:id}"      
                          href="{concat('etexts/',
                                /tei:TEI/@xml:id,
                                '/',
                                substring-before(tei:graphic/@url,'.'),
                                '(t100).',
                                substring-after(tei:graphic/@url,'.'))}"           
                          media-type="{$mimetype}" />
                    <item id="image-t150-{@xml:id}"      
                          href="{concat('etexts/',
                                /tei:TEI/@xml:id,
                                '/',
                                substring-before(tei:graphic/@url,'.'),
                                '(t150).',
                                substring-after(tei:graphic/@url,'.'))}"           
                          media-type="{$mimetype}" />

                  </xsl:if>
                </xsl:when>
                <xsl:otherwise>
                  <!-- texts -->
                  <item id="page-{@xml:id}"      
                        href="tm/scholarly/{@nzetc:id}.html"           
                        media-type="application/xhtml+xml" />
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>

            <item id="ncx"      href="toc.ncx"                 
                  media-type="application/x-dtbncx+xml" />
          </manifest>
          
          <spine toc="ncx">
            <itemref idref="metadata" />
            <itemref idref="head" />
            <!-- this includes both figures and texts -->
            <xsl:for-each select="$pages[not(self::tei:figure)]">
              <!-- texts -->
              <itemref idref="page-{@xml:id}" />
            </xsl:for-each>
          </spine>

          <guide>
            <reference type="colophon"       
                       title="Colophon"              
                       href="tm/scholarly/metadata-{tei:TEI/@xml:id}.html" />
            <reference type="text"       
                       title="Text"              
                       href="tm/scholarly/tei-{/tei:TEI/@xml:id}.html" />
            
            <!-- this includes both figures and texts -->
            <xsl:for-each select="$pages[not(self::tei:figure)]">
              <!-- texts -->
              <reference type="text"       
                         title="{normalize-space(./tei:head)}"              
                         href="tm/scholarly/{@nzetc:id}.html" />
            </xsl:for-each>
            
          </guide>
        </package>
      </zip:entry>
      
      
      <!-- daisybook ncx table of contents -->
      <!-- this tells the epub reader how to present the epub to the reader -->
      <zip:entry name="toc.ncx"  serializer="xml">
        <ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">

          <head>
            <meta name="dtb:uid" content="http://www.nzetc.org/tm/scholarly/tei-{/tei:TEI/@xml:id}.html"/>
            <!--<meta name="dtb:depth" content="2"/>-->
            <meta name="dtb:totalPageCount" content="{count(//tei:pb)}"/>
            <meta name="dtb:maxPageNumber" content="{//tei:pb[@n][position()=last()]}"/>
          </head>

          <docTitle>
            <text><xsl:value-of select="translate(normalize-space(//tei:title),$accented,$unaccented)"/></text>
          </docTitle>

          <navMap>
            <navPoint id="navPoint-1" playOrder="1">
              <navLabel>
                <text>digital title page and colophon</text>
              </navLabel>
              <content src="tm/scholarly/metadata-{tei:TEI/@xml:id}.html"/>
            </navPoint>
            
            <xsl:for-each select="$pages[not(self::tei:figure)]">
              
              <navPoint id="navPoint-{@xml:id}" playOrder="{position()+1}">
                <navLabel>
                  <text>
                    <xsl:choose>
                      <xsl:when test="normalize-space(.//tei:head)">
                        <xsl:value-of select="normalize-space(.//tei:head)"/>
                      </xsl:when>
                      <xsl:when test="normalize-space(@n)">
                        <xsl:text>[</xsl:text><xsl:value-of select="normalize-space(@n)"/><xsl:text>]</xsl:text>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:text>[untitled]</xsl:text>
                      </xsl:otherwise>
                    </xsl:choose>
                  </text>
                </navLabel>
                <content src="tm/scholarly/{@nzetc:id}.html"/>
              </navPoint>
            </xsl:for-each>
          </navMap>
        </ncx>
      </zip:entry>
      
    </zip:archive>
    
  </xsl:template>

</xsl:stylesheet>
