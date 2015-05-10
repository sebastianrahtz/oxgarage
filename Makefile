PREFIX=/usr/local
SERVER=localhost
SUDO=sudo
PORT=8080
APACHE=apache-tomcat-7.0.52
LIB=$(PREFIX)/$(APACHE)/webapps/ege-webservice/WEB-INF/lib/

default: build

install:
	$(SUDO) $(PREFIX)/$(APACHE)/bin/shutdown.sh
	sleep 5
	$(SUDO) rm -rf $(PREFIX)/$(APACHE)/webapps/ege-webservice $(PREFIX)/$(APACHE)/webapps/ege-webclient
	$(SUDO) cp ege-webclient/target/ege-webclient.war $(PREFIX)/$(APACHE)/webapps/
	$(SUDO) cp ege-webservice/target/ege-webservice.war $(PREFIX)/$(APACHE)/webapps/
	$(SUDO) $(PREFIX)/$(APACHE)/bin/startup.sh
	sleep 5

build:
	@echo Maven build
	mvn install

sitedoc:
	mvn site
	for i in */target; do mv $$i/site target/site/`echo $$i | sed 's/.target//'`;done

debversion:
	sh ./mydch debian-tei-oxgarage/debian/changelog

deb: debversion
	@echo BUILD Make Debian packages
	rm -f tei*oxgarage*_*deb
	rm -f tei*oxgarage*_*changes
	rm -f tei*oxgarage*_*build
	(cd debian-tei-oxgarage; debclean;debuild --no-lintian  -nc -b -i.svn -I.svn -uc -us)

setup:
	mvn install:install-file -DgroupId=jpf-tools -DartifactId=jpf-tools -Dversion=1.5.1 -Dpackaging=jar -Dfile=jpf-tools.jar
	mvn install:install-file -DgroupId=com.artofsolving -DartifactId=jodconverter -Dversion=3.0-beta-4 -Dpackaging=jar -Dfile=jod-lib/jodconverter-core-3.0-beta-4.jar
	mvn install:install-file -DgroupId=com.sun.star -DartifactId=jurt  -Dversion=3.2.1 -Dpackaging=jar -Dfile=jod-lib/jurt-3.2.1.jar
	mvn install:install-file -DgroupId=com.sun.star -DartifactId=juh   -Dversion=3.2.1 -Dpackaging=jar -Dfile=jod-lib/juh-3.2.1.jar
	mvn install:install-file -DgroupId=com.sun.star -DartifactId=unoil -Dversion=3.2.1 -Dpackaging=jar -Dfile=jod-lib/unoil-3.2.1.jar
	mvn install:install-file -DgroupId=com.sun.star -DartifactId=ridl  -Dversion=3.2.1 -Dpackaging=jar -Dfile=jod-lib/ridl-3.2.1.jar
	mvn install:install-file -DgroupId=org.apache.commons.cli -DartifactId=commons-cli -Dversion=1.1 -Dpackaging=jar -Dfile=jod-lib/commons-cli-1.1.jar

test:
	(cd Tests;make)	

clean:
	mvn clean
	rm -f tei*oxgarage*_*deb
	rm -f tei*oxgarage*_*changes
	rm -f tei*oxgarage*_*build
	-rm -rf debian-tei-oxgarage/debian/tei-oxgarage
	(cd Tests; make clean)
