package polimi.deib.rsp_services_csparql.commons;

import eu.larkc.csparql.cep.api.RdfStream;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import polimi.deib.rsp_services.interfaces.RDF_Stream_Processor_Interface;

public class Csparql_Engine implements RDF_Stream_Processor_Interface{

	private CsparqlEngineImpl engine;

	@Override
	public Object getRDFStreamProcessor() {
		return engine;
	}

	@Override
	public Object registerStream(String streamName) {
		RdfStream stream = new RdfStream(streamName);
		return engine.registerStream(stream);
	}

	@Override
	public Object unregisterStream(String streamName) {
		boolean result = false;
		try{
			engine.unregisterStream(streamName);
			result = true;
		}catch(Exception e){
			result = false;
		}
		return result;
	}

	@Override
	public Object getStream(String streamName) {
		return engine.getStreamByIri(streamName);
	}

	@Override
	public Object registerQuery(String queryBody) throws Exception{
		return engine.registerQuery(queryBody);
	}

	@Override
	public Object unregisterQuery(String queryID) {
		boolean result = false;
		try{
			engine.unregisterQuery(queryID);
			result = true;
		}catch(Exception e){
			result = false;
		}
		return result;	
	}

	@Override
	public Object getAllQueries() {
		return engine.getAllQueries();
	}

	@Override
	public Object stopQuery(String queryID) {
		boolean result = false;
		try{
			engine.stopQuery(queryID);
			result = true;
		}catch(Exception e){
			result = false;
		}
		return result;	
	}

	@Override
	public Object startQuery(String queryID) {
		boolean result = false;
		try{
			engine.startQuery(queryID);
			result = true;
		}catch(Exception e){
			result = false;
		}
		return result;	
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

}
