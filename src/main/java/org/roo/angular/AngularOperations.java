package org.roo.angular;

import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.LogicalPath;

/**
 * Interface of commands that are available via the Roo shell.
 *
 * @since 1.1.1
 */
public interface AngularOperations {

    /**
     * Indicate of the install tags command should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isInstallTagsCommandAvailable();

    /**
     * @param propertyName to obtain (required)
     * @return a message that will ultimately be displayed on the shell
     */
    String getProperty(String propertyName);
    
    /**
     * Install Common view artifacts.
     */
    void installCommonViewArtefacts();
    
    void installAngular();
    
    public boolean isAngularInstallationPossible();
   
}