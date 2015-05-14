package it.polimi.deib.rsp_services_csparql.observers.utilities;

import eu.larkc.csparql.common.RDFTable;

public class DefaultDataMarshaller implements OutputDataMarshaller {

	@Override
	public String marshal(RDFTable q, String format) {
		// TODO only RDF/JSON implemented
		return q.getJsonSerialization();
	}

}
