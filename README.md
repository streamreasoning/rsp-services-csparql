rsp-service-csparql
===========

Implementatons of rsp-services interfaces for C-SPARQL Engine
This project implements rsp-services components, go to https://github.com/streamreasoning/rsp-services to have more informations
rsp-service-csparql implements a complete RESTful server to remotely control a C-SPARQL Engine

Services

# Subject # | # Call #  |  # URL #  |  # Description #  |  # Body Parameters #
---------|-------|-------|---------------|-------------------
RDF Stream  | GET  | /streams |  Retrieve the information about all the registered streams  |  
RDF Stream  | GET  | /streams/{streamname} |  Retrieve the information about the stream {streamname}  |  
RDF Stream  | PUT  | /streams/{streamname} |  Register a new RDF stream named {streamname} on the csparql engine  |  # streamIri #: the sGraph (Stream Descriptor) IRI
RDF Stream  | POST  | /streams/{streamname} |  Push a payload on {streamname}  |  # payload #: the information to push on the RDF Stream. it must be a JSON-LD serialization of an RDF Graph
RDF Stream  | DELETE | /streams/{streamname} |  Remove RDF Stream from engine  | 


## Run with Docker

Run `mvn clean package docker:build` to build the docker image. 
The [Spotify Docker Maven Plugin](https://github.com/spotify/docker-maven-plugin) is used.
Finally run it via `docker run -p 8175:8175 -d streamreasoning/rsp-services-csparql`.