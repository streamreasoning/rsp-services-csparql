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
package it.polimi.deib.rsp_services_csparql.queries.utilities;

import java.util.Observer;

public class Csparql_Observer_Descriptor {
	
	private String id;
	private Observer observer;
	
	public Csparql_Observer_Descriptor(String id, Observer observer) {
		super();
		this.id = id;
		this.observer = observer;
	}
	
	public Csparql_Observer_Descriptor() {
		super();
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public Observer getObserver() {
		return observer;
	}
	
	public void setObserver(Observer observer) {
		this.observer = observer;
	}
	
	

}
