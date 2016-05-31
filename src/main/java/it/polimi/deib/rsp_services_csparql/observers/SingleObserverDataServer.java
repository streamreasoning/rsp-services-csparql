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

import it.polimi.deib.rsp_services_csparql.commons.Csparql_Query;
import it.polimi.deib.rsp_services_csparql.configuration.Config;

import java.io.StringWriter;
import java.util.*;

import it.polimi.deib.rsp_services_csparql.queries.utilities.Csparql_Observer_Descriptor;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class SingleObserverDataServer extends ServerResource {

	private static Hashtable<String, Csparql_Query> csparqlQueryTable;
	private Gson gson = new Gson();

	private Logger logger = LoggerFactory.getLogger(SingleObserverDataServer.class.getName());

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
    public void getObserversInfo(){

        csparqlQueryTable = (Hashtable<String, Csparql_Query>) getContext().getAttributes().get("csaprqlQueryTable");

        String queryName = (String) this.getRequest().getAttributes().get("queryname");
        String obsId = (String) this.getRequest().getAttributes().get("id");

        String origin = getRequest().getClientInfo().getAddress();
        Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
        if (responseHeaders == null) {
            responseHeaders = new Series<Header>(Header.class);
            getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
        }
        responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));

        try{
            if(csparqlQueryTable.containsKey(queryName)){
                Csparql_Query csparqlQuery = csparqlQueryTable.get(queryName);
                HashMap<String, Csparql_Observer_Descriptor> obsMap = csparqlQuery.getObservers();
                if(obsMap.containsKey(obsId)) {
                    Csparql_Observer_Descriptor cso = obsMap.get(obsId);
                    StringWriter sw = new StringWriter();
                    cso.getsGraph().write(sw, "JSON-LD");
                    this.getResponse().setStatus(Status.SUCCESS_OK, "Observers informations succesfully extracted");
                    this.getResponse().setEntity(sw.toString(), MediaType.APPLICATION_JSON);
                } else {
                    this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, obsId + " is not associated to any registered observer");
                    this.getResponse().setEntity(gson.toJson(obsId + " is not associated to any registered observer"), MediaType.APPLICATION_JSON);
                }
            } else {
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, queryName + " is not associated to any registered query");
                this.getResponse().setEntity(gson.toJson(queryName + " is not associated to any registered query"), MediaType.APPLICATION_JSON);
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
	@Delete
	public void unregisterObserver(){

		try {

			csparqlQueryTable = (Hashtable<String, Csparql_Query>) getContext().getAttributes().get("csaprqlQueryTable");
			String hostName = Config.getInstance().getHostName();

			String queryName = (String) this.getRequest().getAttributes().get("queryname");
			String observerId = (String) this.getRequest().getAttributes().get("id");

			String origin = getRequest().getClientInfo().getAddress();
			Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
			if (responseHeaders == null) {
				responseHeaders = new Series<Header>(Header.class);
				getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
			}
			responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));

			try{
				if(csparqlQueryTable.containsKey(queryName)){
					Csparql_Query csparqlQuery = csparqlQueryTable.get(queryName);
					if(csparqlQuery.getObservers().containsKey(observerId)){
						csparqlQuery.removeObserver(observerId);
						getContext().getAttributes().put("csaprqlQueryTable", csparqlQueryTable);
						this.getResponse().setStatus(Status.SUCCESS_OK,"Observer succesfully unregistered");
						this.getResponse().setEntity(gson.toJson("Observer succesfully unregistered"), MediaType.APPLICATION_JSON);
					} else {
						this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, queryName + " ID is not associated to any registered query");
						this.getResponse().setEntity(gson.toJson(queryName + " ID is not associated to any registered query"), MediaType.APPLICATION_JSON);
					}
				} else {
					this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, queryName + " ID is not associated to any registered query");
					this.getResponse().setEntity(gson.toJson(queryName + " ID is not associated to any registered query"), MediaType.APPLICATION_JSON);
				}
			} catch (Exception e) {
				logger.error("Error while unregistering observer " + observerId);
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Error while unregistering observer " + observerId);
				this.getResponse().setEntity(gson.toJson("Error while unregistering observer " + observerId), MediaType.APPLICATION_JSON);
			} finally{
				this.getResponse().commit();
				this.commit();	
				this.release();
			}
		} catch (Exception e) {
			logger.error("Error while unregistering query",e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Error while unregistering query");
			this.getResponse().setEntity(gson.toJson("Error while unregistering query"), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}
	}

}
