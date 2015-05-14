package it.polimi.deib.rsp_services_csparql.streams.utilities;

import com.hp.hpl.jena.rdf.model.Model;

public interface InputDataUnmarshaller {

	public static final String INPUT_DATA_UNMARSHALLER_IMPL_PROPERTY_NAME = "it.polimi.deib.rsp_services_csparql.streams.InputDataUnmarshaller";
	public static final Class<? extends InputDataUnmarshaller> DEFAULT_INPUT_DATA_UNMARSHALLER_IMPL = DefaultDataUnmarshaller.class;

	Model unmarshal(String inputData) throws Exception;

}
