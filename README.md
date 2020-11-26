# SBML4j

A Service oriented approach to persisting SBML formatted files in a Neo4j database.  

## Docker
This software is available as docker image:  
https://hub.docker.com/repository/docker/thortiede/sbml4j/general  
`docker pull thortiede/sbml4j:latest`

## Prerequisites:

1. Neo4j (minimum version 4.0), preferably use the docker image (https://hub.docker.com/_/neo4j)
A running instance of the Neo4j Database needs to be available (configure connection details in src/main/resources/application-xxx.properties depending on the environment you intend to run)  

2. APOC neo4j plugin  
The /context and /overview endpoint use the APOC neo4j plugins for perfoming the BFS and Dijkstra calculations.
You need to have the APOC plugin for neo4j database version 4.
The latest tested version is: https://github.com/neo4j-contrib/neo4j-apoc-procedures/releases/tag/4.1.0.2

3. Create docker network (optional)  
In order to isolate the communication between the sbml4j application docker and the neo4j docker containers, create a network to run them in:  
`docker network create sbml4jnet`

## Setup and Running SBML4j:

1. Run the neo4j docker image with plugins volume mounted:  
`docker run -it --rm --detach --publish=7474:7474 --publish=7687:7687 --name=sbml4jsimple --net sbml4jnet --volume=~/neo4j/data:/data --volume=~/neo4j/plugins:/plugins --volume=~/neo4j/conf:/var/lib/neo4j/conf --volume=~/neo4j/logs:/logs neo4j`

2. run the sbml4j docker image  
`docker run -it --rm --detach -e "SPRING_PROFILES_ACTIVE=test" --publish=8080:8080 --volume=~/sbml4j/logs:/logs --name sbml4j --net sbml4jnet thortiede/sbml4j:latest`

## API documentation
You can find the documentation for the REST API at swaggerhub:
`https://app.swaggerhub.com/apis-docs/tiede/sbml4j/1.1.4`

## Docker compose

You can find a docker-compose project over at
`https://github.com/thortiede/sbml4j-compose`

It will start both the neo4j an the sbml4j containers.
It includes a  preloaded database comprised of 40 cancer-related publilcy available KEGG pathways.
The kgml-files obtained from KEGG were translated using [KEGGtranslator][1].
After loading all pathway maps we created a CollectionPathway from which four network mappings were derived.
The mappings are enriched with drug-nodes from [Drugbank][2] and drug-target relationships which were fetched from a [MyDrug][3] instance using the /POST /networks/{UUID}/myDrug endpoint.
You can find the network mappings via the GET /networks endpoint using the user `pecax`.


[1]: Clemens Wrzodek, Andreas Dräger, and Andreas Zell. KEGGtranslator: visualizing and converting the KEGG PATHWAY database to various formats. Bioinformatics, 27(16):2314--2315, June 2011
[2]: https://go.drugbank.com/
[3]: Developed at the University of Tübingen, to be published
