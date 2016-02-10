package org.jboss.javadoc;

import java.io.FilenameFilter;
import java.io.File;

/**
 * Identify javadoc archives that uses the standard naming convention.
 *
 * User: rsearls
 * Date: 2/2/16
 */
public class JavaDocFilenameFilter implements FilenameFilter {
   public boolean accept(File dir, String name) {
      if(name.endsWith("-javadoc.jar")) {
         return true;
      }
      return false;
   }
}
