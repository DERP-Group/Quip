package com.derpgroup.quip.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.derpgroup.quip.configuration.QuipConfig;
import com.derpgroup.quip.manager.QuipRequestTypes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QuipStore {
  private final Logger LOG = LoggerFactory.getLogger(QuipStore.class);
  
  private static QuipStore instance;
  private boolean initialized = false;
  
  Map<QuipRequestTypes, String> sourceFiles = new HashMap<QuipRequestTypes,String>();
  Map<QuipRequestTypes, Instant> lastUpdateTimes = new HashMap<QuipRequestTypes,Instant>();
  Map<QuipRequestTypes, List<Quip>> quips = new HashMap<QuipRequestTypes,List<Quip>>();
  int refreshRate;  // seconds

  private QuipStore(){}
  
  public static synchronized QuipStore getInstance(){
    if(instance == null){
      instance = new QuipStore();
    }
    return instance;
  }
  
  public synchronized void init(QuipConfig config){
    if(initialized){
      throw new RuntimeException("QuipStore is already initialized");
    }
    sourceFiles.put(QuipRequestTypes.COMPLIMENT, config.getComplimentsFile());
    sourceFiles.put(QuipRequestTypes.WINSULT, config.getWinsultsFile());
    sourceFiles.put(QuipRequestTypes.INSULT, config.getInsultsFile());
    sourceFiles.put(QuipRequestTypes.BACKHANDED_COMPLIMENT, config.getBackhandedComplimentsFile());
    refreshRate = config.getRefreshRate();
    initialized = true;
    try{
      updateQuips(QuipRequestTypes.COMPLIMENT);
      updateQuips(QuipRequestTypes.WINSULT);
      updateQuips(QuipRequestTypes.INSULT);
      updateQuips(QuipRequestTypes.BACKHANDED_COMPLIMENT);
      LOG.info("QuipStore initialized");
    }
    catch(IOException e){
      LOG.error("There was a problem loading quips",e);
    }
  }
  
  protected void updateQuips(QuipRequestTypes quipType) throws IOException{
    if(!initialized){throw new RuntimeException("QuipStore must be initialized before use");}

    synchronized(quipType){
      int oldQuipCount = 0;
      if(quips.get(quipType)!=null){
        oldQuipCount = quips.get(quipType).size();
      }

      quips.put(quipType,readQuipsFromFile(sourceFiles.get(quipType)));
      lastUpdateTimes.put(quipType,Instant.now());

      if(oldQuipCount != quips.get(quipType).size()){
        LOG.info(quipType+" quips updated from "+oldQuipCount+" quips to "+quips.get(quipType).size()+" quips");
      }
    }
  }
  
  protected List<Quip> readQuipsFromFile(String fileName) throws IOException{
    String content = new String(Files.readAllBytes(Paths.get(fileName)));
    ObjectMapper mapper = new ObjectMapper();
    List<Quip> quips = mapper.readValue(content, new TypeReference<List<Quip>>(){});
    
    return quips;
  }
  
  public Quip getRandomQuip(QuipRequestTypes quipType){
    if(!initialized){throw new RuntimeException("QuipStore must be initialized before use");}
    Instant lastUpdateTime = lastUpdateTimes.get(quipType);
    if(lastUpdateTime==null || lastUpdateTime.minusSeconds(refreshRate).isBefore(Instant.now())){
      try {
        updateQuips(quipType);
      } catch (IOException e) {
        LOG.error("There was a problem loading "+ quipType +" quips",e);
      }
    }
    List<Quip> quipList = quips.get(quipType);
    Quip quip = quipList.get(new Random().nextInt(quipList.size())); 
    return quip;
  }
  
  public Quip getRandomCompliment(){return getRandomQuip(QuipRequestTypes.COMPLIMENT);}
  public Quip getRandomWinsult(){return getRandomQuip(QuipRequestTypes.WINSULT);}
  public Quip getRandomInsult(){return getRandomQuip(QuipRequestTypes.INSULT);}
  public Quip getRandomBackhandedCompliment(){return getRandomQuip(QuipRequestTypes.BACKHANDED_COMPLIMENT);}
}
