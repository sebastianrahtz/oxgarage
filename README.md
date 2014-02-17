OxGarage
========

OxGarage is a web, and RESTful, service to manage the transformation of documents between a variety of formats. The majority of transformations use Text Encoding Initiative XML as a pivot format.

OxGarage is based on the Enrich Garage Engine (https://sourceforge.net/projects/enrich-ege/)
developed by Poznan Supercomputing and Networking Center and Oxford University Computing Services for the EU-funded ENRICH project 

Installing
--------

Packages are available from the TEI's continuous integration (CI) server at http://tei.it.ox.ac.uk/jenkins/job/OxGarage/

 * If you  have a Debian or Ubuntu system, you can subscribe to TEI-related packages at http://tei.it.ox.ac.uk/teideb/, or download the .deb file directly from the CI server; these packages have a dependency on Apache Tomcat and the TEI (packages tei-xsl, tei-p5-source, and tomcat6)
 * If you have a running Tomcat (or similar container), you can download  two WAR files from the CI server and install them in the normal way. in this case, you will need to do some configuration manually
 
 1.   copy the file  `ege-webservice/WEB-INF/lib/oxgarage.properties` to `/etc/oxgarage.properties`
 2.   create a directorory `/var/cache/oxgarage` and copy the file `log4j.xml` to there
 3.   make the directory owned by the Tomcat user, so that it can create files there: eg `chown -R tomcat6:tomcat6 /var/cache/oxgarage`
 4.   edit the file `webapps/ege-webclient/WEB-INF/web.xml` so that it has the hostname of the server set. eg 
    perl -p -i -e "s/localhost/`hostname -f`/" /var/lib/tomcat6/webapps/ege-webclient/WEB-INF/web.xml
 5.  make sure the TEI stylesheets and source are installed at `/usr/share/xml/tei` using the Debian file hierarchy standard; the distribution file at https://sourceforge.net/projects/tei/files/Stylesheets/ is in the right layout.

You'll probably need to restart your servlet container to make sure these changes take effect.

Edit the file `oxgarage.properties` if you need to change the names of directories.

Note also that OxGarage needs a headless OpenOffice available to do some of its work. The properties file specifies that this is
at `/usr/lib/openoffice/`, but this can be changed if needed

Check the working system by visiting /ege-webclient/ on your Tomcat (or similar) server, and trying an example transformation. You can check the RESTful web server using eg Curl. For example, to convert a TEI XML file to Word format, you might do

    curl -s  -o test.docx -F upload=@test.xml http://localhost:8080/ege-webservice/Conversions/TEI%3Atext%3Axml/docx%3Aapplication%3Avnd.openxmlformats-officedocument.wordprocessingml.document

Building
------

OxGarage is written in Java and built using Maven, so the command

    mvn install

will normally do the job, if you have Maven, Java etc installed. The WAR files will be created in the `target` directory.

There are a variety of packages which may not be available in Maven repositories, so you may first need to install them locally, using these commands:

    mvn install:install-file -DgroupId=jpf-tools -DartifactId=jpf-tools -Dversion=1.5.1 -Dpackaging=jar -Dfile=jpf-tools.jar
    mvn install:install-file -DgroupId=com.artofsolving -DartifactId=jodconverter -Dversion=3.0-beta-4 -Dpackaging=jar -Dfile=jod-lib/jodconverter-core-3.0-beta-4.jar
    mvn install:install-file -DgroupId=com.sun.star -DartifactId=jurt  -Dversion=3.2.1 -Dpackaging=jar -Dfile=jod-lib/jurt-3.2.1.jar
    mvn install:install-file -DgroupId=com.sun.star -DartifactId=juh   -Dversion=3.2.1 -Dpackaging=jar -Dfile=jod-lib/juh-3.2.1.jar
    mvn install:install-file -DgroupId=com.sun.star -DartifactId=unoil -Dversion=3.2.1 -Dpackaging=jar -Dfile=jod-lib/unoil-3.2.1.jar
    mvn install:install-file -DgroupId=com.sun.star -DartifactId=ridl  -Dversion=3.2.1 -Dpackaging=jar -Dfile=jod-lib/ridl-3.2.1.jar
    mvn install:install-file -DgroupId=org.apache.commons.cli -DartifactId=commons-cli -Dversion=1.1 -Dpackaging=jar -Dfile=jod-lib/commons-cli-1.1.jar
