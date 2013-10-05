/*******************************************************************************
 * Copyright 2013 DEIB - Politecnico di Milano
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
 ******************************************************************************/
package polimi.deib.rsp_service4csparql_server.query.utilities;

import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.streams.format.GenericObserver;

public class CsparqlObserver {
	
	private String id;
	private GenericObserver<RDFTable> observer;
	
	public CsparqlObserver(String id, GenericObserver<RDFTable> observer) {
		super();
		this.id = id;
		this.observer = observer;
	}
	
	public CsparqlObserver() {
		super();
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public GenericObserver<RDFTable> getObserver() {
		return observer;
	}
	
	public void setObserver(GenericObserver<RDFTable> observer) {
		this.observer = observer;
	}
	
	

}
