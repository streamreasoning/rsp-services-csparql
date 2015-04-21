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
package it.polimi.deib.rsp_services_csparql.commons;


import it.polimi.deib.rsp_services_csparql.configuration.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streamreasoning.rsp_services.interfaces.RDF_Stream_Processor_Interface;

import eu.larkc.csparql.cep.api.RdfStream;
import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;

public class Csparql_Engine implements RDF_Stream_Processor_Interface{

	private Logger logger = LoggerFactory.getLogger(Csparql_Engine.class.getName());
	private CsparqlEngineImpl engine;

	public void execUpdateQueryOverDatasource(String queryBody){
		engine.execUpdateQueryOverDatasource(queryBody);
	}

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
	public Object registerStream(Object stream) {
		RdfStream RDFstream = (RdfStream) stream;
		return engine.registerStream(RDFstream);
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
	public Object unregisterStream(Object stream) {
		RdfStream RDFstream = (RdfStream) stream;
		boolean result = false;
		try{
			engine.unregisterStream(RDFstream.getIRI());
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
		return engine.registerQuery(queryBody, false);
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
	public void initialize() throws Exception {
		engine = new CsparqlEngineImpl();

		if(Config.getInstance().getEnableTSFunction()){
			logger.debug("Timestamp function enabled");
			engine.initialize(true);
		} else {
			logger.debug("Timestamp function disbaled");
			engine.initialize(false);
		}
	}

	public void addStaticModel(String iri, String location){
		engine.putStaticNamedModel(iri, location);
	}

	public void removeStaticModel(String iri){
		engine.removeStaticNamedModel(iri);
	}

	public void queryStaticKnowledge(String queryBody){
		engine.execUpdateQueryOverDatasource(queryBody);
	}

	public RDFTable evaluateQueryOverDatasource(String queryBody) {
		return engine.evaluateQueryOverDatasource(queryBody);
	}

}
