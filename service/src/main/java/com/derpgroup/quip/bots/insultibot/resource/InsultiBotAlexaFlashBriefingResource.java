package com.derpgroup.quip.bots.insultibot.resource;

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
@Path("/insultibot/alexa/flashbriefing")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InsultiBotAlexaFlashBriefingResource {

  private static final Logger LOG = LoggerFactory.getLogger(InsultiBotAlexaFlashBriefingResource.class);
  private static final Set<String> BAD_QUIP_GROUPS;
  
  static {
    BAD_QUIP_GROUPS = new HashSet<String>();
    BAD_QUIP_GROUPS.add("TALK_TO_YOU");
    BAD_QUIP_GROUPS.add("PITBULL");
    BAD_QUIP_GROUPS.add("NOTHING_AT_ALL");
    BAD_QUIP_GROUPS.add("BIRTHDAY");
    BAD_QUIP_GROUPS.add("BATTERY_LOVE");
    BAD_QUIP_GROUPS.add("BLOCK_LIST");
    BAD_QUIP_GROUPS.add("COMPLIBOT");
    BAD_QUIP_GROUPS.add("CROCS_N_SOCKS");
    BAD_QUIP_GROUPS.add("HATE_THE_GAME");
    BAD_QUIP_GROUPS.add("LEAVE_ME_ALONE");
  }
  
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
    while(BAD_QUIP_GROUPS.contains(insult.getQuipGroup())){
      LOG.info("Generated an improper quip of group '" + insult.getQuipGroup() + "', rerolling.");
      insult = quipStore.getRandomInsult();
    }
    
    dailyInsult.setUid(UUID.randomUUID().toString());
    dailyInsult.setTitleText("An insult from InsultiBot! For more, click below.");
    dailyInsult.setMainText(insult.getText());
    dailyInsult.setUpdateDate(Date.from(Instant.now()));
    dailyInsult.setRedirectionUrl("http://www.3po-labs.com/bots.html#1");
  }
  
  public static LocalDateTime calculateMidnightUTC(){

    LocalDate date = LocalDate.now(ZoneId.of("UTC"));
    LocalTime time = LocalTime.MIDNIGHT;
    return LocalDateTime.of(date, time);
  }
}
