/*******************************************************************************
 * Copyright 2014 DEIB - Politecnico di Milano
 *  
 * Marco Balduini (marco.balduini@polimi.it)
 * Emanuele Della Valle (emanuele.dellavalle@polimi.it)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This work was partially supported by the European project LarKC (FP7-215535) and by the European project MODAClouds (FP7-318484)
 ******************************************************************************/
package it.polimi.deib.rsp_services_csparql.observers;

import it.polimi.deib.rsp_services_csparql.commons.Csparql_Engine;
import it.polimi.deib.rsp_services_csparql.commons.Csparql_Query;
import it.polimi.deib.rsp_services_csparql.configuration.Config;
import it.polimi.deib.rsp_services_csparql.observers.utilities.Observer4HTTP;
import it.polimi.deib.rsp_services_csparql.observers.utilities.Observer4Socket;
import it.polimi.deib.rsp_services_csparql.queries.SingleQueryDataServer;
import it.polimi.deib.rsp_services_csparql.queries.utilities.Csparql_Observer_Descriptor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streamreasoning.rsp_services.interfaces.Continuous_Query_Observer_Interface;

import com.google.gson.Gson;

public class MultipleObserversDataServer  extends ServerResource {

	private static Hashtable<String, Csparql_Query> csparqlQueryTable;
	private Gson gson = new Gson();

	private Logger logger = LoggerFactory.getLogger(SingleQueryDataServer.class.getName());
	private Csparql_Engine engine;

	@Options
	public void optionsRequestHandler(){
		String origin = getRequest().getClientInfo().getAddress();
		getResponse().setAccessControlAllowOrigin(origin);
	}

	@SuppressWarnings({ "unchecked" })
	@Get
	public void getObserversInfo(){

		csparqlQueryTable = (Hashtable<String, Csparql_Query>) getContext().getAttributes().get("csaprqlQueryTable");
		ArrayList<Csparql_Observer_Descriptor> observers = new ArrayList<Csparql_Observer_Descriptor>();

		String queryName = (String) this.getRequest().getAttributes().get("queryname");

		String origin = getRequest().getClientInfo().getAddress();
		getResponse().setAccessControlAllowOrigin(origin);
		
		try{
			if(queryName.contains(queryName)){
				Csparql_Query csparqlQuery = csparqlQueryTable.get(queryName);
				Set<Entry<String, Csparql_Observer_Descriptor>> obsSet = csparqlQuery.getObservers().entrySet();
				for(Entry<String, Csparql_Observer_Descriptor> eObserver : obsSet){
					observers.add(eObserver.getValue());
				}
				this.getResponse().setStatus(Status.SUCCESS_OK, "Observers informations succesfully extracted");
				this.getResponse().setEntity(gson.toJson(observers), MediaType.APPLICATION_JSON);
			} else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, queryName + " ID is not associated to any registered query");
				this.getResponse().setEntity(gson.toJson(queryName + " ID is not associated to any registered query"), MediaType.APPLICATION_JSON);
			}
		} catch (Exception e) {
			logger.error("Error while getting observers information");
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Error while getting observers information");
			this.getResponse().setEntity(gson.toJson("Error while getting observers information"), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}

	}
	
	@SuppressWarnings({ "unchecked" })
	@Post
	public void addObserver(Representation rep){

		try{

			csparqlQueryTable = (Hashtable<String, Csparql_Query>) getContext().getAttributes().get("csaprqlQueryTable");
			engine = (Csparql_Engine) getContext().getAttributes().get("csparqlengine");
			String hostName = Config.getInstance().getHostName();
			String serverAddress = (String) getContext().getAttributes().get("complete_server_address");


			String origin = getRequest().getClientInfo().getAddress();
			getResponse().setAccessControlAllowOrigin(origin);

			String requestBody = rep.getText();
						
			List<NameValuePair> parametersPairs = URLEncodedUtils.parse(requestBody, StandardCharsets.UTF_8);
			String callbackUrl = null;
			String format = "RDF/JSON";
			String protocol = "HTTP";
			String observerHost = null;
			String observerPort = null;
			for (NameValuePair nameValuePair : parametersPairs) {
				if (nameValuePair.getName().equals("callbackUrl"))
					callbackUrl = nameValuePair.getValue();
				else if (nameValuePair.getName().equals("format"))
					format = nameValuePair.getValue();
				else if (nameValuePair.getName().equals("protocol"))
					protocol = nameValuePair.getValue();
				else if (nameValuePair.getName().equals("observerHost"))
					observerHost = nameValuePair.getValue();
				else if (nameValuePair.getName().equals("observerPort"))
					observerPort = nameValuePair.getValue();
			}

			String queryURI = (String) this.getRequest().getAttributes().get("queryname");
			String queryName = queryURI.replace(hostName + "queries/", "");

			if (csparqlQueryTable.containsKey(queryName)){
				Csparql_Query csparqlQuery = csparqlQueryTable.get(queryName);
				String observerID = String.valueOf(requestBody.hashCode());
				String observerURI = serverAddress + "/queries/" + queryName + "/observers/" + observerID;
				Continuous_Query_Observer_Interface observer;
				if (protocol.equalsIgnoreCase("TCP") || protocol.equalsIgnoreCase("UDP")){
					if (observerHost == null || observerPort == null){
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No observerHost or observerPort specified");
						this.getResponse().setEntity(gson.toJson("No observerHost or observerPort specified"), MediaType.APPLICATION_JSON);
					}
					observer = new Observer4Socket(observerHost, Integer.parseInt(observerPort), protocol, format);
				} else {
					if (callbackUrl == null){
						this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"No callbackUrl specified");
						this.getResponse().setEntity(gson.toJson("No callbackUrl specified"), MediaType.APPLICATION_JSON);
					}
					observer = new Observer4HTTP(callbackUrl, format);
				}
				Csparql_Observer_Descriptor csObs = new Csparql_Observer_Descriptor(observerID, observer);
				csparqlQuery.addObserver(csObs);
				this.getResponse().setStatus(Status.SUCCESS_OK,"Observer succesfully registered");
				this.getResponse().setEntity(gson.toJson(observerURI), MediaType.APPLICATION_JSON);	
				getContext().getAttributes().put("csaprqlQueryTable", csparqlQueryTable);
				getContext().getAttributes().put("csparqlengine", engine);
			} else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,queryURI + " ID is not associated to any registered query");
				this.getResponse().setEntity(gson.toJson(queryURI + " ID is not associated to any registered query"), MediaType.APPLICATION_JSON);
			}
		} catch (Exception e) {
			logger.error("Error while adding observer to query", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Error during query operations");
			this.getResponse().setEntity(gson.toJson("Error during query operations"), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}
	}
}
