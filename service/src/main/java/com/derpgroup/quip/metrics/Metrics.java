package com.derpgroup.quip.metrics;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.derpgroup.quip.configuration.QuipConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Metrics {

  private final static Logger LOG = LoggerFactory.getLogger(Metrics.class);

  private static boolean initialized = false;
  private static QuipMetrics metrics = new QuipMetrics();
  private static String metricsFile;

  private Metrics(){}

  public static synchronized void init(QuipConfig config) throws IOException{
    if(initialized){
      throw new RuntimeException("Metrics is already initialized");
    }
    if(config==null){return;}
    metricsFile = config.getMetricsFile();

    try{
      loadMetricsFromFile(metricsFile);
    }catch(IOException e){
      LOG.warn("Unable to load metrics from metrics file",e);
    }

    initialized = true;
    LOG.info("Metrics initialized");
  }

  protected static QuipMetrics loadMetricsFromFile(String fileName) throws IOException{
    String content = new String(Files.readAllBytes(Paths.get(fileName)),Charset.defaultCharset());
    return new ObjectMapper().readValue(content, new TypeReference<QuipMetrics>(){});
  }

  public static void saveMetrics() throws IOException{
    saveMetricsToFile(metricsFile);
  }

  public static synchronized void saveMetricsToFile(String fileName) throws IOException {
    final File file = new File(fileName);
    file.createNewFile();

    ObjectMapper mapper = new ObjectMapper();
    String content = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(metrics);

    FileUtils.writeStringToFile(file, content, Charset.defaultCharset());
  }

  // All the synchronized incrementers
  public static synchronized void incrementComplibotRequests(){
    metrics.setComplibotRequests(metrics.getComplibotRequests()+1);
  }
  public static synchronized void incrementComplimentRequests(){
    metrics.setComplimentRequests(metrics.getComplimentRequests()+1);
  }
  public static synchronized void incrementTargetableComplimentRequests(){
    metrics.setTargetableComplimentRequests(metrics.getTargetableComplimentRequests()+1);
  }
  public static synchronized void incrementWinsultRequests(){
    metrics.setWinsultRequests(metrics.getWinsultRequests()+1);
  }
  public static synchronized void incrementTargetableWinsultRequests(){
    metrics.setTargetableWinsultRequests(metrics.getTargetableWinsultRequests()+1);
  }
  public static synchronized void incrementInsultibotRequests(){
    metrics.setInsultibotRequests(metrics.getInsultibotRequests()+1);
  }
  public static synchronized void incrementInsultRequests(){
    metrics.setInsultRequests(metrics.getInsultRequests()+1);
  }
  public static synchronized void incrementTargetableInsultRequests(){
    metrics.setTargetableInsultRequests(metrics.getTargetableInsultRequests()+1);
  }
  public static synchronized void incrementBackhandedComplimentRequests(){
    metrics.setBackhandedComplimentRequests(metrics.getBackhandedComplimentRequests()+1);
  }
  public static synchronized void incrementTargetableBackhandedComplimentRequests(){
    metrics.setTargetableBackhandedComplimentRequests(metrics.getTargetableBackhandedComplimentRequests()+1);
  }
  public static synchronized void incrementHelpRequests(){
    metrics.setHelpRequests(metrics.getHelpRequests()+1);
  }
  public static synchronized void incrementStopRequests(){
    metrics.setStopRequests(metrics.getStopRequests()+1);
  }
  public static synchronized void incrementCancelRequests(){
    metrics.setCancelRequests(metrics.getCancelRequests()+1);
  }
  public static synchronized void incrementAnotherRequests(){
    metrics.setAnotherRequests(metrics.getAnotherRequests()+1);
  }
  public static synchronized void incrementWhoBuiltYouRequests(){
    metrics.setWhoBuiltYouRequests(metrics.getWhoBuiltYouRequests()+1);
  }
  public static synchronized void incrementFriendsRequests(){
    metrics.setFriendsRequests(metrics.getFriendsRequests()+1);
  }
  public static synchronized void incrementWhoIsRequests(){
    metrics.setWhoIsRequests(metrics.getWhoIsRequests()+1);
  }
  public static synchronized void incrementWhoIsCompliBotRequests(){
    metrics.setWhoIsCompliBotRequests(metrics.getWhoIsCompliBotRequests()+1);
  }
  public static synchronized void incrementWhoIsInsultiBotRequests(){
    metrics.setWhoIsInsultiBotRequests(metrics.getWhoIsInsultiBotRequests()+1);
  }
}
