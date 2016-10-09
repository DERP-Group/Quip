package com.derpgroup.quip.bots.insultibot.resource;

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
@Path("/insultibot/alexa/flashbriefing")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InsultiBotAlexaFlashBriefingResource {

  private static final Logger LOG = LoggerFactory.getLogger(InsultiBotAlexaFlashBriefingResource.class);
  private QuipStore quipStore;
  
  private FlashBriefingResponse dailyInsult;
  private Instant ttl;
  
  public InsultiBotAlexaFlashBriefingResource(MainConfig config, Environment env) {
    quipStore = QuipStore.getInstance();
    updateDailyCompliment();
  }
  
  @GET
  public FlashBriefingResponse getCompliment(){
    Instant now = Instant.now();
    if(now.compareTo(ttl) >= 0){
      updateDailyCompliment();
    }
    return dailyInsult;
  }

  private void updateDailyCompliment() {
    if(dailyInsult == null){
      dailyInsult = new FlashBriefingResponse();
    }
    
    LocalDateTime midnight = calculateMidnightUTC();
    ttl = midnight.plusDays(1).toInstant(ZoneOffset.UTC);
    
    Quip insult = quipStore.getRandomInsult();
    
    dailyInsult.setUid(UUID.randomUUID().toString());
    dailyInsult.setTitle("An insult from InsultiBot!");
    dailyInsult.setMainText(insult.getSsml());
    dailyInsult.setUpdateDate(Date.from(Instant.now()));
    dailyInsult.setRedirectionUrl("http://www.3po-labs.com");
  }
  
  public static LocalDateTime calculateMidnightUTC(){

    LocalDate date = LocalDate.now(ZoneId.of("UTC"));
    LocalTime time = LocalTime.MIDNIGHT;
    return LocalDateTime.of(date, time);
  }
}
