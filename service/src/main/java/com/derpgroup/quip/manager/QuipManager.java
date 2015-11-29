package com.derpgroup.quip.manager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.derpgroup.derpwizard.manager.AbstractManager;
import com.derpgroup.derpwizard.voice.exception.DerpwizardException;
import com.derpgroup.derpwizard.voice.model.ConversationHistoryEntry;
import com.derpgroup.derpwizard.voice.model.SsmlDocumentBuilder;
import com.derpgroup.derpwizard.voice.model.VoiceInput;
import com.derpgroup.derpwizard.voice.util.ConversationHistoryUtils;
import com.derpgroup.quip.MixInModule;
import com.derpgroup.quip.QuipMetadata;
import com.derpgroup.quip.model.Quip;
import com.derpgroup.quip.model.QuipStore;

public class QuipManager extends AbstractManager {
  private final Logger LOG = LoggerFactory.getLogger(QuipManager.class);
  
  static{
    ConversationHistoryUtils.getMapper().registerModule(new MixInModule());
  }
  
  private static final String[] metaRequestSubjects = new String[]{"ANOTHER"};
  protected static final int MAXIMUM_QUIP_HISTORY_SIZE = 10;
  protected static final double MAXIMUM_QUIP_HISTORY_PERCENT = .5;
  private static final int MAX_QUIP_REROLLS = 10;
  
  public QuipManager(){
    super();
  }
  
  protected Quip getRandomQuip(QuipType quipType, Queue<String> recentlyUsedQuips){
    QuipStore quipStore = QuipStore.getInstance();
    LOG.debug("Recently used "+quipType.toString().toLowerCase()+"s in user session:\n"+recentlyUsedQuips.toString());
    int maxQuipHistorySize = determineMaxQuipHistorySize(quipStore.getQuips(quipType).size());
    Quip quip = quipStore.getRandomQuip(quipType);
    int rerolls = 0;
    while(recentlyUsedQuips.contains(quip.getQuipGroup()) && rerolls < MAX_QUIP_REROLLS){
      quip = quipStore.getRandomQuip(quipType);
      rerolls++;
    }
    recentlyUsedQuips.add(quip.getQuipGroup());
    while(recentlyUsedQuips.size() > maxQuipHistorySize){
      recentlyUsedQuips.remove();
    }
    LOG.debug(quipType.toString().toLowerCase()+" being used ("+quip.getQuipGroup()+"): "+quip.getText());
    return quip;
  }
  
  protected Quip doInsultRequest(Map<String, String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    Queue<String> insultsUsed = metadata.getInsultsUsed();
    Quip quip = getRandomQuip(QuipType.INSULT, insultsUsed);
    
    builder.text(quip.getSsml());
    builder.setFullTextMessage(quip.getText());
    builder.setShortFormTextMessage("InsultiBot insult");
    return quip;
  }

  protected Quip doComplimentRequest(Map<String, String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    Queue<String> complimentsUsed = metadata.getComplimentsUsed();
    Quip quip = getRandomQuip(QuipType.COMPLIMENT, complimentsUsed);
    
    builder.text(quip.getSsml());
    builder.setFullTextMessage(quip.getText());
    builder.setShortFormTextMessage("CompliBot compliment");
    return quip;
  }

  protected Quip doBackhandedComplimentRequest(Map<String, String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    Queue<String> backhandedComplimentsUsed = metadata.getBackhandedComplimentsUsed();
    Quip quip = getRandomQuip(QuipType.BACKHANDED_COMPLIMENT, backhandedComplimentsUsed);
    
    builder.text(quip.getSsml());
    builder.setFullTextMessage(quip.getText());
    builder.setShortFormTextMessage("InsultiBot compliment");
    return quip;
  }

  protected Quip doWinsultRequest(Map<String, String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    Queue<String> winsultsUsed = metadata.getWinsultsUsed();
    Quip quip = getRandomQuip(QuipType.WINSULT, winsultsUsed);
    
    builder.text(quip.getSsml());
    builder.setFullTextMessage(quip.getText());
    builder.setShortFormTextMessage("CompliBot insult");
    return quip;
  }
  
  protected static int determineMaxQuipHistorySize(int sizeOfQuipGroup){
    return Math.min(MAXIMUM_QUIP_HISTORY_SIZE, (int) (sizeOfQuipGroup*MAXIMUM_QUIP_HISTORY_PERCENT));
  }

  @Override
  protected void doHelpRequest(VoiceInput voiceInput, SsmlDocumentBuilder builder) {
    QuipMetadata metadata = (QuipMetadata) voiceInput.getMetadata();
    doHelpRequest(voiceInput.getMessageAsMap(), builder, metadata);
  }
  
