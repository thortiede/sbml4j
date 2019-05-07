package org.tts.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.common.GraphBaseEntity;
import org.tts.model.common.SBMLQualSpecies;
import org.tts.repository.common.GraphBaseEntityRepository;
import org.tts.repository.common.SBMLQualSpeciesRepository;

@Service
public class DatabaseCacheServiceImpl implements DatabaseCacheService {

	private List<GraphBaseEntity> allGraphBaseEntities;
	//private List<SBMLQualSpecies> allQualSpecies;
	
	private Map<String, SBMLQualSpecies> symbolQualSpeciesMap;
	
	private GraphBaseEntityRepository graphBaseEntityRepository;
	SBMLQualSpeciesRepository sbmlQualSpeciesRepository;
	
	@Autowired
	public DatabaseCacheServiceImpl(GraphBaseEntityRepository graphBaseEntityRepository,
			SBMLQualSpeciesRepository sbmlQualSpeciesRepository) {
		super();
		this.graphBaseEntityRepository = graphBaseEntityRepository;
		this.sbmlQualSpeciesRepository = sbmlQualSpeciesRepository;
	}

	@Override
	public Iterable<GraphBaseEntity> getAllEntities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<SBMLQualSpecies> getAllQualSpecies() {
		return sbmlQualSpeciesRepository.findAll();
	}

	@Override
	public Map<String, SBMLQualSpecies> getSymbolToQualSpeciesMap() {
		this.symbolQualSpeciesMap = new HashMap<>();
		getAllQualSpecies().forEach(species->{
									this.symbolQualSpeciesMap.put(species.getsBaseName(), species);});
		return symbolQualSpeciesMap;
	}

}
