# Change Log
All notable changes to this project will be documented in this file.
This document adheres to [keep-a-changelog].

## [0.4.8-modaclouds] - 2015-05-14

### Added
- the possibility to use a custom unmarshaller for input data (e.g.: to reduce network traffic)
- the possibility to use a custom marshaller to serialize data in different formats (e.g.: Graphite, InfluxDB)
- a new api to evaluate general query against the static KB
- the possibility to send data to observers via socket (tcp and udp) (e.g.: required by Graphite)

### Changed
- upgraded restlet version for multi thread support
- set the server maxThreads and the maxConnections to 500 to increase performances
- the api for adding an observer for REST compliance and to specify the marshalling format and the protocol

### Fixed
- temporary fix to allow inference rules to be used in the last version of the csparql-engine

[0.4.8-modaclouds]: https://github.com/streamreasoning/rsp-services-csparql/compare/0.4.8...0.4.8-modaclouds
[keep-a-changelog]: https://github.com/olivierlacan/keep-a-changelog
