SERVER=localhost
PORT=8080

P="<conversions><conversion%20index='0'><property%20id='pl.psnc.dl.ege.tei.profileNames'>iso</property></conversion></conversions>" 
O="<conversions><conversion%20index='0'><property%20id='pl.psnc.dl.ege.tei.profileNames'>ota</property></conversion></conversions>" 
L="<conversions><conversion%20index='1'><property%20id='oxgarage.lang'>fr</property></conversion></conversions>" 


test:
	curl -s -o conversions.xml http://$(SERVER):$(PORT)/ege-webservice/Conversions
	rm -rf temp-results
	mkdir  temp-results
	echo To DocX
	curl -s  -o temp-results/result.docx -F upload=@test.xml http://$(SERVER):$(PORT)/ege-webservice/Conversions/TEI%3Atext%3Axml/docx%3Aapplication%3Avnd.openxmlformats-officedocument.wordprocessingml.document
	echo ODD to JSON
	curl -s  -F upload=@test.odd  -o temp-results/test.js http://$(SERVER):$(PORT)/ege-webservice/Conversions/ODD%3Atext%3Axml/ODDC%3Atext%3Axml/oddjson%3Aapplication%3Ajson/
	perl -p -i -e 's/"date":"20.*Z"/"date":""/' temp-results/test.js
	diff temp-results/test.js expected-results/test.js
	echo TCP to TEI P5
	curl -s -o temp-results/A00005.xml -F upload=@A00005.xml -F upload=@A00005.hdr http://$(SERVER):$(PORT)/ege-webservice/Conversions/TCP%3Atext%3Axml/TEI%3Atext%3Axml/
	diff temp-results/A00005.xml expected-results/A00005.xml
	echo ODD to DTD
	curl -s  -F upload=@test.odd  -o temp-results/result4.dtd http://$(SERVER):$(PORT)/ege-webservice/Conversions/ODD%3Atext%3Axml/ODDC%3Atext%3Axml/dtd%3Aapplication%3Axml-dtd/
	perl -p -i -e 's/DTD generated.*/DTD/' temp-results/result4.dtd
	diff temp-results/result4.dtd expected-results/result4.dtd
	echo To LaTeX
	curl -s  -F upload=@test.xml  -o temp-results/result2.tex http://$(SERVER):$(PORT)/ege-webservice/Conversions/TEI%3Atext%3Axml/latex%3Aapplication%3Ax-latex
	perl -p -i -e 's/TheFullDate{.*}/TheFullDate{}/' temp-results/result2.tex
	diff temp-results/result2.tex expected-results/result2.tex
	echo To ePub
	curl -s  -o temp-results/result.epub -F upload=@test.xml http://$(SERVER):$(PORT)/ege-webservice/Conversions/TEI%3Atext%3Axml/epub%3Aapplication%3Aepub%2Bzip
	curl -s  -F upload=@test.xml -o temp-results/result2.docx  http://$(SERVER):$(PORT)/ege-webservice/Conversions/TEI%3Atext%3Axml/docx%3Aapplication%3Avnd.openxmlformats-officedocument.wordprocessingml.document?properties=$(P)
	curl  -s -F upload=@test8.xml  -F upload=@portrait.jpg -F upload=@lessing.jpg -o temp-results/result.docx http://$(SERVER):$(PORT)/ege-webservice/Conversions/TEI%3Atext%3Axml/docx%3Aapplication%3Avnd.openxmlformats-officedocument.wordprocessingml.document
	echo To HTML
	curl -s  -F upload=@test.xml  -o temp-results/result.html http://$(SERVER):$(PORT)/ege-webservice/Conversions/TEI%3Atext%3Axml/xhtml%3Aapplication%3Axhtml%2Bxml/
	curl -s  -F upload=@test.xml  -o temp-results/result2.html http://$(SERVER):$(PORT)/ege-webservice/Conversions/TEI%3Atext%3Axml/xhtml%3Aapplication%3Axhtml%2Bxml/?properties=$(P)
	echo From DocX
	curl -s  -o temp-results/result.xml -F upload=@test.docx http://$(SERVER):$(PORT)/ege-webservice/Conversions/docx%3Aapplication%3Avnd.openxmlformats-officedocument.wordprocessingml.document/TEI%3Atext%3Axml
	perl -p -i -e 's+date>.*</date+date/+' temp-results/result.xml
	diff temp-results/result.xml expected-results/result.xml
	echo ODD to RELAXNG
	curl -s  -F upload=@test.odd  -o temp-results/result.rng http://$(SERVER):$(PORT)/ege-webservice/Conversions/ODD%3Atext%3Axml/ODDC%3Atext%3Axml/relaxng%3Aapplication%3Axml-relaxng/
	perl -p -i -e 's/generated from ODD.*//' temp-results/result.rng
	diff temp-results/result.rng expected-results/result.rng
	echo Word .doc to ePub
	curl -s  -o temp-results/result2.epub -F upload=@test.doc http://$(SERVER):$(PORT)/ege-webservice/Conversions/doc%3Aapplication%3Amsword/odt%3Aapplication%3Avnd.oasis.opendocument.text/TEI%3Atext%3Axml/epub%3Aapplication%3Aepub%2Bzip/
	echo TEI To ODT
	curl -s  -F upload=@test.xml  -o temp-results/result.odt http://$(SERVER):$(PORT)/ege-webservice/Conversions/TEI%3Atext%3Axml/oo%3Aapplication%3Avnd.oasis.opendocument.text
	echo TEI XML to Text 
	curl -s  -F upload=@test.xml  -o temp-results/result.txt http://$(SERVER):$(PORT)/ege-webservice/Conversions/TEI%3Atext%3Axml/txt%3Atext%3Aplain/
	diff temp-results/result.txt expected-results/result.txt
	echo ODT to TEI
	curl -s  -F upload=@test.odt  -o temp-results/result3.xml http://$(SERVER):$(PORT)/ege-webservice/Conversions/odt%3Aapplication%3Avnd.oasis.opendocument.text/TEI%3Atext%3Axml/
	echo TEI To ODT with pictures
	curl  -s -F upload=@test8.xml  -F upload=@portrait.jpg -F upload=@lessing.jpg -o temp-results/result.odt http://$(SERVER):$(PORT)/ege-webservice/Conversions/TEI%3Atext%3Axml/odt%3Aapplication%3Avnd.oasis.opendocument.text
	echo ODD to Lite
	curl -s  -F upload=@test.odd  -o temp-results/result5.xml http://$(SERVER):$(PORT)/ege-webservice/Conversions/ODD%3Atext%3Axml/ODDC%3Atext%3Axml/TEI%3Atext%3Axml/
	echo ODD to Compiled
	curl -s  -F upload=@test.odd  -o temp-results/result4.oddc http://$(SERVER):$(PORT)/ege-webservice/Conversions/ODD%3Atext%3Axml/ODDC%3Atext%3Axml/
	echo ODD to HTML
	curl -s  -F upload=@test.odd  -o temp-results/result4.html http://$(SERVER):$(PORT)/ege-webservice/Conversions/ODD%3Atext%3Axml/ODDC%3Atext%3Axml/oddhtml%3Aapplication%3Axhtml%2Bxml/
	echo ODD to RELAXNG in French
	curl -s  -F upload=@test.odd  -o temp-results/result2.rng http://$(SERVER):$(PORT)/ege-webservice/Conversions/ODD%3Atext%3Axml/ODDC%3Atext%3Axml/relaxng%3Aapplication%3Axml-relaxng/?properties=$(L)
	echo TEI to NLM
	curl -s  -F upload=@test.xml  -o temp-results/result.nlm http://$(SERVER):$(PORT)/ege-webservice/Conversions/TEI%3Atext%3Axml/NLM%3Atext%3Axml
	curl -s  -F upload=@test.docx  -o temp-results/result2.nlm http://$(SERVER):$(PORT)/ege-webservice/Conversions/docx%3Aapplication%3Avnd.openxmlformats-officedocument.wordprocessingml.document/TEI%3Atext%3Axml/NLM%3Atext%3Axml/
	echo Docbook to TEI
	curl -s  -F upload=@test.dbk  -o temp-results/result4.xml http://$(SERVER):$(PORT)/ege-webservice/Conversions/DBK%3Atext%3Axml/TEI%3Atext%3Axml
	echo TEI to NLM
	curl -s  -F upload=@test.xml  -o temp-results/result.nlm http://$(SERVER):$(PORT)/ege-webservice/Conversions/TEI%3Atext%3Axml/NLM%3Atext%3Axml
	echo ODD to ISOSCH
	curl -s  -F upload=@test2.odd  -o temp-results/result.isosch http://$(SERVER):$(PORT)/ege-webservice/Conversions/ODD%3Atext%3Axml/ODDC%3Atext%3Axml/isosch%3Atext%3Axml/
	echo Compiled ODD to ISOSCH
	curl -s  -F upload=@test.oddc  -o temp-results/result.isosch http://$(SERVER):$(PORT)/ege-webservice/Conversions/ODDC%3Atext%3Axml/isosch%3Atext%3Axml/
	echo ODD to HTML via Lite
	curl -s  -F upload=@test.odd  -o temp-results/result3.html http://$(SERVER):$(PORT)/ege-webservice/Conversions/ODD%3Atext%3Axml/ODDC%3Atext%3Axml/TEI%3Atext%3Axml/xhtml%3Aapplication%3Axhtml%2Bxml/
	echo TITE to ePUB
	curl -s  -F upload=@test.tite  -o temp-results/test10.epub http://$(SERVER):$(PORT)/ege-webservice/Conversions/Tite%3Atext%3Axml/TEI%3Atext%3Axml/epub%3Aapplication%3Aepub%2Bzip/
	curl  -s  -o about-equations.zip -F upload=@about-equations.docx http://$(SERVER):$(PORT)/ege-webservice/Conversions/docx%3Aapplication%3Avnd.openxmlformats-officedocument.wordprocessingml.document/TEI%3Atext%3Axml/xhtml%3Aapplication%3Axhtml%2Bxml/
	echo P4 to P5
	curl -s  -F upload=@testp4.xml  -o temp-results/result.xml http://$(SERVER):$(PORT)/ege-webservice/Conversions/P4%3Atext%3Axml/TEI%3Atext%3Axml/
	echo xls to TEI XML
	curl -s  -F upload=@test.xls  -o temp-results/result2.xml http://$(SERVER):$(PORT)/ege-webservice/Conversions/xls%3Aapplication%3Avnd.ms-excel/csv%3Atext%3Acsv/TEI%3Atext%3Axml/
	echo xls to PDF
	curl -s  -F upload=@test.xls  -o temp-results/result2.pdf http://$(SERVER):$(PORT)/ege-webservice/Conversions/xls%3Aapplication%3Avnd.ms-excel/pdf%3Aapplication%3Apdf/

x:
	curl  -s -F upload=@test8.xml  -F upload=@portrait.jpg -F upload=@lessing.jpg -o temp-results/result.docx http://$(SERVER):$(PORT)/ege-webservice/Conversions/ODD%3Atext%3Axml/ODDC%3Atext%3Axml/TEI%3Atext%3Axml/docx%3Aapplication%3Avnd.openxmlformats-officedocument.wordprocessingml.document/


ole2:
	curl -s  -o about-equations.zip -F upload=@about-equations.docx http://$(SERVER):$(PORT)/ege-webservice/Conversions/docx%3Aapplication%3Avnd.openxmlformats-officedocument.wordprocessingml.document/TEI%3Atext%3Axml/

ole3:
	curl -s  -o about-equations.zip -F upload=@about-equations.xml -F upload=@media.zip http://$(SERVER):$(PORT)/ege-webservice/Conversions/TEI%3Atext%3Axml/xhtml%3Aapplication%3Axhtml%2Bxml/
clean:
	rm -rf temp-results
	rm -f about-equations.html  about-equations.html  about-equations.zip about-equations.xml
