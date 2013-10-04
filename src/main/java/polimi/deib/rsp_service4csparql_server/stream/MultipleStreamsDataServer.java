package polimi.deib.rsp_service4csparql_server.stream;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import polimi.deib.rsp_service4csparql_server.stream.utilities.CsparqlStream;
import polimi.deib.rsp_service4csparql_server.stream.utilities.CsparqlStreamDescriptionForGet;

import com.google.gson.Gson;

public class MultipleStreamsDataServer extends ServerResource {

	private static Hashtable<String, CsparqlStream> csparqlStreamTable;
	private Gson gson = new Gson();
	private Logger logger = LoggerFactory.getLogger(MultipleStreamsDataServer.class.getName());

	@SuppressWarnings("unchecked")
	@Get
	public void getStreamsInformations(){

		try{
			csparqlStreamTable = (Hashtable<String, CsparqlStream>) getContext().getAttributes().get("csaprqlinputStreamTable");
			ArrayList<CsparqlStreamDescriptionForGet> streamDescriptionList = new ArrayList<CsparqlStreamDescriptionForGet>();

			Set<String> keySet = csparqlStreamTable.keySet();
			CsparqlStream registeredCsparqlStream;
			for(String key : keySet){
				registeredCsparqlStream = csparqlStreamTable.get(key); 
				streamDescriptionList.add(new CsparqlStreamDescriptionForGet(registeredCsparqlStream.getStream().getIRI(), registeredCsparqlStream.getStatus()));
			}

			this.getResponse().setStatus(Status.SUCCESS_OK,"Information about streams succesfully extracted");
			this.getResponse().setEntity(gson.toJson(streamDescriptionList), MediaType.APPLICATION_JSON);

		} catch(Exception e){
			logger.error("Error while getting multiple streams informations", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Generic Error");
			this.getResponse().setEntity(gson.toJson("Generic Error"), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}

	}
}
