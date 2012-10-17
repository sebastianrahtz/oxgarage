PREFIX=/usr/local
SERVER=localhost
SUDO=sudo
PORT=8080
APACHE=apache-tomcat-7.0.32
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

debversion:
	sh ./mydch debian-tei-oxgarage/debian/changelog

deb: debversion
	@echo BUILD Make Debian packages
	rm -f tei*oxgarage*_*deb
	rm -f tei*oxgarage*_*changes
	rm -f tei*oxgarage*_*build
	(cd debian-tei-oxgarage; debclean;debuild --no-lintian  -nc -b -i.svn -I.svn -uc -us)

setup:
	mvn install:install-file -DgroupId=jpf -DartifactId=jpf -Dversion=1.5.1 -Dpackaging=jar -Dfile=jpf-1.5.1.jar 
	mvn install:install-file -DgroupId=jpf-tools -DartifactId=jpf-tools -Dversion=1.5.1 -Dpackaging=jar -Dfile=jpf-tools.jar
	mvn install:install-file -DgroupId=org.tei.vesta -DartifactId=Vesta -Dversion=1.0.8 -Dpackaging=jar -Dfile=Vesta-1.0.8.jar 
	mvn install:install-file -DgroupId=org.apache.commons.compress -DartifactId=commons-compress -Dversion=20050911 -Dpackaging=jar -Dfile=commons-compress-20050911.jar 
	mvn install:install-file -DgroupId=net.sf.saxon -DartifactId=saxon9 -Dversion=9.2.0 -Dpackaging=jar -Dfile=saxon9he.jar 
	mvn install:install-file -DgroupId=org.apache.commons.io -DartifactId=commons-io -Dversion=1.4 -Dpackaging=jar -Dfile=commons-io-1.4.jar
	mvn install:install-file -DgroupId=com.artofsolving -DartifactId=jodconverter -Dversion=3.0-beta-3 -Dpackaging=jar -Dfile=jod-lib/jodconverter-core-3.0-beta-3.jar
	mvn install:install-file -DgroupId=com.sun.star -DartifactId=jurt -Dversion=3.1.0 -Dpackaging=jar -Dfile=jod-lib/jurt-3.1.0.jar
	mvn install:install-file -DgroupId=com.sun.star -DartifactId=juh -Dversion=3.1.0 -Dpackaging=jar -Dfile=jod-lib/juh-3.1.0.jar
	mvn install:install-file -DgroupId=com.sun.star -DartifactId=unoil -Dversion=3.1.0 -Dpackaging=jar -Dfile=jod-lib/unoil-3.1.0.jar
	mvn install:install-file -DgroupId=com.sun.star -DartifactId=ridl -Dversion=3.1.0 -Dpackaging=jar -Dfile=jod-lib/ridl-3.1.0.jar
	mvn install:install-file -DgroupId=org.apache.commons.cli -DartifactId=commons-cli -Dversion=1.1 -Dpackaging=jar -Dfile=jod-lib/commons-cli-1.1.jar

test:
	(cd Tests;make)	

clean:
	mvn clean
	rm -f tei*oxgarage*_*deb
	rm -f tei*oxgarage*_*changes
	rm -f tei*oxgarage*_*build
	-rm -rf debian-tei-oxgarage/debian/tei-oxgarage
	-rm -rf Tests/temp-results
