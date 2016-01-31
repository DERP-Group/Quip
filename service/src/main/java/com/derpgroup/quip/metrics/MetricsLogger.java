package com.derpgroup.quip.metrics;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.derpgroup.quip.configuration.QuipConfig;

public class MetricsLogger extends Thread{
  private final Logger LOG = LoggerFactory.getLogger(MetricsLogger.class);

  private Integer loggingInterval;
  
  public MetricsLogger(QuipConfig config){
    loggingInterval = config.getLoggingInterval();
  }
  
  /** 
   * This thread just loops infinitely and writes the QuipMetrics to a log file every X seconds
   */
  public void run(){
    while(true){
      try {
        Thread.sleep(1000*loggingInterval);
        Metrics.saveMetrics();
      } catch (InterruptedException | IOException e) {
        LOG.error(e.getMessage());
      }
    }
  }
}
