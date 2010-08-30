<!SGML "ISO 8879:1986"

--***************************************************************--
--***************************************************************--
--***************************************************************--
--                                                               --
--                                                               --
--                                                               --
--                                                               --
--                                                               --
--                  ENCODED ARCHIVAL DESCRIPTION                 --
--                    DOCUMENT TYPE DEFINITION                   --
--                       SGML DECLARATION                        --
--                          VERSION 1.0                          --
--                                                               --
--                                                               --
--                                                               --
--                                                               --
--                                                               --
--                                                               --
--                                                               --
--                 Prepared and Maintained by the                --
--           Encoded Archival Description Working Group          --
--              of the Society of American Archivists            --
--                            and the                            --
--          Network Development and MARC Standards Office        --
--                   of the Library of Congress                  --
--                                                               --
--                                                               --
--                                                               --
--                                                               --
--                                                               --
--                                                               --
--                                                               --
--                 Society of American Archivists                --
--                        Chicago, Illinois                      --
--                              1998                             --
--                                                               --
--                                                               --
--                                                               --
--                                                               --
--***************************************************************--
--***************************************************************--
--***************************************************************--
--***************************************************************--
--***************************************************************--
--***************************************************************--
--         Encoded Archival Description SGML Declaration         --
--                                                               --
--              EAD Technical Document No. 1, Part 6             --
--                                                               --
--                                                               --
--  This file constitutes a component of version 1.0 of the      --
--  Encoded Archival Description Document Type Definition (DTD), --
--  released in August 1998.  Version 1.0 of the DTD supersedes  --
--  the beta version made available via the EAD Internet site    --
--  beginning in 1996.                                           --
--                                                               --
--                                                               --
--  The SAA Encoded Archival Description Working Group is        --
--  responsible for updating and editing the EAD DTD (EAD        --
--  Technical Document No. 1) and tag library (EAD Technical     --
--  Document No. 2).                                             --
--                                                               --
--                                                               --
--  The LC Network Development and MARC Standards Office serves  --
--  as the maintenance agency for online EAD documentation,      --
--  including storage and delivery of electronic files and       --
--  maintenance of the EAD Internet site, located at             --
--  http://www.loc.gov/ead/.                                     --
--                                                               --
--                                                               --
--  Published by the Society of American Archivists.             --
--                                                               --
--                                                               --
--  Tag Library available from:                                  --
--                  Society of American Archivists               --
--                  527 S. Wells Street, 5th floor               --
--                  Chicago, IL 60607                            --
--                                                               --
--                  Phone:    312/922-0140                       --
--                  Fax:      312/347-1452                       --
--                  Internet: info@archivists.org                --
--                                                               --
--                                                               --
--  (c) Society of American Archivists, 1998.                    --
--   All rights reserved.                                        --
--***************************************************************--
--***************************************************************--

--***************************************************************--
--***************************************************************--
--            INTRODUCTION                                       --
--***************************************************************--
--***************************************************************--
--    NAME:      Encoded Archival Description SGML Declaration   --
--                      File name = eadsgml.dcl                  --
--                      Version = 1.0                            --
--                      Version date = 980824                    --
--                                                               --
--    EDITOR:    Daniel V. Pitti                                 --
--               Institute for Advanced Technology in the        --
--               Humanities, University of Virginia              --
--                                                               --
--    PURPOSE:   SGML Declaration for use in SGML applications;  --
--               XML uses implied SGML declaration.              --
--                                                               --
--    CONTACT:   EAD listserv of the Library of Congress Network --
--               Development and MARC Standards Office.          --
--               To subscribe to this listserv, send a one-line  --
--               message to LISTSERV@LOC.GOV containing the      --
--               text:  SUBSCRIBE EAD YourName. Once subscribed, --
--               submit messages to EAD@LOC.GOV.                 --
--                                                               --
--               EAD Internet site, located at                   --
--               http://www.loc.gov/ead/                         --
--                                                               --
--    DEPENDENCIES:                                              --
--               None.                                           --
--                                                               --
--***************************************************************--
--***************************************************************--

--***************************************************************--
--            CHANGE HISTORY (POST VERSION 1.0 CHANGES)          --
--***************************************************************--
-- 
NUM  DESCRIPTION OF CHANGE                                MM/DD/YY


                                                                 --
--***************************************************************--
--            END CHANGE HISTORY                                 --
--***************************************************************--


CHARSET
       BASESET 
       "ISO 646-1983//CHARSET International Reference Version
       (IRV)//ESC 2/5 4/0"

       DESCSET
          0    9   UNUSED
          9    2   9
         11    2   UNUSED
         13    1   13
         14   18   UNUSED
         32   95   32
        127    1   UNUSED

       BASESET  
       "ISO Registration Number 100//CHARSET
       ECMA-94 Right Part of Latin Alphabet Nr. 1//ESC 2/13 4/1"

       DESCSET
        128    32     UNUSED
        160    95     32
        255    1      UNUSED

-- ============================================ --
CAPACITY
     SGMLREF

     TOTALCAP      600000
     ENTCAP        100000
     ENTCHCAP      120000
     ELEMCAP        70000
     GRPCAP        220000
     ATTCAP        150000
     ATTCHCAP       70000
     AVGRPCAP      150000
     IDCAP          70000
     IDREFCAP       70000

     SCOPE        DOCUMENT

-- ============================================ --
SYNTAX
     SHUNCHAR NONE

     BASESET
         "ISO 646-1983//CHARSET 
          International Reference Version (IRV)//ESC 2/5 4/0"

     DESCSET    0  128   0

     FUNCTION
         RE                     13
         RS                     10
         SPACE                  32
         TAB       SEPCHAR       9

     NAMING
         LCNMSTRT   ""
         UCNMSTRT   ""
         LCNMCHAR   "-._:"
         UCNMCHAR   "-._:"
         NAMECASE
          GENERAL   YES
          ENTITY    NO
DELIM      
         GENERAL    SGMLREF
         SHORTREF   NONE

NAMES    SGMLREF

QUANTITY SGMLREF    
      ATTCNT      200
      ATTSPLEN    960
      ENTLVL       50
      GRPCNT      253
      GRPGTCNT    253
      GRPLVL       50
      LITLEN     2048
      NAMELEN     128
      TAGLEN     2048
      TAGLVL      400

-- ============================================ --

FEATURES
  MINIMIZE
   DATATAG        NO
   OMITTAG        NO
   RANK           NO
   SHORTTAG       YES

  LINK
   SIMPLE         NO
   IMPLICIT       NO
   EXPLICIT       NO

  OTHER
   CONCUR         NO
   SUBDOC         NO
   FORMAL         NO
   APPINFO        NONE                                            >