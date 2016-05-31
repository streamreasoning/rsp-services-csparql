rsp-service-csparql
===========

Implementatons of rsp-services interfaces for C-SPARQL Engine
This project implements rsp-services components, go to https://github.com/streamreasoning/rsp-services to have more informations
rsp-service-csparql implements a complete RESTful server to remotely control a C-SPARQL Engine

Services

Subject | Call  |  URL  |  Description                |  Body Parameters
---------|-------|-------|----------------------------|-------------------
RDF Streams  | GET  | */streams* |  Retrieve the information about all the registered streams  |  
RDF Stream  | GET  | */streams/{streamname}* |  Retrieve the information about the stream {streamname}  |  
RDF Stream  | PUT  | */streams/{streamname}* |  Register a new RDF stream named {streamname} on the csparql engine  |  **streamIri**: the sGraph (Stream Descriptor) IRI
RDF Stream  | POST  | */streams/{streamname}* |  Push a payload on {streamname}  | **payload**: the information to push on the RDF Stream. it must be a JSON-LD serialization of an RDF Graph
RDF Stream  | DELETE | */streams/{streamname}* |  Remove RDF Stream from engine  | 
C-SPARQL Queries  | GET | *queries/{queryname}* |  Retrieve the information about {queryname}  |
C-SPARQL Query  | GET | */queries* |  Retrieve the information about all the registered queries  |
C-SPARQL Query  | PUT | */queries/{queryname}* |   Register a new continupus query named {queryname} on the csparql engine  | **queryBody**: The body of the C-SPARQL query
C-SPARQL Query  | POST | */queries/{queryname}* |   Modify the status of the query based on action parameter  | **action**: The action to perform on the query (*pause, restart or addobserver*) if action is addobserver, the engine needs **host** and **port** parameter to set up the websocket
C-SPARQL Query  | DELETE | */queries/{queryname}* |  Remove Query from engine  | 
Observers  | GET | *queries/{queryname}/observervers* |  Retrieve the information about all the observers related to {queryname} query  |
Observer  | GET | *queries/{queryname}/observervers/{obsid}* |  Retrieve the information about the observer {obs id of the query {queryname}  |
Observer  | DELETE | *queries/{queryname}/observervers/{obsid}* | Remove the observer {obs id of the query {queryname}  |


## Run with Docker

Run `mvn clean package docker:build` to build the docker image. 
The [Spotify Docker Maven Plugin](https://github.com/spotify/docker-maven-plugin) is used.
Finally run it via `docker run -p 8175:8175 -d streamreasoning/rsp-services-csparql`.