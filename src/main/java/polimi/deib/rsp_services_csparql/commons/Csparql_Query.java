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
package polimi.deib.rsp_services_csparql.commons;

import java.util.Collection;
import java.util.HashMap;

import org.streamreasoning.rsp_services.commons.Rsp_services_Component_Status;
import org.streamreasoning.rsp_services.interfaces.Continuous_Query_Interface;

import polimi.deib.rsp_services_csparql.queries.utilities.Csparql_Observer_Descriptor;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;

public class Csparql_Query implements Continuous_Query_Interface{

	private String id;
	private String name;
	private String type;
	private Collection<String> streams;
	private String body;
	private CsparqlQueryResultProxy resultProxy;
	private HashMap<String, Csparql_Observer_Descriptor> observers;
	private Rsp_services_Component_Status status;

	public Csparql_Query() {
		super();
	}

	public Csparql_Query(String id, String name, String type,
			Collection<String> streams, String body,
			CsparqlQueryResultProxy resultProxy,
			HashMap<String, Csparql_Observer_Descriptor> observers, Rsp_services_Component_Status status) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.streams = streams;
		this.body = body;
		this.resultProxy = resultProxy;
		this.observers = observers;
		this.status = status;
	}

	public boolean containsObserver(String observerID){
		if(observers.containsKey(observerID))
			return true;
		else
			return false;
	}

	@Override
	public Object addObserver(Object observer) {
		boolean result = false;
		try{
			Csparql_Observer_Descriptor obs = (Csparql_Observer_Descriptor) observer;
			if(!observers.containsKey(obs.getId())){
				this.resultProxy.addObserver(obs.getObserver());
				observers.put(obs.getId(), obs);
			}
			result = true;
		} catch(Exception e){
			result = false;
		}

		return result;
	}

	@Override
	public Object removeObserver(Object observerID) {
		boolean result = false;
		try{
			Csparql_Observer_Descriptor cso = observers.get(observerID);
			resultProxy.deleteObserver(cso.getObserver());
			observers.remove(observerID);
			result = true;
		} catch(Exception e){
			result = false;
		}

		return result;
	}

	@Override
	public void setQueryID(String queryID) {
		this.id = queryID;
	}

	@Override
	public String getQueryID() {
		return id;
	}

	@Override
	public void setQueryBody(String queryBody) {
		this.body = queryBody;
	}

	@Override
	public String getQueryBody() {
		return body;
	}

	@Override
	public void changeQueryStatus(Rsp_services_Component_Status newStatus) {
		this.status = newStatus;
	}

	@Override
	public Rsp_services_Component_Status getQueryStatus() {
		return status;
	}

	public CsparqlQueryResultProxy getResultProxy() {
		return resultProxy;
	}

	public void setResultProxy(CsparqlQueryResultProxy resultProxy) {
		this.resultProxy = resultProxy;
	}

	public HashMap<String, Csparql_Observer_Descriptor> getObservers() {
		return observers;
	}

	public void setObservers(HashMap<String, Csparql_Observer_Descriptor> observers) {
		this.observers = observers;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Collection<String> getStreams() {
		return streams;
	}

	public void setStreams(Collection<String> streams) {
		this.streams = streams;
	}
}
