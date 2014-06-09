package org.roo.angular;

import org.springframework.roo.addon.web.mvc.controller.details.FinderMetadataDetails;
import org.w3c.dom.Document;

public interface ViewManager {

	public abstract Document getCreateDocument();

	public abstract Document getFinderDocument(
			FinderMetadataDetails finderMetadataDetails);

	public abstract Document getListDocument();

	public abstract Document getShowDocument();

	public abstract Document getUpdateDocument();

	public abstract Document getMenuDocument();

}