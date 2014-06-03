/*******************************************************************************
 * Copyright 2013 Marco Balduini, Emanuele Della Valle
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
 * Acknowledgements:
 *   
 * This work was partially supported by the European project LarKC (FP7-215535) 
 * and by the European project MODAClouds (FP7-318484)
 ******************************************************************************/
package polimi.deib.rsp_services_csparql.streams;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import polimi.deib.rsp_services_csparql.commons.Csparql_RDF_Stream;
import polimi.deib.rsp_services_csparql.streams.utilities.CsparqlStreamDescriptionForGet;

import com.google.gson.Gson;

public class MultipleStreamsDataServer extends ServerResource {

	private static Hashtable<String, Csparql_RDF_Stream> csparqlStreamTable;
	private Gson gson = new Gson();
	private Logger logger = LoggerFactory.getLogger(MultipleStreamsDataServer.class.getName());
	
	@SuppressWarnings("unchecked")
	@Options
	public void optionsRequestHandler(){
		String origin = getRequest().getClientInfo().getAddress();
		Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
		if (responseHeaders == null) {
			responseHeaders = new Series<Header>(Header.class);
			getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
		}
		responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));
	}

	@SuppressWarnings({ "unchecked" })
	@Get
	public void getStreamsInformations(){

		try{
			
			String origin = getRequest().getClientInfo().getAddress();
			Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
			if (responseHeaders == null) {
				responseHeaders = new Series<Header>(Header.class);
				getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
			}
			responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));
		    
			csparqlStreamTable = (Hashtable<String, Csparql_RDF_Stream>) getContext().getAttributes().get("csaprqlinputStreamTable");
			ArrayList<CsparqlStreamDescriptionForGet> streamDescriptionList = new ArrayList<CsparqlStreamDescriptionForGet>();

			Set<String> keySet = csparqlStreamTable.keySet();
			Csparql_RDF_Stream registeredCsparqlStream;
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
