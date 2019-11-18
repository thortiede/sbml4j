# SBML4j

A Service oriented approach to persisting SBML formatted files in a Neo4j database.  

## Docker
This software is available as docker image:  
https://hub.docker.com/repository/docker/thortiede/sbml4j/general  
`docker pull thortiede/sbml4j:latest`

## Prerequisites:

1. Neo4j (tested with version 3.5.2)  
A running instance of the Neo4j Database needs to be available (configure connection details in src/main/resources/application-xxx.properties depending on the environment you intend to run)  
preferably use docker image (https://hub.docker.com/_/neo4j)
2. APOC neo4j plugin  
The /context endpoint uses the APOC neo4j plugins for perfoming the BFS.  
tested with 3.5.0.4: https://github.com/neo4j-contrib/neo4j-apoc-procedures/releases/tag/3.5.0.4  
3. Create docker network (optional)  
In order to isolate the communication between the sbml4j application docker and the neo4j docker containers, create a network to run them in:  
`docker network create sbml4jnet`

## Setup and Running SBML4j:

1. Run the neo4j docker image with plugins volume mounted:  
`docker run -it --rm --detach --publish=7474:7474 --publish=7687:7687 --name=sbml4jsimple --net sbml4jnet --volume=~/neo4j/data:/data --volume=~/neo4j/plugins:/plugins --volume=~/neo4j/conf:/var/lib/neo4j/conf --volume=~/neo4j/logs:/logs neo4j:3.5.2`

2. run the sbml4j docker image  
`docker run -it --rm --detach -e "SPRING_PROFILES_ACTIVE=test" --publish=8080:8080 --volume=~/sbml4j/logs:/logs --name sbml4j --net sbml4jnet thortiede/sbml4j:latest`

## Open help page for basic endpoint-documentation:  
`http://localhost:8080/sbml4j`
