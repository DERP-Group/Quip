package com.derpgroup.quip.bots.complibot.resource;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.derpgroup.derpwizard.voice.model.alexa.FlashBriefingResponse;
import com.derpgroup.quip.configuration.MainConfig;
import com.derpgroup.quip.model.Quip;
import com.derpgroup.quip.model.QuipStore;

import io.dropwizard.setup.Environment;

/**
 * REST APIs for requests generating from Amazon Alexa
 *
 * @author Eric
 * @since 0.0.1
 */
@Path("/complibot/alexa/flashbriefing")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompliBotAlexaFlashBriefingResource {

  private static final Logger LOG = LoggerFactory.getLogger(CompliBotAlexaFlashBriefingResource.class);
  private QuipStore quipStore;
  
  private FlashBriefingResponse dailyCompliment;
  private Instant ttl;
  
  public CompliBotAlexaFlashBriefingResource(MainConfig config, Environment env) {
    quipStore = QuipStore.getInstance();
    updateDailyCompliment();
  }
  
  @GET
  public FlashBriefingResponse getCompliment(){
    Instant now = Instant.now();
    if(now.compareTo(ttl) >= 0){
      updateDailyCompliment();
    }
    return dailyCompliment;
  }

  private void updateDailyCompliment() {
    if(dailyCompliment == null){
      dailyCompliment = new FlashBriefingResponse();
    }
    
    LocalDateTime midnight = calculateMidnightUTC();
    ttl = midnight.plusDays(1).toInstant(ZoneOffset.UTC);
    
    Quip compliment = quipStore.getRandomCompliment();
    
    dailyCompliment.setUid(UUID.randomUUID().toString());
    dailyCompliment.setTitle("A Compliment from CompliBot!");
    dailyCompliment.setMainText(compliment.getSsml());
    dailyCompliment.setUpdateDate(Date.from(Instant.now()));
    dailyCompliment.setRedirectionUrl("http://www.3po-labs.com");
  }
  
  public static LocalDateTime calculateMidnightUTC(){

    LocalDate date = LocalDate.now(ZoneId.of("UTC"));
    LocalTime time = LocalTime.MIDNIGHT;
    return LocalDateTime.of(date, time);
  }
}
