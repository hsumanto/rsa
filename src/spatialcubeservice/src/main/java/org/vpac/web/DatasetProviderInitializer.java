package org.vpac.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.vpac.ndg.query.io.DatasetProvider;
import org.vpac.ndg.query.io.ProviderRegistry;

public class DatasetProviderInitializer extends HttpServlet {

	private static final long serialVersionUID = 1L;
	@Autowired
	DatasetProvider rsaDatasetProvider;
	@Autowired
	DatasetProvider previewDatasetProvider;

	
	public void init() throws ServletException {
		ProviderRegistry.getInstance().clearProivders();
		ProviderRegistry.getInstance().addProivder(rsaDatasetProvider);
		ProviderRegistry.getInstance().addProivder(previewDatasetProvider);
	}
}
