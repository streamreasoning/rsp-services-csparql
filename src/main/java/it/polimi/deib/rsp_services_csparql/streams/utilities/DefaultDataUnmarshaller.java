package it.polimi.deib.rsp_services_csparql.streams.utilities;

import java.io.ByteArrayInputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class DefaultDataUnmarshaller implements InputDataUnmarshaller {

	@Override
	public Model unmarshal(String inputData) throws Exception {
		Model model = ModelFactory.createDefaultModel();
		try {
			model.read(new ByteArrayInputStream(inputData.getBytes("UTF-8")),
					null, "RDF/JSON");
		} catch (Exception e) {
			try {
				model.read(
						new ByteArrayInputStream(inputData.getBytes("UTF-8")),
						null, "RDF/XML");
			} catch (Exception e1) {
				try {
					model.read(
							new ByteArrayInputStream(inputData
									.getBytes("UTF-8")), null, "N-TRIPLE");
				} catch (Exception e2) {
					model.read(
							new ByteArrayInputStream(inputData
									.getBytes("UTF-8")), null, "TURTLE");
				}
			}
		}
		return model;
	}

}
