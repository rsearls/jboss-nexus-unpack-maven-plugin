/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.javadoc;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.http.StatusLine;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

/**
 *
 */
@Mojo( name = "unpack", defaultPhase = LifecyclePhase.DEPLOY, requiresOnline=true )
public class UnpackMojo extends AbstractMojo {

   @Parameter(defaultValue = "${session}", readonly = true, required = true)
   private MavenSession mavenSession;

   @Parameter(property = "project.build.directory")
   private File outputDirectory;

   /**
    * (Optional) Subdirectory path to append to unpack repo URL
    */
   @Parameter(property = "nexusSubdirectory", readonly = true)
   private String nexusSubdirectory;

   /**
    * Repository Id declared in your settings.xml file that defines the URL of the
    * Jboss Nexus repo defined for unpacked archives as well as your repository
    * credentials.
    */
   @Parameter(property = "upackRepositoryId", readonly = true, required = true)
   private String upackRepositoryId;


   public void execute() throws MojoExecutionException {

      MavenProject mProject = mavenSession.getCurrentProject();
      Settings settings = mavenSession.getSettings();

      Server server = settings.getServer(upackRepositoryId);
      if (server == null) {
         throw new MojoExecutionException("No server definition found in settings.xml for upackRepositoryId: " + upackRepositoryId);
      }

      Repository repository = lookupRepository(settings, upackRepositoryId);
      if (repository == null) {
         throw new MojoExecutionException("No repository defintion found in settings.xml for upackRepositoryId: " + upackRepositoryId);
      }

      String fullUrl = generateFullNexusPath(mProject, repository.getUrl(), nexusSubdirectory);
      if (outputDirectory.exists() && outputDirectory.isDirectory()) {

         File [] fList = outputDirectory.listFiles(new JavaDocFilenameFilter());
         if(fList == null) {
            throw new MojoExecutionException(" ERROR  No files found in directory:  "
               + outputDirectory.getAbsolutePath());
         } else {
            for(int i = 0; i < fList.length; i++) {
               sendPost(fullUrl, fList[i], server.getUsername(), server.getPassword());
            }
         }

      } else {
         throw new MojoExecutionException(" ERROR  directory not found:  "
            + outputDirectory.getAbsolutePath());
      }
   }

   /**
    * Retrieve the repository definition.  It is expected that the repo URL is
    * declared in this xml block.
    *
    * @param settings
    * @param upackRepositoryId
    * @return
    */
   private Repository lookupRepository(Settings settings, String upackRepositoryId) {
      List<Profile> profileList = settings.getProfiles();
      for(Profile p : profileList) {
         List<Repository> repoList = p.getRepositories();
         for (Repository r : repoList) {
            if (r.getId().equals(upackRepositoryId) ) {
               return r;
            }
         }
      }
      return null;
   }

   /**
    * Generate the full repo URL into which the archive will be unpacked.
    * Formate  [REPO_URL]/{OPTIONAL Subdir}/{group id}/{archive id}/{version}
    *
    * @param mProject the current maven project
    * @param upackRepositoryId repository id according to the maven settings.xml rules
    * @param nexusSubdirectory OPTIONAL subdir
    * @return  generated URL pat
    */
   private String generateFullNexusPath(MavenProject mProject, String upackRepositoryId,
                                        String nexusSubdirectory) {
      StringBuilder sb = new StringBuilder();
      sb.append(upackRepositoryId);

      if(!upackRepositoryId.endsWith("/")) {
         sb.append("/");
      }

      if (nexusSubdirectory != null && !nexusSubdirectory.trim().isEmpty()) {
         String subDir = nexusSubdirectory.trim();
         sb.append(subDir);
         if (!subDir.endsWith("/")) {
            sb.append("/");
         }
      }

      Artifact artifact = mProject.getArtifact();
      sb.append(artifact.getGroupId() + "/" + artifact.getArtifactId() + "/" + artifact.getVersion());

      return sb.toString();
   }

   /**
    * POST the archive and report results.
    *
    * @param fullUrl full path to which to unpack archive
    * @param file    archive to post
    * @param username  credential
    * @param password  credential
    */
   private void sendPost(String fullUrl, File file, String username, String password) {

      PostFile postFile = new PostFile(fullUrl, file,
         (username == null)? "" : username,
         (password == null)? "" : password);

      try {
         StatusLine statusLine = postFile.post();
         if (statusLine.getStatusCode() == 201) {
            getLog().info(file.getName() + " unpacked to URL " + fullUrl);
         }
         getLog().info("status: " + statusLine.toString());
      } catch (IOException e) {
         getLog().info("IOException: " + e.toString());
         e.printStackTrace();
      }
   }
}
