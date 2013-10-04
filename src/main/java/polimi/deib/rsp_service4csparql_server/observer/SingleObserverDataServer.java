package polimi.deib.rsp_service4csparql_server.observer;

import java.util.Hashtable;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import polimi.deib.rsp_service4csparql_server.query.utilities.CsparqlQuery;

import com.google.gson.Gson;

public class SingleObserverDataServer extends ServerResource {

	private static Hashtable<String, CsparqlQuery> csparqlQueryTable;
	private Gson gson = new Gson();

	private Logger logger = LoggerFactory.getLogger(SingleObserverDataServer.class.getName());

	@SuppressWarnings("unchecked")
	@Delete
	public void unregisterObserver(){

		csparqlQueryTable = (Hashtable<String, CsparqlQuery>) getContext().getAttributes().get("csaprqlQueryTable");

		String server_address = (String) getContext().getAttributes().get("complete_server_address");

		String queryURI = (String) this.getRequest().getAttributes().get("queryname");
		String queryName = queryURI.replace(server_address + "/queries/", "");
		String observerURI = (String) this.getRequest().getAttributes().get("id");
		String observerId = observerURI.replace(queryURI + "/observers/", "");

		try{
			if(csparqlQueryTable.containsKey(queryName)){
				CsparqlQuery csparqlQuery = csparqlQueryTable.get(queryName);
				if(csparqlQuery.getObservers().containsKey(observerId)){
					csparqlQuery.removeObserver(observerId);
					getContext().getAttributes().put("csaprqlQueryTable", csparqlQueryTable);
					this.getResponse().setStatus(Status.SUCCESS_OK,"Observer succesfully unregistered");
					this.getResponse().setEntity(gson.toJson("Observer succesfully unregistered"), MediaType.APPLICATION_JSON);
				} else {
					this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, queryURI + " ID is not associated to any registered query");
					this.getResponse().setEntity(gson.toJson(queryURI + " ID is not associated to any registered query"), MediaType.APPLICATION_JSON);
				}
			} else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, queryURI + " ID is not associated to any registered query");
				this.getResponse().setEntity(gson.toJson(queryURI + " ID is not associated to any registered query"), MediaType.APPLICATION_JSON);
			}
		} catch (Exception e) {
			logger.error("Error while unregistering observer " + observerURI);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Error while unregistering observer " + observerURI);
			this.getResponse().setEntity(gson.toJson("Error while unregistering observer " + observerURI), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}

	}

	@SuppressWarnings("unchecked")
	@Get
	public void getObserverInfo(){

		csparqlQueryTable = (Hashtable<String, CsparqlQuery>) getContext().getAttributes().get("csaprqlQueryTable");

		String server_address = (String) getContext().getAttributes().get("complete_server_address");

		String queryURI = (String) this.getRequest().getAttributes().get("queryname");
		String queryName = queryURI.replace(server_address + "/queries/", "");
		String observerURI = (String) this.getRequest().getAttributes().get("id");
		String observerId = observerURI.replace(queryURI + "/observers/", "");

		try{
			if(csparqlQueryTable.containsKey(queryName)){
				CsparqlQuery csparqlQuery = csparqlQueryTable.get(queryName);
				if(csparqlQuery.getObservers().containsKey(observerId)){
					getContext().getAttributes().put("csaprqlQueryTable", csparqlQueryTable);
					this.getResponse().setStatus(Status.SUCCESS_OK,"Observer informations succesfully extracted");
					this.getResponse().setEntity(gson.toJson(csparqlQuery.getObservers().get(observerId)), MediaType.APPLICATION_JSON);		
				} else {
					this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, observerURI + " ID is not associated to any registered observer");
					this.getResponse().setEntity(gson.toJson(observerURI + " ID is not associated to any registered observer"), MediaType.APPLICATION_JSON);
				}
			} else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, queryURI + " ID is not associated to any registered query");
				this.getResponse().setEntity(gson.toJson(queryURI + " ID is not associated to any registered query"), MediaType.APPLICATION_JSON);
			}
		} catch (Exception e) {
			logger.error("Error while unregistering observer " + observerURI);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Error while unregistering observer " + observerURI);
			this.getResponse().setEntity(gson.toJson("Error while unregistering observer " + observerURI), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}

	}
}
