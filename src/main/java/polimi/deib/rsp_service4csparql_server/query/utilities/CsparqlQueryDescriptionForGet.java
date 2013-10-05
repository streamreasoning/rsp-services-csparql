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

import java.util.Collection;

import polimi.deib.rsp_service4csparql_server.common.CsparqlComponentStatus;

public class CsparqlQueryDescriptionForGet {
	
	private String id;
	private String name;
	private String type;
	private Collection<String> streams;
	private String body;
	private CsparqlComponentStatus status;
	
	public CsparqlQueryDescriptionForGet() {
		super();
	}

	public CsparqlQueryDescriptionForGet(String id, String name, String type,
			Collection<String> streams, String body,CsparqlComponentStatus status) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.streams = streams;
		this.body = body;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public CsparqlComponentStatus getStatus() {
		return status;
	}

	public void setStatus(CsparqlComponentStatus status) {
		this.status = status;
	}
}
