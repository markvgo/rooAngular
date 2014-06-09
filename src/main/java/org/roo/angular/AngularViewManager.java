package org.roo.angular;

import static org.springframework.roo.model.JdkJavaType.CALENDAR;
import static org.springframework.roo.model.JdkJavaType.DATE;
import static org.springframework.roo.model.Jsr303JavaType.SIZE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Reference;
import org.springframework.roo.addon.web.mvc.controller.details.FinderMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypeMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypePersistenceMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.addon.web.mvc.jsp.roundtrip.XmlRoundTripFileManager;
import org.springframework.roo.classpath.customdata.CustomDataKeys;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JpaJavaType;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlRoundTripUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AngularViewManager implements ViewManager {

	private static final String CREATED = "created";
	private static final JavaSymbolName VALUE = new JavaSymbolName("value");
	private final String entityName;
	private final List<FieldMetadata> fields;
	private final JavaType formBackingType;
	private final JavaTypeMetadataDetails formBackingTypeMetadata;
	private final JavaTypePersistenceMetadataDetails formBackingTypePersistenceMetadata;
	private final Map<JavaType, JavaTypeMetadataDetails> relatedDomainTypes;
	private final WebScaffoldAnnotationValues webScaffoldAnnotationValues;

	private Logger log = Logger.getLogger(getClass().getName());
	
	private String requestPath;

	
	@Reference
	private WebMetadataService webMetadataService;
	@Reference
	private MetadataService metadataService;
	@Reference
	private XmlRoundTripFileManager xmlRoundTripFileManager;

	public AngularViewManager(final List<FieldMetadata> fields,
			final WebScaffoldAnnotationValues webScaffoldAnnotationValues,
			final Map<JavaType, JavaTypeMetadataDetails> relatedDomainTypes,
			final String requestPath) {
		Validate.notNull(fields, "List of fields required");
		Validate.notNull(webScaffoldAnnotationValues,
				"Web scaffold annotation values required");
		Validate.notNull(relatedDomainTypes, "Related domain types required");
		this.fields = Collections.unmodifiableList(fields);
		this.webScaffoldAnnotationValues = webScaffoldAnnotationValues;
		formBackingType = webScaffoldAnnotationValues.getFormBackingObject();
		this.relatedDomainTypes = relatedDomainTypes;		
		this.requestPath  = requestPath;
		entityName = JavaSymbolName.getReservedWordSafeName(formBackingType)
				.getSymbolName();
		formBackingTypeMetadata = relatedDomainTypes.get(formBackingType);
		Validate.notNull(formBackingTypeMetadata,
				"Form backing type metadata required");
		formBackingTypePersistenceMetadata = formBackingTypeMetadata
				.getPersistenceDetails();
		Validate.notNull(formBackingTypePersistenceMetadata,
				"Persistence metadata required for form backing type");
		Validate.notNull(
				webScaffoldAnnotationValues.getPath(),
				"Path is not specified in the @RooWebScaffold annotation for '%s'",
				webScaffoldAnnotationValues.getGovernorTypeDetails().getName());
	}

	public Document getCreateDocument() {
		log.info("****in getCreateDocument");
		final DocumentBuilder builder = XmlUtils.getDocumentBuilder();
		final Document document = builder.newDocument();

		// Add document namespaces
		final Element formCreate = (Element) document
				.appendChild(new XmlElementBuilder("accordion", document)
						.addAttribute("close-others", "oneAtATime").build());
		formCreate.appendChild(new XmlElementBuilder("accordion-group",
				document).addAttribute("heading", "Create")
				.addAttribute("is-disabled", "false").build());

		final List<FieldMetadata> formFields = new ArrayList<FieldMetadata>();
		final List<FieldMetadata> fieldCopy = new ArrayList<FieldMetadata>(
				fields);

		// Handle Roo identifiers
		if (!formBackingTypePersistenceMetadata.getRooIdentifierFields()
				.isEmpty()) {
			final String identifierFieldName = formBackingTypePersistenceMetadata
					.getIdentifierField().getFieldName().getSymbolName();
			formCreate.setAttribute("compositePkField", identifierFieldName);
			for (final FieldMetadata embeddedField : formBackingTypePersistenceMetadata
					.getRooIdentifierFields()) {
				final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
						embeddedField);
				fieldBuilder
						.setFieldName(new JavaSymbolName(identifierFieldName
								+ "."
								+ embeddedField.getFieldName().getSymbolName()));
				for (int i = 0; i < fieldCopy.size(); i++) {
					// Make sure form fields are not presented twice.
					if (fieldCopy.get(i).getFieldName()
							.equals(embeddedField.getFieldName())) {
						fieldCopy.remove(i);
						break;
					}
				}
				formFields.add(fieldBuilder.build());
			}
		}
		formFields.addAll(fieldCopy);

		// If identifier manually assigned, show it in creation
		if (formBackingTypePersistenceMetadata.getIdentifierField()
				.getAnnotation(JpaJavaType.GENERATED_VALUE) == null) {

			formFields.add(formBackingTypePersistenceMetadata
					.getIdentifierField());
		}
		log.info("****start");
		createFieldsForCreateAndUpdate(formFields, document, formCreate, true);
		formCreate.setAttribute("z",
				XmlRoundTripUtils.calculateUniqueKeyFor(formCreate));
		log.info("****finish");
		final Element dependency = new XmlElementBuilder("form:dependency",
				document)
				.addAttribute(
						"id",
						XmlUtils.convertId("d:"
								+ formBackingType.getFullyQualifiedTypeName()))
				.addAttribute("render", "${not empty dependencies}")
				.addAttribute("dependencies", "${dependencies}").build();
		dependency.setAttribute("z",
				XmlRoundTripUtils.calculateUniqueKeyFor(dependency));

		// div.appendChild(formCreate);
		// div.appendChild(dependency);

		return document;
	}

	private void createFieldsForCreateAndUpdate(
			final List<FieldMetadata> formFields, final Document document,
			final Element root, final boolean isCreate) {
		int j = 0;
		Element divElement = null;
		Element  createElement = document.createElement("create");
		createElement.setAttribute("ng-attr-mapping", requestPath);
		createElement.setAttribute("ng-attr-entity", entityName);
		root.appendChild(createElement);	
		for (final FieldMetadata field : formFields) {
			if (j % 2 == 0 ){
			// divElement.appendChild(fieldElement);
				divElement = document.createElement("div");
				divElement.setAttribute("class","ra-row");
				createElement.appendChild(divElement);				
			}	
			divElement.appendChild(createElement(field, document, j));			
			j++;
		}
	}

	private Element createElement(final FieldMetadata field,
			final Document document, int j) {
		final String fieldName = field.getFieldName().getSymbolName();
		JavaType fieldType = field.getFieldType();
		AnnotationMetadata annotationMetadata;

		// Ignoring java.util.Map field types (see ROO-194)
		if (fieldType.equals(new JavaType(Map.class.getName()))) {
			return null;
		}
		// Fields contained in the embedded Id type have been added
		// separately to the field list
		if (field.getCustomData().keySet()
				.contains(CustomDataKeys.EMBEDDED_ID_FIELD)) {
			return null;
		}
		log.info("****fieldType.getFullyQualifiedTypeName() "+fieldType.getFullyQualifiedTypeName());
		log.info("****entityName "+entityName);
		
		// fieldType = getJavaTypeForField(field);

		final JavaTypeMetadataDetails typeMetadataHolder = relatedDomainTypes
				.get(fieldType);
		JavaTypePersistenceMetadataDetails typePersistenceMetadataHolder = null;
		if (typeMetadataHolder != null) {
			typePersistenceMetadataHolder = typeMetadataHolder
					.getPersistenceDetails();
		}

		// if(j%2==0) {
		//
		// divElement = document.createElement("div");
		// divElement.setAttribute("class","ra-row");
		// root.appendChild(divElement);
		// }
		Element fieldElement = null;

		fieldElement = document.createElement("ra-input");

		// addCommonAttributes(field, fieldElement);
		fieldElement.setAttribute("label",field.getFieldName().getReadableSymbolName());
	    fieldElement.setAttribute("ng-attr-name",entityName+"."+field.getFieldName().getSymbolName());
		fieldElement.setAttribute("ng-attr-id",field.getFieldName().getSymbolName());
		
		if (fieldType.getFullyQualifiedTypeName().equals(
				Boolean.class.getName())
				|| fieldType.getFullyQualifiedTypeName().equals(
						boolean.class.getName())) {
			fieldElement.setAttribute("ng-attr-type", "checkbox");
			// Handle enum fields
		} else if (typeMetadataHolder != null
				&& typeMetadataHolder.isEnumType()) {
			fieldElement.setAttribute("ng-attr-type", "checkbox");

			// .addAttribute(
			// "items",
			// "${"
			// + typeMetadataHolder.getPlural()
			// .toLowerCase() + "}")
			// .addAttribute("path", getPathForType(fieldType))
			// .build();
		} else if (field.getCustomData().keySet()
				.contains(CustomDataKeys.ONE_TO_MANY_FIELD)) {
			// OneToMany relationships are managed from the 'many' side of
			// the relationship, therefore we provide a link to the relevant
			// form the link URL is determined as a best effort attempt
			// following Roo REST conventions, this link might be wrong if
			// custom paths are used if custom paths are used the developer
			// can adjust the path attribute in the field:reference tag
			// accordingly
			// if (typePersistenceMetadataHolder != null) {
			// fieldElement = new XmlElementBuilder("field:simple",
			// document)
			// .addAttribute("messageCode",
			// "entity_reference_not_managed")
			// .addAttribute(
			// "messageCodeAttribute",
			// new JavaSymbolName(fieldType
			// .getSimpleTypeName())
			// .getReadableSymbolName()).build();
			// }
			// else {
			// continue;
			// }
		} else if (field.getCustomData().keySet()
				.contains(CustomDataKeys.MANY_TO_ONE_FIELD)
				|| field.getCustomData().keySet()
						.contains(CustomDataKeys.MANY_TO_MANY_FIELD)
				|| field.getCustomData().keySet()
						.contains(CustomDataKeys.ONE_TO_ONE_FIELD)) {
			// final JavaType referenceType = getJavaTypeForField(field);
			// final JavaTypeMetadataDetails referenceTypeMetadata =
			// relatedDomainTypes
			// .get(referenceType);
			// if (referenceType != null && referenceTypeMetadata != null
			// && referenceTypeMetadata.isApplicationType()
			// && typePersistenceMetadataHolder != null) {
			// fieldElement = new XmlElementBuilder("field:select",
			// document)
			// .addAttribute(
			// "items",
			// "${"
			// + referenceTypeMetadata.getPlural()
			// .toLowerCase() + "}")
			// .addAttribute(
			// "itemValue",
			// typePersistenceMetadataHolder
			// .getIdentifierField()
			// .getFieldName().getSymbolName())
			// .addAttribute(
			// "path",
			// "/"
			// + getPathForType(getJavaTypeForField(field)))
			// .build();
			// if (field.getCustomData().keySet()
			// .contains(CustomDataKeys.MANY_TO_MANY_FIELD)) {
			// fieldElement.setAttribute("multiple", "true");
			// }
			// }
		} else if (fieldType.equals(DATE) || fieldType.equals(CALENDAR)) {
			if (fieldName.equals(CREATED)) {
				return null;
			}
			// Only include the date picker for styles supported by Dojo
			// (SMALL & MEDIUM)
			fieldElement.setAttribute("ng-attr-type", "datetime");
			// fieldElement = new XmlElementBuilder("field:datetime", document)
			// .addAttribute(
			// "dateTimePattern",
			// "${" + entityName + "_"
			// + fieldName.toLowerCase()
			// + "_date_format}").build();
			// if (null != MemberFindingUtils.getAnnotationOfType(
			// field.getAnnotations(), FUTURE)) {
			// fieldElement.setAttribute("future", "true");
			// }
			// else if (null != MemberFindingUtils.getAnnotationOfType(
			// field.getAnnotations(), PAST)) {
			// fieldElement.setAttribute("past", "true");
			// }
		} else if (field.getCustomData().keySet()
				.contains(CustomDataKeys.LOB_FIELD)) {

			fieldElement.setAttribute("ng-attr-type", "textarea");
		}
		if ((annotationMetadata = MemberFindingUtils.getAnnotationOfType(
				field.getAnnotations(), SIZE)) != null) {
			final AnnotationAttributeValue<?> max = annotationMetadata
					.getAttribute(new JavaSymbolName("max"));
			if (max != null) {
				final int maxValue = (Integer) max.getValue();
				if (fieldElement == null && maxValue > 30) {
					fieldElement = new XmlElementBuilder("field:textarea",
							document).build();
				}
			}
		}
		if (fieldType.getFullyQualifiedTypeName().equals(
				String.class.getName())) {			
			fieldElement.setAttribute("ng-attr-type", "string");
		}
		// Use a default input field if no other criteria apply
		if (fieldElement == null) {
		//	fieldElement.setAttribute("ng-attr-type", "string");
		}
	
		// If identifier manually assigned, then add 'required=true'
		if (formBackingTypePersistenceMetadata.getIdentifierField()
				.getFieldName().equals(field.getFieldName())
				&& field.getAnnotation(JpaJavaType.GENERATED_VALUE) == null) {
			fieldElement.setAttribute("mandatory", "true");
		}

		fieldElement.setAttribute("z",
				XmlRoundTripUtils.calculateUniqueKeyFor(fieldElement));

		fieldElement.setAttribute("ng-attr-id", fieldName);
		// fieldElement.setAttribute("ng-attr-name",
		// fieldElement.setAttribute("label",
		// fieldElement.setAttribute("ng-attr-type",
		fieldElement.setAttribute("ng-attr-side", (j % 2 == 0 ? "left"
				: "right"));

		return fieldElement;

	}

	@Override
	public Document getFinderDocument(
			FinderMetadataDetails finderMetadataDetails) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document getListDocument() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document getShowDocument() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document getUpdateDocument() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Document getMenuDocument(){
		
		
		return null;
	}
}
