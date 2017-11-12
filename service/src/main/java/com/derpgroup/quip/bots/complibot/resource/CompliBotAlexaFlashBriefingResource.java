package com.derpgroup.quip.bots.complibot.resource;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
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
  private static final Set<String> BAD_QUIP_GROUPS;
  
  static {
    BAD_QUIP_GROUPS = new HashSet<String>();
    BAD_QUIP_GROUPS.add("SNEAKY_WINKY");
    BAD_QUIP_GROUPS.add("PURRFECT");
    BAD_QUIP_GROUPS.add("CREATIVITY");
    BAD_QUIP_GROUPS.add("TALKING");
    BAD_QUIP_GROUPS.add("BELIEVE_IN_YOU");
  }
  
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
    while(BAD_QUIP_GROUPS.contains(compliment.getQuipGroup())){
      LOG.info("Generated an improper quip of group '" + compliment.getQuipGroup() + "', rerolling.");
      compliment = quipStore.getRandomCompliment();
    }
    
    dailyCompliment.setUid(UUID.randomUUID().toString());
    dailyCompliment.setTitleText("A Compliment from CompliBot! For more, click below.");
    dailyCompliment.setMainText(compliment.getText());
    dailyCompliment.setUpdateDate(Date.from(Instant.now()));
    dailyCompliment.setRedirectionUrl("http://www.3po-labs.com/bots.html#0");
  }
  
  public static LocalDateTime calculateMidnightUTC(){

    LocalDate date = LocalDate.now(ZoneId.of("UTC"));
    LocalTime time = LocalTime.MIDNIGHT;
    return LocalDateTime.of(date, time);
  }
}
