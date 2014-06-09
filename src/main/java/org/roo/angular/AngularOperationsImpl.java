package org.roo.angular;

import static org.springframework.roo.classpath.customdata.CustomDataKeys.PERSISTENT_TYPE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.plural.PluralMetadata;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypePersistenceMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
import org.springframework.roo.addon.web.mvc.jsp.JspMetadata;
import org.springframework.roo.addon.web.mvc.jsp.JspViewManager;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.customdata.tagkeys.MemberHoldingTypeDetailsCustomDataKey;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.operations.AbstractOperations;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.osgi.OSGiUtils;
import org.springframework.roo.support.util.FileUtils;

import static org.springframework.roo.model.RooJavaType.ROO_JAVA_BEAN;
import static org.springframework.roo.model.RooJavaType.ROO_JSF_MANAGED_BEAN;
import static org.springframework.roo.model.RooJavaType.ROO_WEB_SCAFFOLD;

/**
 * Implementation of {@link AngularOperations} interface.
 * 
 * @since 1.1.1
 */
@Component
@Service
public class AngularOperationsImpl extends AbstractOperations implements AngularOperations {

	private static final char SEPARATOR = File.separatorChar;


	/**
	 * Get a reference to the ProjectOperations from the underlying OSGi
	 * container. Make sure you are referencing the Roo bundle which contains
	 * this service in your add-on pom.xml.
	 */
	@Reference
	private ProjectOperations projectOperations;

	@Reference
	private WebMetadataService webMetadataService;

	@Reference
	private TypeLocationService typeLocationService;

	@Reference
	private MetadataService metadataService;

	@Reference
	private MetadataDependencyRegistry dependencyRegistry;

	@Reference
	private org.roo.angular.roundtrip.XmlRoundTripFileManager xmlRoundTripFileManager;
	
	@Reference private PathResolver pathResolver;

	private Logger log = Logger.getLogger(getClass().getName());

	private final Map<JavaType, String> formBackingObjectTypesToLocalMids = new HashMap<JavaType, String>();

	/** {@inheritDoc} */
	public boolean isInstallTagsCommandAvailable() {
		return projectOperations.isFocusedProjectAvailable()
				&& fileManager.exists(projectOperations.getPathResolver()
						.getFocusedIdentifier(Path.SRC_MAIN_WEBAPP,
								"WEB-INF" + SEPARATOR + "tags"));
	}

	/** {@inheritDoc} */
	public String getProperty(String propertyName) {
		Validate.notBlank(propertyName, "Property name required");
		return System.getProperty(propertyName);
	}

