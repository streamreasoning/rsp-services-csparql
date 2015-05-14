package it.polimi.deib.rsp_services_csparql.observers.utilities;

import eu.larkc.csparql.common.RDFTable;

public interface OutputDataMarshaller {

	public final static Class<DefaultDataMarshaller> DEFAULT_OUTPUT_DATA_MARSHALLER_IMPL = DefaultDataMarshaller.class;
	public static final String OUTPUT_DATA_MARSHALLER_IMPL_PROPERTY_NAME = "it.polimi.deib.rsp_services_csparql.observers.OutputDataMarshaller";
	String marshal(RDFTable q, String outputFormat);

}
