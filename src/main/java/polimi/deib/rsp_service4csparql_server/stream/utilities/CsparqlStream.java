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
package polimi.deib.rsp_service4csparql_server.stream.utilities;

import polimi.deib.rsp_service4csparql_server.common.CsparqlComponentStatus;
import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;

public class CsparqlStream{

	private RdfStream stream;
	private CsparqlComponentStatus status;
	
	public CsparqlStream() {
		super();
	}
	public CsparqlStream(RdfStream stream, CsparqlComponentStatus status) {
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
	
	public CsparqlComponentStatus getStatus() {
		return status;
	}
	
	public void setStatus(CsparqlComponentStatus status) {
		this.status = status;
	}
	
	public void feedStream(RdfQuadruple quadruple){
		stream.put(quadruple);
	}
}
