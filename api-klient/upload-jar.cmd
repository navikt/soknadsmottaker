mvn install:install-file -Dfile= target/soknadsmottaker-api-klient-1-sources.jar -DgroupId=no.nav.soknad.arkivering.api     -DartifactId=soknadsmottaker-api-klient -Dversion=1 -Dpackaging=jar
	
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=pom.xml