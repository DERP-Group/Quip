package com.derpgroup.quip.model;

import java.io.IOException;
import java.nio.charset.Charset;
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
import com.derpgroup.quip.manager.QuipType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QuipStore {
  private final Logger LOG = LoggerFactory.getLogger(QuipStore.class);
  
  private static QuipStore instance;
  private boolean initialized = false;
  
  Map<QuipType, String> sourceFiles = new HashMap<QuipType,String>();
  Map<QuipType, Instant> lastUpdateTimes = new HashMap<QuipType,Instant>();
  Map<QuipType, List<Quip>> quips = new HashMap<QuipType,List<Quip>>();
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
    if(config==null){return;}
    sourceFiles.put(QuipType.COMPLIMENT, config.getComplimentsFile());
    sourceFiles.put(QuipType.WINSULT, config.getWinsultsFile());
    sourceFiles.put(QuipType.INSULT, config.getInsultsFile());
    sourceFiles.put(QuipType.BACKHANDED_COMPLIMENT, config.getBackhandedComplimentsFile());
    refreshRate = config.getRefreshRate();
    initialized = true;
    try{
      updateQuips(QuipType.COMPLIMENT);
      updateQuips(QuipType.WINSULT);
      updateQuips(QuipType.INSULT);
      updateQuips(QuipType.BACKHANDED_COMPLIMENT);
      LOG.info("QuipStore initialized");
    }
    catch(IOException e){
      LOG.error("There was a problem loading quips",e);
    }
  }
  
  protected void updateQuips(QuipType quipType) throws IOException{
    if(!initialized){throw new RuntimeException("QuipStore must be initialized before use");}

    synchronized(quipType){
      Instant lastUpdateTime = lastUpdateTimes.get(quipType);
      if(lastUpdateTime!=null && !lastUpdateTime.isBefore(Instant.now().minusSeconds(refreshRate))){
        return;
      }
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
    String content = new String(Files.readAllBytes(Paths.get(fileName)),Charset.defaultCharset());
    return new ObjectMapper().readValue(content, new TypeReference<List<Quip>>(){});
  }
  
  public Quip getRandomQuip(QuipType quipType){
    if(!initialized){throw new RuntimeException("QuipStore must be initialized before use");}
    Instant lastUpdateTime = lastUpdateTimes.get(quipType);
    if(lastUpdateTime==null || lastUpdateTime.isBefore(Instant.now().minusSeconds(refreshRate))){
      try {
        updateQuips(quipType);
      } catch (IOException e) {
        LOG.error("There was a problem loading "+ quipType +" quips",e);
      }
    }
    List<Quip> quipList = quips.get(quipType);
    return quipList.get(new Random().nextInt(quipList.size()));
  }
  
  public Quip getRandomCompliment(){return getRandomQuip(QuipType.COMPLIMENT);}
  public Quip getRandomWinsult(){return getRandomQuip(QuipType.WINSULT);}
  public Quip getRandomInsult(){return getRandomQuip(QuipType.INSULT);}
  public Quip getRandomBackhandedCompliment(){return getRandomQuip(QuipType.BACKHANDED_COMPLIMENT);}
}
