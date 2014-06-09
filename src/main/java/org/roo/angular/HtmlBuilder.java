package org.roo.angular;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;

import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HtmlBuilder {
	private Logger log = Logger.getLogger(getClass().getName());
	
	public Document buildPage(List<String> items, String title) {
		final DocumentBuilder builder = XmlUtils.getDocumentBuilder();
		final Document document = builder.newDocument();
		final Element html = (Element) document
				.appendChild(new XmlElementBuilder("html", document).build());

		buildHead(document, html);
		
		Element bodyElement =  (Element) html.appendChild(new XmlElementBuilder("body", document).build());
		bodyElement.appendChild(new XmlElementBuilder("div", document).addAttribute("class", "spacer").build());
		
		Element topElement =  (Element)  bodyElement.appendChild(new XmlElementBuilder("div", document)
		.addAttribute("class", "container-fluid").build());
		
		Element topElement1 = document.createElement("nav");
		topElement1.setAttribute("class", "navbar navbar-default");
		topElement1.setAttribute("role", "navigation");
		topElement.appendChild(topElement1);
		
		Element topElement2 = document.createElement("div");
		topElement2.setAttribute("class", "container-fluid");
		topElement1.appendChild(topElement2);	
		
		buildMenuTitle(document,topElement2, title);
		
		Element topElement4 = document.createElement("div");
		topElement4.setAttribute("class", "collapse navbar-collapse");
		topElement4.setAttribute("id", "bs-example-navbar-collapse-1");
		topElement2.appendChild(topElement4);
		
		buildMenu(document, topElement4, items);
		
		buildBody(document, topElement);
		return document;
	}

	private void buildHead(final Document document, final Element root){
		Element headElement = document.createElement("head");	
		root.appendChild(headElement);		
		Element linkElement = document.createElement("link");
		linkElement.setAttribute("rel", "stylesheet"); 
		linkElement.setAttribute("href", "js/custom.css"); 
		headElement.appendChild(linkElement);
		Element linkElement1 = document.createElement("link");
		linkElement1.setAttribute("rel", "stylesheet"); 
		linkElement1.setAttribute("href", "stylesheets/styles.css"); 
		headElement.appendChild(linkElement1);
		Element scriptElement1 = document.createElement("script");
		scriptElement1.setAttribute("data-main", "js/main.js"); 
		scriptElement1.setAttribute("src", "bower_components/requirejs/require.js"); 
		headElement.appendChild(scriptElement1);
		Element scriptElement2 = document.createElement("script");		
		scriptElement2.setAttribute("src", "bower_components/angular/angular.js"); 
		headElement.appendChild(scriptElement2);
	}
	
	private void buildMenuTitle(final Document document, final Element root, String title ){
	
		Element topElement = document.createElement("div");
		topElement.setAttribute("class", "navbar-header");
		root.appendChild(topElement);
		log.info("buttonElement");
		Element buttonElement = document.createElement("button");
		buttonElement.setAttribute("type", "button");
		buttonElement.setAttribute("class", "navbar-toggle");
		buttonElement.setAttribute("data-toggle", "collapse");
		buttonElement.setAttribute("data-target", "#bs-example-navbar-collapse-1");
		topElement.appendChild(buttonElement);
		
		Element spanElement = document.createElement("span");
		spanElement.setAttribute("class", "sr-only");
		spanElement.setTextContent("Toggle navigation");
		buttonElement.appendChild(spanElement);
		
		spanElement = document.createElement("span");
		spanElement.setAttribute("class", "icon-bar");
		buttonElement.appendChild(spanElement);
		
		spanElement = document.createElement("span");
		spanElement.setAttribute("class", "icon-bar");
		buttonElement.appendChild(spanElement);
		
		spanElement = document.createElement("span");
		spanElement.setAttribute("class", "icon-bar");
		buttonElement.appendChild(spanElement);
		
		Element linkElement =  document.createElement("a");
		linkElement.setAttribute("class", "navbar-brand");
		linkElement.setAttribute("href", "#");
		linkElement.setTextContent(title); 	  
		topElement.appendChild(linkElement);
		
	}
	
	private void buildMenu(final Document document, final Element root,
			List<String> items) {
		for (String item : items) {
			Element topElement = document.createElement("ul");
			topElement.setAttribute("class", "nav navbar-nav");
			root.appendChild(topElement);
			Element bodyElement = document.createElement("li");
			bodyElement.setAttribute("class", "dropdown");
			topElement.appendChild(bodyElement);
			Element linkElement = document.createElement("a");
			linkElement.setAttribute("href", "#");
			linkElement.setAttribute("data-toggle", "dropdown");
			linkElement.setAttribute("class", "dropdown-toggle");
			linkElement.setTextContent(item);
			bodyElement.appendChild(linkElement);		
			buildMenuItem(document, bodyElement, item);
		}

	}
	 

	private void buildMenuItem(final Document document, final Element root,
			final String entity) {
		Element menuElement = document.createElement("ul");
		menuElement.setAttribute("class", "dropdown-menu");
		root.appendChild(menuElement);
		List<String> options = new ArrayList<String>();
		options.add("Create");
		options.add("List");
		for (String item : options) {
			Element itemElement = document.createElement("li");
			Element menuLinkElement = document.createElement("a");
			menuLinkElement.setTextContent(item);
			menuLinkElement.setAttribute("href", "#"+entity+"/create");
			itemElement.appendChild(menuLinkElement);
			menuElement.appendChild(itemElement);
			// <li class="divider"></li>
		}
	}

	private void buildBody(final Document document, final Element root){
		Element bodyElement = document.createElement("div");
		bodyElement.setAttribute("class", "row");
		root.appendChild(bodyElement);
		Element leftElement = document.createElement("div");
		leftElement.setAttribute("class", "col-md-4");
		bodyElement.appendChild(leftElement);
		Element leftBody = document.createElement("div");
		leftBody.setAttribute("class", "jumbotron");
		leftElement.appendChild(leftBody);
		Element leftPara = document.createElement("p");	
		leftBody.appendChild(leftPara);
		leftPara.setTextContent("Content");      
		
		Element rightElement = document.createElement("div");
		rightElement.setAttribute("class", "col-md-8");
		bodyElement.appendChild(rightElement);
		Element rightBody = document.createElement("div");
		Attr atttribute = document.createAttribute("ng-view");
		rightBody.setAttributeNode(atttribute);
		rightElement.appendChild(rightBody);
	}
}
