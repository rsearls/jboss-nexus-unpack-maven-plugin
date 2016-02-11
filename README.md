# jboss-nexus-unpack-maven-plugin
Utility that uploads and unpacks the project's *-javadoc.jar files to RedHat's corporate nexus repository using Sonatype's nexus-unpack-plugin.

## Plugin configuration parameters
   parameter name | description | usage
   ---------------|-------------|------
   upackRepositoryId | id of repository definition containing nexus URL | required
   nexusSubdirectory | directory path to add to nexus URL | optional

## Dependencies
* A named repository definition to RedHat's corporate nexus released javadoc URL is requried.
  It must reside in the user's settings.xml file.
* A named server definition containing the user's nexus credentials and an id that matches the
  repository id above is required.  It must reside in the user's settings.xml file.
* jboss-nexus-unpack-maven-plugin's configuration parameter, **upackRepositoryId** must be set to the
  id defined for the nexus repository
  
  
##Example


####Settings.xml additions

    <profiles>
       <profile>
          <repositories>
             <repository>
                <id>jboss-nexus-unpack</id>
                <name>jboss nexus unpack archive</name>
                <url>http://<REDHAT-CORP-NEXUS-URL></url>
                <layout>default</layout>
                <releases>
                   <enabled>true</enabled>
                </releases>
                <snapshots>
                   <enabled>false</enabled>
                   <updatePolicy>never</updatePolicy>
                </snapshots>
             </repository>
          </repositories>
       </profile>
    </profiles>


    <servers>
       <server>
          <id>jboss-nexus-unpack</id>  <!-- id MUST match <repository> id -->
          <username>myUsername</username>
          <password>myPassword</password>
       </server>
    </servers>

------
####pom.xml plugin settings

    <build>
       <plugins>
          <plugin>
             <groupId>org.jboss.maven.plugins</groupId>
             <artifactId>jboss-nexus-unpack-maven-plugin</artifactId>
             <version>1.0-SNAPSHOT</version>
             <configuration>
                <upackRepositoryId>jboss-nexus-unpack</upackRepositoryId> <!-- required, MUST match <repository> id -->
                <nexusSubdirectory>example/extended/dirs</nexusSubdirectory>
             </configuration>
          </plugin>
       </plugins>
    </build>
 ------
  