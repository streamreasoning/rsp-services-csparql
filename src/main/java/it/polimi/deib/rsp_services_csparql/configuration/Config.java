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
package it.polimi.deib.rsp_services_csparql.configuration;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Config {
	private static Config _instance = null;
	private static final Logger logger = LoggerFactory.getLogger(Config.class); 

    private static Configurations configs = new Configurations();
	private static PropertiesConfiguration config;
	
	private Config(String propertiesFilePath){
		try {
			config = configs.properties(new File(propertiesFilePath));
			for (Iterator<String> iterator = config.getKeys(); iterator.hasNext();) {
				String property = iterator.next();
				String sysValue = System.getProperty(property);
				if (sysValue!=null)
					config.setProperty(property, sysValue);
			}
		} catch (Exception e) {
			logger.error("Error while reading the configuration file", e);
		}
	}
	
	public static void initialize(String propertiesFilePath){
		_instance = new Config(propertiesFilePath);
	}
	
	public static Config getInstance() throws Exception{
		if(_instance==null)
			throw new Exception("Please initialize the configuration object at server startup");
		return _instance;
	}

	public int getServerPort(){
		return config.getInt("csparql_server.port");
	}
	
	public String getHostName(){
		if(config.getString("csparql_server.host_name").endsWith("/"))
			return config.getString("csparql_server.host_name");
		else
			return config.getString("csparql_server.host_name") + "/";
	}
				
	public boolean getEnableTSFunction(){
		return config.getBoolean("csparql_engine.enable_timestamp_function");
	}
	
	public boolean getSendEmptyResultsProperty(){
		return config.getBoolean("csparql_engine.send_empty_results");
	}	
}
