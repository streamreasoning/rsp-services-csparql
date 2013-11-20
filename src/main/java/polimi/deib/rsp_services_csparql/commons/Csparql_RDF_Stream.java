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

import org.streamreasoning.rsp_services.commons.Rsp_services_Component_Status;
import org.streamreasoning.rsp_services.interfaces.RDF_Stream_Interface;

import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;

public class Csparql_RDF_Stream implements RDF_Stream_Interface{

	private RdfStream stream;
	private Rsp_services_Component_Status status;
	
	public Csparql_RDF_Stream() {
		super();
	}
	public Csparql_RDF_Stream(RdfStream stream, Rsp_services_Component_Status status) {
		super();
		this.stream = stream;
		this.status = status;
	}
	
	public RdfStream getStream() {
		return stream;
	}
	
	public void setStream(RdfStream stream) {
		this.stream = stream;
	}
	
	public Rsp_services_Component_Status getStatus() {
		return status;
	}
	
	public void setStatus(Rsp_services_Component_Status status) {
		this.status = status;
	}
	
	@Override
	public void feed_RDF_stream(Object dataSerialization) {
		RdfQuadruple quadruple = (RdfQuadruple) dataSerialization;
		stream.put(quadruple);
	}
	
}