  protected void doHelpRequest(Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata){
    String bot = metadata.getBot();
    String s1, s2, s3, s4;
    if(StringUtils.isEmpty(bot)){
      builder.text("I don't have any help topics for this situation.");
      return;
    }
    switch (bot) {
    case "complibot":
      s1 = "Complibot";
      s2 = "nice";
      s3 = "awesome";
      s4 = "praise";
      break;
    case "insultibot":
      s1 = "Insultibot";
      s2 = "mean";
      s3 = "terrible";
      s4 = "shade";
      break;
    default:
      builder.text("I don't have any help topics for the bot named '" + bot + "'.");
      return;
    }
    builder.text("You can just say ").pause()
        .text(String.format("open %s ", s1)).pause().text("or ").pause()
        .text(String.format("launch %s ", s1)).pause()
        .text(String.format("and I'll say something %s about you!", s2))
        .endSentence();
    builder
        .text(
            String.format(
                " Once I've told you how %s you are, you can just say ", s3))
        .pause().text("another ").pause().text("or ").pause().text("again ")
        .pause().text(String.format("to get more %s.", s4)).endSentence();
  }

  @Override
  protected void doHelloRequest(VoiceInput voiceInput, SsmlDocumentBuilder builder) {

    Map<String,String> messageMap = voiceInput.getMessageAsMap();
    QuipMetadata metadata = (QuipMetadata) voiceInput.getMetadata();
    doDefaultRequest(messageMap, builder, metadata);
  }

  public void doDefaultRequest(Map<String, String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    String bot = metadata.getBot();
    if(bot == null){
      builder.text("I don't know how to handle requests for unnamed bots.");
      return;
    }
    switch (bot) {
    case "complibot":
      doComplimentRequest(messageMap, builder, metadata);
      break;
    case "insultibot":
      doInsultRequest(messageMap, builder, metadata);
      break;
    default:
      builder.text("I don't know how to handle requests for the bot named '" + bot + "'.");
      break;
    }
  }

  @Override
  protected void doGoodbyeRequest(VoiceInput voiceInput,
      SsmlDocumentBuilder builder) {
  }

  @Override
  protected void doConversationRequest(VoiceInput voiceInput,
      SsmlDocumentBuilder builder) throws DerpwizardException {

    Map<String,String> messageMap = voiceInput.getMessageAsMap();
    QuipMetadata metadata = (QuipMetadata) voiceInput.getMetadata();
    String messageSubject = voiceInput.getMessageSubject();

    switchOnSubject(messageSubject, messageMap, builder, metadata);
  }

  public void switchOnSubject(String messageSubject, Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) throws DerpwizardException {
    switch (messageSubject) {
    case "COMPLIMENT":
      doComplimentRequest(messageMap, builder, metadata);
      break;
    case "INSULT":
      doInsultRequest(messageMap, builder, metadata);
      break;
    case "BACKHANDED_COMPLIMENT":
      doBackhandedComplimentRequest(messageMap, builder, metadata);
      break;
    case "WINSULT":
      doWinsultRequest(messageMap, builder, metadata);
      break;
    case "WHO_BUILT_YOU":
      doWhoBuiltYouRequest(messageMap, builder, metadata);
      break;
    case "WHAT_DO_YOU_DO":
      doWhatDoYouDoRequest(messageMap, builder, metadata);
      break;
    case "FRIENDS":
      doFriendsRequest(messageMap, builder, metadata);
      break;
    case "WHO_IS":
      doWhoIsRequest(messageMap, builder, metadata);
      break;
    case "HELP":
      doHelpRequest(messageMap, builder, metadata);
      break;
    case "CANCEL": //Placeholders until we decide how to actually use these two request types
      doCancelRequest();
      break;
    case "STOP":
      doStopRequest();
      break;
    case "ANOTHER":
      doAnotherRequest(messageSubject, messageMap, builder, metadata);
      break;
    case "START_OF_CONVERSATION":
      doDefaultRequest(messageMap, builder, metadata);
      break;
    case "END_OF_CONVERSATION":
      doStopRequest();
      break;
    default:
      String message = "Unknown request type '" + messageSubject + "'.";
      LOG.warn(message);
      throw new DerpwizardException(new SsmlDocumentBuilder().text(message).build().getSsml(), message, "Unknown request.");
    }
  }