	public void installAngular() {
		final Set<ClassOrInterfaceTypeDetails> controllers = typeLocationService
				.findClassesOrInterfaceDetailsWithAnnotation(ROO_WEB_SCAFFOLD);
		List<String> list = new  ArrayList<String>();
		final String destinationBase = projectOperations
				.getPathResolver().getRoot()
				+ "\\"
				+ Path.SRC_MAIN_WEBAPP.getDefaultLocation() + "\\";
		for (ClassOrInterfaceTypeDetails controller : controllers) {		
		
			log.info("Controller  getDeclaredByMetadataId "
					+ controller.getDeclaredByMetadataId());
			log.info("Controller  class " + controller.getClass());
			// MID:org.springframework.roo.addon.web.mvc.jsp.JspMetadata#SRC_MAIN_JAVA?com.springsource.petclinic.web.PetController
			// MID:org.springframework.roo.classpath.PhysicalTypeIdentifier#SRC_MAIN_JAVA?com.springsource.petclinic.web.OwnerController
			String jspMetadataId = controller.getDeclaredByMetadataId();
			log.info("getJavaType "
					+ PhysicalTypeIdentifierNamingUtils
							.getJavaType(jspMetadataId));
			// log.info("getPath "+PhysicalTypeIdentifierNamingUtils.getPath(jspMetadataId));
			String javaType = PhysicalTypeIdentifierNamingUtils.getJavaType(
					jspMetadataId).toString();
			jspMetadataId = "MID:org.springframework.roo.addon.web.mvc.jsp.JspMetadata#SRC_MAIN_JAVA?"
					+ javaType;
			log.info("controller.getName() "+controller.getName());
					
			final String webScaffoldMetadataKey = WebScaffoldMetadata
					.createIdentifier(JspMetadata.getJavaType(jspMetadataId),
							JspMetadata.getPath(jspMetadataId));
			final WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) metadataService
					.get(webScaffoldMetadataKey);
		
			String path = webScaffoldMetadata.getAnnotationValues().getFormBackingObject().getSimpleTypeName().toLowerCase();
			list.add(path);
			
			log.info("webScaffoldMetadata.getAnnotationValues().getPath() "+webScaffoldMetadata.getAnnotationValues().getPath());
					
			if (webScaffoldMetadata == null || !webScaffoldMetadata.isValid()) {
				// Can't get the corresponding scaffold, so we certainly don't need to manage any JSPs at this time
				return;
			}

			final JavaType formBackingType = webScaffoldMetadata
					.getAnnotationValues().getFormBackingObject();
			final MemberDetails memberDetails = webMetadataService
					.getMemberDetails(formBackingType);
			final JavaTypeMetadataDetails formBackingTypeMetadataDetails = webMetadataService
					.getJavaTypeMetadataDetails(formBackingType, memberDetails,
							jspMetadataId);

			Validate.notNull(formBackingTypeMetadataDetails,
					"Unable to obtain metadata for type %s",
					formBackingType.getFullyQualifiedTypeName());
			formBackingObjectTypesToLocalMids.put(formBackingType,
					jspMetadataId);

			final SortedMap<JavaType, JavaTypeMetadataDetails> relatedTypeMd = webMetadataService
					.getRelatedApplicationTypeMetadata(formBackingType,
							memberDetails, jspMetadataId);
			final List<FieldMetadata> eligibleFields = webMetadataService
					.getScaffoldEligibleFieldMetadata(formBackingType,
							memberDetails, jspMetadataId);
			// if (eligibleFields.isEmpty()
			// && formBackingTypePersistenceMetadata.getRooIdentifierFields()
			// .isEmpty()) {
			// return;
			// }

			final AngularViewManager viewManager = new AngularViewManager(
					eligibleFields, webScaffoldMetadata.getAnnotationValues(),
					relatedTypeMd,webScaffoldMetadata.getAnnotationValues().getPath());
			log.info("zzzzzzz");

			String name = projectOperations.getFocusedProjectName();

			// Make the holding directory for this controller
			String destinationDirectory = destinationBase +"views\\"+ path;
			log.info("DestinationDirectory: " + destinationDirectory);
			if (!fileManager.exists(destinationDirectory)) {
				fileManager.createDirectory(destinationDirectory);
			} else {
				final File file = new File(destinationDirectory);
				Validate.isTrue(file.isDirectory(),
						"%s is a file, when a directory was expected",
						destinationDirectory);
			}

			final String createPath = destinationDirectory + "/create.html";
			xmlRoundTripFileManager.writeToDiskIfNecessary(createPath,
					viewManager.getCreateDocument());

			
		}
		final String title = projectOperations.getProjectName(projectOperations.getFocusedModuleName());
		final String menuPath = destinationBase + "/ra.html";
		xmlRoundTripFileManager.writeToDiskIfNecessary(menuPath,
				new HtmlBuilder().buildPage(list,title));
	}
	
	 public void installCommonViewArtefacts() {
		 final String moduleName =  projectOperations.getFocusedModuleName();
		 final LogicalPath webappPath = Path.SRC_MAIN_WEBAPP
	                .getModulePathId(moduleName);
		 log.info("installCommonViewArtefact "+pathResolver.getIdentifier(webappPath, "stylesheets"));		
		 String path = FileUtils.getPath(getClass(), "stylesheets/*.*");
		 log.info("path "+  path);
		 final Iterable<URL> urls = OSGiUtils.findEntriesByPattern(
	                context.getBundleContext(), path);
		 for(URL  url : urls){
			 log.info("url "+ url);
		 }
		 copyDirectoryContentsRelative("stylesheets/**/*.*",
	              pathResolver.getIdentifier(webappPath, "/"), false,"stylesheets/");
		 copyDirectoryContentsRelative("js/**/*.*",
	              pathResolver.getIdentifier(webappPath, "/"), false,"js/");
		 copyDirectoryContents("views/*.*",
	              pathResolver.getIdentifier(webappPath, "views"), false);
		 copyDirectoryContents("bower.json",
	              pathResolver.getIdentifier(webappPath, ""), false);

	 }
	

	 public boolean isAngularInstallationPossible() {
			return projectOperations.isFocusedProjectAvailable()
					&& projectOperations
							.isFeatureInstalledInFocusedModule(FeatureNames.MVC);
		}

	  
	/**
	 * A private method which illustrates how to reference and manipulate
	 * resources in the target project as well as the bundle classpath.
	 * 
	 * @param path
	 * @param fileName
	 */
	private void createOrReplaceFile(String path, String fileName) {
		String targetFile = path + SEPARATOR + fileName;

		// Use MutableFile in combination with FileManager to take advantage of
		// Roo's transactional file handling which
		// offers automatic rollback if an exception occurs
		MutableFile mutableFile = fileManager.exists(targetFile) ? fileManager
				.updateFile(targetFile) : fileManager.createFile(targetFile);
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			// Use FileUtils to open an InputStream to a resource located in
			// your bundle
			inputStream = FileUtils.getInputStream(getClass(), fileName);
			outputStream = mutableFile.getOutputStream();
			IOUtils.copy(inputStream, outputStream);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
		}
	}
	
	 /**
	  *
	  * THIS IA A HACK BECAUSE 'copyDirectoryContents' REMOVED SUBDIRECTORIES
	  *
     * This method will copy the contents of a directory to another if the
     * resource does not already exist in the target directory
     * 
     * @param sourceAntPath the source path
     * @param targetDirectory the target directory
     */
    public void copyDirectoryContentsRelative(final String sourceAntPath,
            String targetDirectory, final boolean replace, final String relative
            ) {
        Validate.notBlank(sourceAntPath, "Source path required");
        Validate.notBlank(targetDirectory, "Target directory required");

        if (!targetDirectory.endsWith("/")) {
            targetDirectory += "/";
        }

        if (!fileManager.exists(targetDirectory)) {
            fileManager.createDirectory(targetDirectory);
        }

        final String path = FileUtils.getPath(getClass(), sourceAntPath);
        final Iterable<URL> urls = OSGiUtils.findEntriesByPattern(
                context.getBundleContext(), path);
        Validate.notNull(urls,
                "Could not search bundles for resources for Ant Path '%s'",
                path);
        for (final URL url : urls) {
            String fileName;
            if (relative != null){
            	 log.info("targetDirectory "+  targetDirectory);
            	 log.info("path "+  url.getPath());
            	 log.info("relative "+  relative);
            	fileName = url.getPath().substring(
                        url.getPath().lastIndexOf(relative));
            	 log.info("fileName "+  fileName);
            }else{
            	fileName = url.getPath().substring(
                    url.getPath().lastIndexOf("/") + 1);
            }
            if (replace) {
                try {
                    String contents = IOUtils.toString(url);
                    fileManager.createOrUpdateTextFileIfRequired(
                            targetDirectory + fileName, contents, false);
                }
                catch (final Exception e) {
                    throw new IllegalStateException(e);
                }
            }
            else {
                if (!fileManager.exists(targetDirectory + fileName)) {
                    InputStream inputStream = null;
                    OutputStream outputStream = null;
                    try {
                        inputStream = url.openStream();
                        outputStream = fileManager.createFile(
                                targetDirectory + fileName).getOutputStream();
                        IOUtils.copy(inputStream, outputStream);
                    }
                    catch (final Exception e) {
                        throw new IllegalStateException(
                                "Encountered an error during copying of resources for the add-on.",
                                e);
                    }
                    finally {
                        IOUtils.closeQuietly(inputStream);
                        IOUtils.closeQuietly(outputStream);
                    }
                }
            }
        }
    }

}