  protected void doAnotherRequest(String messageSubject, Map<String, String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) throws DerpwizardException {
    //this has its own method in case we want to do things like logging
    ConversationHistoryEntry entry = ConversationHistoryUtils.getLastNonMetaRequestBySubject(metadata.getConversationHistory(), new HashSet<String>(Arrays.asList(metaRequestSubjects)));
    if(entry == null){
      doDefaultRequest(messageMap, builder, metadata);
      return;
    }
    switchOnSubject(entry.getMessageSubject(), messageMap, builder, metadata);
  }

  protected void doWhoIsRequest(Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    String bot = metadata.getBot();
    String botInQuestion = messageMap.get("botName");
    if(StringUtils.isEmpty(bot) || StringUtils.isEmpty(botInQuestion)){
      builder.text("I don't have any info for this situation.");
      return;
    }
    switch(bot){
    case "complibot":
      if(botInQuestion.equals(bot)){
        builder.text("That's me!  ").endSentence();
        doWhatDoYouDoRequest(messageMap, builder, metadata);
      }else if(botInQuestion.equals("insultibot")){
        builder.text("That's my bestie.  ").endSentence().text("It can act grumpy sometimes, but it has a heart of gold.").endSentence();
      }else{
        builder.text("I don't know that bot, but I bet it's awesome.").endSentence();
      }
      break;
    case "insultibot":
      if(botInQuestion.equals(bot)){
        builder.text("Are you trolling me?  ").endSentence().text("That's me.  ").endSentence();
        doWhatDoYouDoRequest(messageMap, builder, metadata);
      }else if(botInQuestion.equals("complibot")){
        builder.text("That's the annoyingly cheerful bot that won't shut up.").endSentence();
      }else{
        builder.text("I don't know that bot, and I'm perfectly fine with that.").endSentence();
      }
      break;
      default:
        builder.text("I don't have any info for the bot named '" + bot + "'.").endSentence();
        return;
    }
  }

  protected void doFriendsRequest(Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    String bot = metadata.getBot();
    if(StringUtils.isEmpty(bot)){
      builder.text("I don't have any info for this situation.");
      return;
    }
    switch(bot){
    case "complibot":
      builder.text("Well, you are my absolute best friend, but I'm also good pals with <phoneme alphabet=\"ipa\" ph=\"InsʌltIbɒt\">InsultiBot</phoneme>.  ")
      .endSentence().text("You should check it out!").endSentence();
      break;
    case "insultibot":
      builder.text("I don't have any friends.").endSentence().text("I don't know anyone else other than <phoneme alphabet=\"ipa\" ph=\"kɒmplIbɒt\"> CompliBot </phoneme>,")
      .pause().text(" and it's even more annoying than you are.  ").endSentence().text("You two would get along well.").endSentence();
      break;
      default:
        builder.text("I don't have that info for the bot named '" + bot + "'.").endSentence();
        return;
    }
  }

  protected void doWhatDoYouDoRequest(Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    String bot = metadata.getBot();
    if(StringUtils.isEmpty(bot)){
      builder.text("I don't have any info for this situation.");
      return;
    }
    switch(bot){
    case "complibot":
      builder.text("I give you compliments and tell you how awesome you are, ").pause().text("and then I sit quietly and wait for you to talk to me again!").endSentence();
      break;
    case "insultibot":
      builder.text("I mainly just tell you how awful you are.  ").endSentence().text("Now, leave me alone.").endSentence();
      break;
      default:
        builder.text("I don't have any info for the bot named '" + bot + "'.").endSentence();
        return;
    }
  }

  protected void doWhoBuiltYouRequest(Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    String s1, s2;
    String bot = metadata.getBot();
    if(StringUtils.isEmpty(bot)){
      builder.text("I don't have any info for this situation.");
      return;
    }
    switch(bot){
    case "complibot":
      s1 = "gentlemen";
      s2 = "paragon of light";
      break;
    case "insultibot":
      s1 = "douche nozzles";
      s2 = "<phoneme alphabet=\"ipa\" ph=\"səmməbItch\">son of a bitch</phoneme>";
      break;
      default:
        builder.text("I don't have that info for the bot named '" + bot + "'.");
        return;
    }
    builder.text("I was built by the ").text(String.format("%s ",s1)).text("of derp group.").endSentence();
    builder.text("The group is made up of David, ").pause().text("Eric, ").pause().text("Rusty, ").pause().text(String.format("and that %s Paul.",s2)).endSentence();
  }

  @Override
  protected void doCancelRequest(VoiceInput voiceInput,
      SsmlDocumentBuilder builder) {
    doCancelRequest();
  }
  
  protected void doCancelRequest(){}

  @Override
  protected void doStopRequest(VoiceInput voiceInput,
      SsmlDocumentBuilder builder) {
    doStopRequest();
  }
  
  protected void doStopRequest(){}

}
