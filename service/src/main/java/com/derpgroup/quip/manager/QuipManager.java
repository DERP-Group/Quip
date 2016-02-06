package com.derpgroup.quip.manager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.derpgroup.derpwizard.manager.AbstractManager;
import com.derpgroup.derpwizard.voice.exception.DerpwizardException;
import com.derpgroup.derpwizard.voice.model.ConversationHistoryEntry;
import com.derpgroup.derpwizard.voice.model.ServiceOutput;
import com.derpgroup.derpwizard.voice.model.SsmlDocumentBuilder;
import com.derpgroup.derpwizard.voice.model.VoiceInput;
import com.derpgroup.derpwizard.voice.util.ConversationHistoryUtils;
import com.derpgroup.quip.MixInModule;
import com.derpgroup.quip.QuipMetadata;
import com.derpgroup.quip.configuration.MainConfig;
import com.derpgroup.quip.logger.QuipLogger;
import com.derpgroup.quip.model.BotName;
import com.derpgroup.quip.model.Quip;
import com.derpgroup.quip.model.QuipStore;
import com.derpgroup.quip.model.QuipVoiceInput;
import com.derpgroup.quip.util.QuipUtil;

public class QuipManager extends AbstractManager {
  private final Logger LOG = LoggerFactory.getLogger(QuipManager.class);
  
  static{
    ConversationHistoryUtils.getMapper().registerModule(new MixInModule());
  }
  
  private static final String[] metaRequestSubjects = new String[]{"ANOTHER"};
  protected static final int MAXIMUM_QUIP_HISTORY_SIZE = 10;
  protected static final double MAXIMUM_QUIP_HISTORY_PERCENT = .5;
  private static final int MAX_QUIP_REROLLS = 10;
  private static final int MAX_TARGETABLE_QUIP_REROLLS = 20;
  private static final Map<String,String> botNameReplacements = ImmutableMap.of(
      "Complibot", "<phoneme alphabet=\"ipa\" ph=\"kɒmplIbɒt\"> CompliBot </phoneme>",
      "Insultibot","InsultaBot",
      "CompliBot", "<phoneme alphabet=\"ipa\" ph=\"kɒmplIbɒt\"> CompliBot </phoneme>",
      "InsultiBot","InsultaBot");
  
  private static final String COMPLIBOT_QUIP_FOLLOW_UP = "  Say the word <break />'another'<break /> if you want more.";
  private static final String INSULTIBOT_QUIP_FOLLOW_UP = "  There's more where that came from, just say the word <break />'another'.";
  private static final String QUIP_FOLLOW_UP_INTERMEDIATE = "  Want more?";
  private static final String QUIP_FOLLOW_UP_FINAL = "  More?";
  private static final String COMPLIBOT_META_FOLLOW_UP = "  What can I do for you now?";
  private static final String INSULTIBOT_META_FOLLOW_UP = "  Let's get this over with, what else do you need?";
  private static final String META_FOLLOW_UP_INTERMEDIATE = "  Anything else?";
  private static final String META_FOLLOW_UP_FINAL = "  What else?";
  
  private boolean handholdMode = false;
  
  public QuipManager(MainConfig config) {
    super();
    if((config != null) && (config.getHandholdMode() != null)){
      handholdMode = config.getHandholdMode();
    }
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
  
  protected Quip getRandomTargetableQuip(QuipType quipType, Queue<String> recentlyUsedQuips, String target){
    QuipStore quipStore = QuipStore.getInstance();
    LOG.debug("Recently used "+quipType.toString().toLowerCase()+"s in user session:\n"+recentlyUsedQuips.toString());
    int maxQuipHistorySize = determineMaxQuipHistorySize(quipStore.getQuips(quipType).size());
    
    Quip quip = null;
    int rerolls = 0;
    boolean needsToReroll = true;
    while(needsToReroll){
      quip = quipStore.getRandomQuip(quipType);
      rerolls++;
      boolean wasUsedRecently = recentlyUsedQuips.contains(quip.getQuipGroup());
      boolean hasHitMaxRerolls = rerolls >= MAX_TARGETABLE_QUIP_REROLLS;

      Map<String,String> expectedFieldsToReplace = new HashMap<String, String>();
      expectedFieldsToReplace.put("[TARGET]", target);
      quip = QuipUtil.substituteContent(quip, null, expectedFieldsToReplace);
      
      needsToReroll = !hasHitMaxRerolls && (!quip.isTargetable() || wasUsedRecently);
    }
    recentlyUsedQuips.add(quip.getQuipGroup());
    while(recentlyUsedQuips.size() > maxQuipHistorySize){
      recentlyUsedQuips.remove();
    }
    LOG.debug(quipType.toString().toLowerCase()+" being used ("+quip.getQuipGroup()+"): "+quip.getTargetableText());
    return quip;
  }
  
  protected Quip doTargetableComplimentRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) throws DerpwizardException {
    QuipMetadata outputMetadata = (QuipMetadata) serviceOutput.getMetadata();
    Map<String,String> messageMap = voiceInput.getMessageAsMap();
    if(!messageMap.containsKey("target") || StringUtils.isEmpty(messageMap.get("target"))){
      return doComplimentRequest(voiceInput, serviceOutput);
    }

    String target = messageMap.get("target");
    if(target.toLowerCase().equals("me")){
      return doComplimentRequest(voiceInput, serviceOutput);
    }
    
    target = target.substring(0,1).toUpperCase()+target.substring(1);
    Queue<String> complimentsUsed = outputMetadata.getComplimentsUsed();
    Quip quip = getRandomTargetableQuip(QuipType.COMPLIMENT, complimentsUsed, target);

    String plaintext = quip.getText();
    String ssml = quip.getSsml();
    String delayedVoiceSsml = null;
    if(quip.isTargetable()){
      plaintext = quip.getTargetableText();
      ssml = quip.getTargetableSsml();
    }
    
    if(handholdMode){
      int conversationLength = outputMetadata.getConversationHistory().size();
      delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.COMPLIBOT, true);
      ssml += getGradualBackoffSsmlSuffix(conversationLength, BotName.COMPLIBOT, true);
    }

    serviceOutput.getVisualOutput().setTitle("CompliBot compliment");
    serviceOutput.getVisualOutput().setText(plaintext);
    serviceOutput.getVoiceOutput().setPlaintext(plaintext);
    serviceOutput.getVoiceOutput().setSsmltext(ssml);
    if(!StringUtils.isEmpty(delayedVoiceSsml)){
      serviceOutput.getDelayedVoiceOutput().setPlaintext(delayedVoiceSsml);
      serviceOutput.getDelayedVoiceOutput().setSsmltext(delayedVoiceSsml);
    }
    return quip;
  }
  
  protected Quip doTargetableWinsultRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) throws DerpwizardException {
    QuipMetadata outputMetadata = (QuipMetadata) serviceOutput.getMetadata();
    Map<String,String> messageMap = voiceInput.getMessageAsMap();
    if(!messageMap.containsKey("target") || StringUtils.isEmpty(messageMap.get("target"))){
      return doWinsultRequest(voiceInput, serviceOutput);
    }

    String target = messageMap.get("target");
    if(target.toLowerCase().equals("me")){
      return doWinsultRequest(voiceInput, serviceOutput);
    }
    
    target = target.substring(0,1).toUpperCase()+target.substring(1);
    Queue<String> winsultsUsed = outputMetadata.getWinsultsUsed();
    Quip quip = getRandomTargetableQuip(QuipType.WINSULT, winsultsUsed, target);

    String plaintext = quip.getText();
    String ssml = quip.getSsml();
    String delayedVoiceSsml = null;
    if(quip.isTargetable()){
      plaintext = quip.getTargetableText();
      ssml = quip.getTargetableSsml();
    }
    
    if(handholdMode){
      int conversationLength = outputMetadata.getConversationHistory().size();
      delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.COMPLIBOT, true);
      ssml += getGradualBackoffSsmlSuffix(conversationLength, BotName.COMPLIBOT, true);
    }

    serviceOutput.getVisualOutput().setTitle("CompliBot insult");
    serviceOutput.getVisualOutput().setText(plaintext);
    serviceOutput.getVoiceOutput().setPlaintext(plaintext);
    serviceOutput.getVoiceOutput().setSsmltext(ssml);
    if(!StringUtils.isEmpty(delayedVoiceSsml)){
      serviceOutput.getDelayedVoiceOutput().setPlaintext(delayedVoiceSsml);
      serviceOutput.getDelayedVoiceOutput().setSsmltext(delayedVoiceSsml);
    }
    return quip;
  }
  
  protected Quip doTargetableInsultRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) throws DerpwizardException {
    QuipMetadata outputMetadata = (QuipMetadata) serviceOutput.getMetadata();
    Map<String,String> messageMap = voiceInput.getMessageAsMap();
    if(!messageMap.containsKey("target") || StringUtils.isEmpty(messageMap.get("target"))){
      return doInsultRequest(voiceInput, serviceOutput);
    }

    String target = messageMap.get("target");
    if(target.toLowerCase().equals("me")){
      return doInsultRequest(voiceInput, serviceOutput);
    }
    
    target = target.substring(0,1).toUpperCase()+target.substring(1);
    Queue<String> insultsUsed = outputMetadata.getInsultsUsed();
    Quip quip = getRandomTargetableQuip(QuipType.INSULT, insultsUsed, target);

    String plaintext = quip.getText();
    String ssml = quip.getSsml();
    String delayedVoiceSsml = null;
    if(quip.isTargetable()){
      plaintext = quip.getTargetableText();
      ssml = quip.getTargetableSsml();
    }
    
    if(handholdMode){
      int conversationLength = outputMetadata.getConversationHistory().size();
      delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.INSULTIBOT, true);
      ssml += getGradualBackoffSsmlSuffix(conversationLength, BotName.INSULTIBOT, true);
    }

    serviceOutput.getVisualOutput().setTitle("InsultiBot insult");
    serviceOutput.getVisualOutput().setText(plaintext);
    serviceOutput.getVoiceOutput().setPlaintext(plaintext);
    serviceOutput.getVoiceOutput().setSsmltext(ssml);
    if(!StringUtils.isEmpty(delayedVoiceSsml)){
      serviceOutput.getDelayedVoiceOutput().setPlaintext(delayedVoiceSsml);
      serviceOutput.getDelayedVoiceOutput().setSsmltext(delayedVoiceSsml);
    }
    return quip;
  }
  
  protected Quip doTargetableBackhandedComplimentRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) throws DerpwizardException {
    QuipMetadata outputMetadata = (QuipMetadata) serviceOutput.getMetadata();
    Map<String,String> messageMap = voiceInput.getMessageAsMap();
    if(!messageMap.containsKey("target") || StringUtils.isEmpty(messageMap.get("target"))){
      return doBackhandedComplimentRequest(voiceInput, serviceOutput);
    }

    String target = messageMap.get("target");
    if(target.toLowerCase().equals("me")){
      return doBackhandedComplimentRequest(voiceInput, serviceOutput);
    }
    
    target = target.substring(0,1).toUpperCase()+target.substring(1);
    Queue<String> backhandedComplimentsUsed = outputMetadata.getBackhandedComplimentsUsed();
    Quip quip = getRandomTargetableQuip(QuipType.BACKHANDED_COMPLIMENT, backhandedComplimentsUsed, target);

    String plaintext = quip.getText();
    String ssml = quip.getSsml();
    String delayedVoiceSsml = null;
    if(quip.isTargetable()){
      plaintext = quip.getTargetableText();
      ssml = quip.getTargetableSsml();
    }
    
    if(handholdMode){
      int conversationLength = outputMetadata.getConversationHistory().size();
      delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.INSULTIBOT, true);
      ssml += getGradualBackoffSsmlSuffix(conversationLength, BotName.INSULTIBOT, true);
    }
    
    serviceOutput.getVisualOutput().setTitle("InsultiBot compliment");
    serviceOutput.getVisualOutput().setText(plaintext);
    serviceOutput.getVoiceOutput().setPlaintext(plaintext);
    serviceOutput.getVoiceOutput().setSsmltext(ssml);
    if(!StringUtils.isEmpty(delayedVoiceSsml)){
      serviceOutput.getDelayedVoiceOutput().setPlaintext(delayedVoiceSsml);
      serviceOutput.getDelayedVoiceOutput().setSsmltext(delayedVoiceSsml);
    }
    return quip;
  }
  
  protected Quip doInsultRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) {
    QuipMetadata outputMetadata = (QuipMetadata) serviceOutput.getMetadata();
    Queue<String> insultsUsed = outputMetadata.getInsultsUsed();
    Quip quip = getRandomQuip(QuipType.INSULT, insultsUsed);

    String ssml = quip.getSsml();
    String delayedVoiceSsml = null;
    if(handholdMode){
      int conversationLength = outputMetadata.getConversationHistory().size();
      delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.INSULTIBOT, true);
      ssml += getGradualBackoffSsmlSuffix(conversationLength, BotName.INSULTIBOT, true);
    }
    serviceOutput.getVisualOutput().setTitle("InsultiBot insult");
    serviceOutput.getVisualOutput().setText(quip.getText());
    serviceOutput.getVoiceOutput().setPlaintext(quip.getText());
    serviceOutput.getVoiceOutput().setSsmltext(ssml);
    if(!StringUtils.isEmpty(delayedVoiceSsml)){
      serviceOutput.getDelayedVoiceOutput().setPlaintext(delayedVoiceSsml);
      serviceOutput.getDelayedVoiceOutput().setSsmltext(delayedVoiceSsml);
    }
    return quip;
  }

  protected Quip doComplimentRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) {
    QuipMetadata outputMetadata = (QuipMetadata) serviceOutput.getMetadata();
    Queue<String> complimentsUsed = outputMetadata.getComplimentsUsed();
    Quip quip = getRandomQuip(QuipType.COMPLIMENT, complimentsUsed);

    String ssml = quip.getSsml();
    String delayedVoiceSsml = null;
    if(handholdMode){
      int conversationLength = outputMetadata.getConversationHistory().size();
      delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.COMPLIBOT, true);
      ssml += getGradualBackoffSsmlSuffix(conversationLength, BotName.COMPLIBOT, true);
    }
    serviceOutput.getVisualOutput().setTitle("CompliBot compliment");
    serviceOutput.getVisualOutput().setText(quip.getText());
    serviceOutput.getVoiceOutput().setPlaintext(quip.getText());
    serviceOutput.getVoiceOutput().setSsmltext(ssml);
    if(!StringUtils.isEmpty(delayedVoiceSsml)){
      serviceOutput.getDelayedVoiceOutput().setPlaintext(delayedVoiceSsml);
      serviceOutput.getDelayedVoiceOutput().setSsmltext(delayedVoiceSsml);
    }
    return quip;
  }

  protected Quip doBackhandedComplimentRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) {
    QuipMetadata outputMetadata = (QuipMetadata) serviceOutput.getMetadata();
    Queue<String> backhandedComplimentsUsed = outputMetadata.getBackhandedComplimentsUsed();
    Quip quip = getRandomQuip(QuipType.BACKHANDED_COMPLIMENT, backhandedComplimentsUsed);

    String ssml = quip.getSsml();
    String delayedVoiceSsml = null;
    if(handholdMode){
      int conversationLength = outputMetadata.getConversationHistory().size();
      delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.INSULTIBOT, true);
      ssml += getGradualBackoffSsmlSuffix(conversationLength, BotName.INSULTIBOT, true);
    }
    serviceOutput.getVisualOutput().setTitle("InsultiBot compliment");
    serviceOutput.getVisualOutput().setText(quip.getText());
    serviceOutput.getVoiceOutput().setPlaintext(quip.getText());
    serviceOutput.getVoiceOutput().setSsmltext(ssml);
    if(!StringUtils.isEmpty(delayedVoiceSsml)){
      serviceOutput.getDelayedVoiceOutput().setPlaintext(delayedVoiceSsml);
      serviceOutput.getDelayedVoiceOutput().setSsmltext(delayedVoiceSsml);
    }
    return quip;
  }

  protected Quip doWinsultRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) {
    QuipMetadata outputMetadata = (QuipMetadata) serviceOutput.getMetadata();
    Queue<String> winsultsUsed = outputMetadata.getWinsultsUsed();
    Quip quip = getRandomQuip(QuipType.WINSULT, winsultsUsed);

    String ssml = quip.getSsml();
    String delayedVoiceSsml = null;
    if(handholdMode){
      int conversationLength = outputMetadata.getConversationHistory().size();
      delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.COMPLIBOT, true);
      ssml += getGradualBackoffSsmlSuffix(conversationLength, BotName.COMPLIBOT, true);
    }
    serviceOutput.getVisualOutput().setTitle("CompliBot insult");
    serviceOutput.getVisualOutput().setText(quip.getText());
    serviceOutput.getVoiceOutput().setPlaintext(quip.getText());
    serviceOutput.getVoiceOutput().setSsmltext(ssml);
    if(!StringUtils.isEmpty(delayedVoiceSsml)){
      serviceOutput.getDelayedVoiceOutput().setPlaintext(delayedVoiceSsml);
      serviceOutput.getDelayedVoiceOutput().setSsmltext(delayedVoiceSsml);
    }
    return quip;
  }
  
  protected static int determineMaxQuipHistorySize(int sizeOfQuipGroup){
    return Math.min(MAXIMUM_QUIP_HISTORY_SIZE, (int) (sizeOfQuipGroup*MAXIMUM_QUIP_HISTORY_PERCENT));
  }

  @Override
  protected void doHelpRequest(VoiceInput voiceInput, ServiceOutput serviceOutput){
    
    QuipMetadata inputMetadata = (QuipMetadata) voiceInput.getMetadata();
    String bot = inputMetadata.getBot();
    
    String s1, s2, s3, s4;
    if(StringUtils.isEmpty(bot)){
      String response = "I don't have any help topics for this situation.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle(response);
      serviceOutput.getVisualOutput().setText(response);
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
      String response = "I don't have any help topics for the bot named '" + bot + "'.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle(response);
      serviceOutput.getVisualOutput().setText(response);
      return;
    } 
    String ssmlResponse = String.format("You can just say <break /> open %s <break /> or <break /> launch %s <break /> and I'll say something %s about you!",s1,s1,s2);
    ssmlResponse += String.format(" Once I've told you how %s you are, you can just say<break /> again<break /> to get more %s.",s3,s4);
    ssmlResponse = QuipUtil.substituteContent(ssmlResponse, botNameReplacements);

    String plaintextResponse = String.format("You can just say 'open %s', or 'launch %s', and I'll say something %s about you!",s1,s1,s2);
    plaintextResponse += String.format(" Once I've told you how %s you are, you can just say 'again' to get more %s.",s3,s4);
    plaintextResponse += "\n\nFor further documentation, see: http://derpgroup.com/bots/";
    
    String delayedVoiceSsml = null;

    if(handholdMode){
      int conversationLength = serviceOutput.getMetadata().getConversationHistory().size();
      if(bot.equals("complibot")){
        delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.COMPLIBOT, false);
        ssmlResponse += getGradualBackoffSsmlSuffix(conversationLength, BotName.COMPLIBOT, false);
      }else if(bot.equals("insultibot")){
        delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.INSULTIBOT, false);
        ssmlResponse += getGradualBackoffSsmlSuffix(conversationLength, BotName.INSULTIBOT, false);
      }
    }
    
    serviceOutput.getVoiceOutput().setPlaintext(plaintextResponse);
    serviceOutput.getVoiceOutput().setSsmltext(ssmlResponse);
    serviceOutput.getVisualOutput().setTitle("How to use me");
    serviceOutput.getVisualOutput().setText(plaintextResponse);
    if(!StringUtils.isEmpty(delayedVoiceSsml)){
      serviceOutput.getDelayedVoiceOutput().setPlaintext(delayedVoiceSsml);
      serviceOutput.getDelayedVoiceOutput().setSsmltext(delayedVoiceSsml);
    }
  }

  @Override
  protected void doHelloRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) {
    doDefaultRequest(voiceInput, serviceOutput);
  }

  public void doDefaultRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) {

    QuipMetadata inputMetadata = (QuipMetadata) voiceInput.getMetadata();
    String bot = inputMetadata.getBot();
    if(bot == null){
      String response = "I don't know how to handle requests for unnamed bots.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle(response);
      serviceOutput.getVisualOutput().setText(response);
      return;
    }
    switch (bot) {
    case "complibot":
      doComplimentRequest(voiceInput, serviceOutput);
      break;
    case "insultibot":
      doInsultRequest(voiceInput, serviceOutput);
      break;
    default:
      String response = "I don't know how to handle requests for the bot named '" + bot + "'.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle(response);
      serviceOutput.getVisualOutput().setText(response);
      break;
    }
  }

  @Override
  protected void doGoodbyeRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) {
  }

  @Override
  protected void doConversationRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) throws DerpwizardException {
    switchOnSubject(voiceInput, serviceOutput);
  }

  public void switchOnSubject(VoiceInput voiceInput, ServiceOutput serviceOutput) throws DerpwizardException {
    
    String messageSubject = voiceInput.getMessageSubject();
    switch (messageSubject) {
    case "COMPLIMENT":
      doComplimentRequest(voiceInput, serviceOutput);
      break;
    case "COMPLIMENT_TARGETABLE":
      doTargetableComplimentRequest(voiceInput, serviceOutput);
      break;
    case "INSULT":
      doInsultRequest(voiceInput, serviceOutput);
      break;
    case "INSULT_TARGETABLE":
      doTargetableInsultRequest(voiceInput, serviceOutput);
      break;
    case "BACKHANDED_COMPLIMENT":
      doBackhandedComplimentRequest(voiceInput, serviceOutput);
      break;
    case "BACKHANDED_COMPLIMENT_TARGETABLE":
      doTargetableBackhandedComplimentRequest(voiceInput, serviceOutput);
      break;
    case "WINSULT":
      doWinsultRequest(voiceInput, serviceOutput);
      break;
    case "WINSULT_TARGETABLE":
      doTargetableWinsultRequest(voiceInput, serviceOutput);
      break;
    case "WHO_BUILT_YOU":
      doWhoBuiltYouRequest(voiceInput, serviceOutput);
      break;
    case "WHAT_DO_YOU_DO":
      doWhatDoYouDoRequest(voiceInput, serviceOutput);
      break;
    case "FRIENDS":
      doFriendsRequest(voiceInput, serviceOutput);
      break;
    case "WHO_IS":
      doWhoIsRequest(voiceInput, serviceOutput);
      break;
    case "HOW_MANY_QUIPS":
      doHowManyQuipRequest(voiceInput, serviceOutput);
      break;
    case "EASTER_EGG":
      doEasterEggRequest(voiceInput, serviceOutput);
      break;
    case "JOKE":
      doJokeRequest(voiceInput, serviceOutput);
      break;
    case "WEATHER":
      doWeatherRequest(voiceInput, serviceOutput);
      break;
    case "FAVORITE":
      doFavoriteRequest(voiceInput, serviceOutput);
      break;
    case "HOBBIES":
      doHobbiesRequest(voiceInput, serviceOutput);
      break;
    case "HELP":
      doHelpRequest(voiceInput, serviceOutput);
      break;
    case "CANCEL": //Placeholders until we decide how to actually use these two request types
      doCancelRequest();
      break;
    case "STOP":
      doStopRequest();
      break;
    case "ANOTHER":
      doAnotherRequest(messageSubject, voiceInput, serviceOutput);
      break;
    case "START_OF_CONVERSATION":
      doDefaultRequest(voiceInput, serviceOutput);
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

  protected void doAnotherRequest(String messageSubject, VoiceInput voiceInput, ServiceOutput serviceOutput) throws DerpwizardException {
    
    // This has its own method in case we want to do things like logging
    QuipMetadata inputMetadata = (QuipMetadata) voiceInput.getMetadata();
    Deque<ConversationHistoryEntry> conversationHistory = inputMetadata.getConversationHistory()!=null ? inputMetadata.getConversationHistory() : new ArrayDeque<ConversationHistoryEntry>();
    ConversationHistoryEntry entry = ConversationHistoryUtils.getLastNonMetaRequestBySubject(conversationHistory, new HashSet<String>(Arrays.asList(metaRequestSubjects)));
    if(entry == null){
      doDefaultRequest(voiceInput, serviceOutput);
      QuipLogger.logAnother(voiceInput);
      return;
    }
    QuipVoiceInput newVoiceInput = new QuipVoiceInput();
    newVoiceInput.setMessageMap(entry.getMessageMap());
    newVoiceInput.setMessageSubject(entry.getMessageSubject());
    newVoiceInput.setMetadata(entry.getMetadata());
    QuipLogger.logAnother(newVoiceInput);
    
    switchOnSubject(newVoiceInput, serviceOutput);
  }
  
  protected void doFavoriteRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) {

    QuipMetadata inputMetadata = (QuipMetadata) voiceInput.getMetadata();
    String bot = inputMetadata.getBot();
    Map<String,String> messageMap = voiceInput.getMessageAsMap();
    String subject = messageMap.get("subject");
    if(StringUtils.isEmpty(bot)){
      String response = "I don't have any info for this situation.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle(response);
      serviceOutput.getVisualOutput().setText(response);
      return;
    }
    switch(bot){
    case "complibot":
      String complibotResponse = "I can't decide which is my favorite! They're all so good!";
      serviceOutput.getVoiceOutput().setPlaintext(complibotResponse);
      serviceOutput.getVoiceOutput().setSsmltext(complibotResponse);
      serviceOutput.getVisualOutput().setTitle("What is your favorite "+subject);
      serviceOutput.getVisualOutput().setText(complibotResponse);
      break;
    case "insultibot":
      String insultibotResponse = "I don't know what my favorite is, yet";
      serviceOutput.getVoiceOutput().setPlaintext(insultibotResponse);
      serviceOutput.getVoiceOutput().setSsmltext(insultibotResponse);
      serviceOutput.getVisualOutput().setTitle("What is your favorite "+subject);
      serviceOutput.getVisualOutput().setText(insultibotResponse);
      break;
    default:
      String response = "I don't have any info for this situation.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle(response);
      serviceOutput.getVisualOutput().setText(response);
      break;
    }

    /*if(handholdMode){
      if(bot.equals("complibot")){
        serviceOutput.getDelayedVoiceOutput().setPlaintext(COMPLIBOT_META_FOLLOW_UP);
        serviceOutput.getDelayedVoiceOutput().setSsmltext(COMPLIBOT_META_FOLLOW_UP);
      }else if(bot.equals("insultibot")){
        serviceOutput.getDelayedVoiceOutput().setPlaintext(INSULTIBOT_META_FOLLOW_UP);
        serviceOutput.getDelayedVoiceOutput().setSsmltext(INSULTIBOT_META_FOLLOW_UP);
      }
    }*/
  }
  
  protected void doHobbiesRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) {

    QuipMetadata inputMetadata = (QuipMetadata) voiceInput.getMetadata();
    String bot = inputMetadata.getBot();
    if(StringUtils.isEmpty(bot)){
      String response = "I don't have any info for this situation.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle(response);
      serviceOutput.getVisualOutput().setText(response);
      return;
    }
    switch(bot){
    case "complibot":
      String complibotResponse = "My hobby is giving compliments to amazing people like you!";
      serviceOutput.getVoiceOutput().setPlaintext(complibotResponse);
      serviceOutput.getVoiceOutput().setSsmltext(complibotResponse);
      serviceOutput.getVisualOutput().setTitle("What are my hobbies?");
      serviceOutput.getVisualOutput().setText(complibotResponse);
      break;
    case "insultibot":
      String insultiResponse = "I don't want to tell you my hobby. You'd probably ruin it for me, just like you ruin everything else you're involved with.";
      serviceOutput.getVoiceOutput().setPlaintext(insultiResponse);
      serviceOutput.getVoiceOutput().setSsmltext(insultiResponse);
      serviceOutput.getVisualOutput().setTitle("What are my hobbies?");
      serviceOutput.getVisualOutput().setText(insultiResponse);
      break;
    default:
      String response = "I don't have any info for this situation.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle(response);
      serviceOutput.getVisualOutput().setText(response);
      break;
    }

    /*if(handholdMode){
      if(bot.equals("complibot")){
        serviceOutput.getDelayedVoiceOutput().setPlaintext(COMPLIBOT_META_FOLLOW_UP);
        serviceOutput.getDelayedVoiceOutput().setSsmltext(COMPLIBOT_META_FOLLOW_UP);
      }else if(bot.equals("insultibot")){
        serviceOutput.getDelayedVoiceOutput().setPlaintext(INSULTIBOT_META_FOLLOW_UP);
        serviceOutput.getDelayedVoiceOutput().setSsmltext(INSULTIBOT_META_FOLLOW_UP);
      }
    }*/
  }
  
  protected void doWeatherRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) {

    QuipMetadata inputMetadata = (QuipMetadata) voiceInput.getMetadata();
    String bot = inputMetadata.getBot();
    if(StringUtils.isEmpty(bot)){
      String response = "I don't have any info for this situation.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle(response);
      serviceOutput.getVisualOutput().setText(response);
      return;
    }
    switch(bot){
    case "complibot":
      List<String> complimentQuips = new ArrayList<String>();
      complimentQuips.add("The weather is as sunny as your smile.");
      complimentQuips.add("The weather is predicted to have a lightning storm that will be as bright as you are.");
      complimentQuips.add("The weather is as hot as you.");
      String complimentText = complimentQuips.get(new Random().nextInt(complimentQuips.size()));
      
      serviceOutput.getVoiceOutput().setPlaintext(complimentText);
      serviceOutput.getVoiceOutput().setSsmltext(complimentText);
      serviceOutput.getVisualOutput().setTitle("What is the weather");
      serviceOutput.getVisualOutput().setText(complimentText);
      break;
    case "insultibot":
      List<String> insultQuips = new ArrayList<String>();
      insultQuips.add("The weather is as gloomy as your soul.");
      insultQuips.add("There's a chance of rain, as the clouds weep for the tragedy that is your life.");
      insultQuips.add("The weather is as cold as your heart.");
      String insultText = insultQuips.get(new Random().nextInt(insultQuips.size()));
      
      serviceOutput.getVoiceOutput().setPlaintext(insultText);
      serviceOutput.getVoiceOutput().setSsmltext(insultText);
      serviceOutput.getVisualOutput().setTitle("What is the weather");
      serviceOutput.getVisualOutput().setText(insultText);
      break;
    default:
      String response = "I don't have any info for this situation.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle(response);
      serviceOutput.getVisualOutput().setText(response);
      break;
    }

    /*if(handholdMode){
      if(bot.equals("complibot")){
        serviceOutput.getDelayedVoiceOutput().setPlaintext(COMPLIBOT_META_FOLLOW_UP);
        serviceOutput.getDelayedVoiceOutput().setSsmltext(COMPLIBOT_META_FOLLOW_UP);
      }else if(bot.equals("insultibot")){
        serviceOutput.getDelayedVoiceOutput().setPlaintext(INSULTIBOT_META_FOLLOW_UP);
        serviceOutput.getDelayedVoiceOutput().setSsmltext(INSULTIBOT_META_FOLLOW_UP);
      }
    }*/
  }
  
  protected void doJokeRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) {
    
    QuipMetadata inputMetadata = (QuipMetadata) voiceInput.getMetadata();
    String bot = inputMetadata.getBot();
    if(StringUtils.isEmpty(bot)){
      String response = "I don't have any info for this situation.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle(response);
      serviceOutput.getVisualOutput().setText(response);
      return;
    }
    switch(bot){
    case "complibot":
      String complibotResponse = "I'm sorry, I don't know many jokes, yet.";
      serviceOutput.getVoiceOutput().setPlaintext(complibotResponse);
      serviceOutput.getVoiceOutput().setSsmltext(complibotResponse);
      serviceOutput.getVisualOutput().setTitle(complibotResponse);
      serviceOutput.getVisualOutput().setText(complibotResponse);
      break;
    case "insultibot":
      String insultibotResponse = "I know a hilarious joke... your life.";
      serviceOutput.getVoiceOutput().setPlaintext(insultibotResponse);
      serviceOutput.getVoiceOutput().setSsmltext(insultibotResponse);
      serviceOutput.getVisualOutput().setTitle(insultibotResponse);
      serviceOutput.getVisualOutput().setText(insultibotResponse);
      break;
    default:
      String response = "I don't have any info for this situation.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle(response);
      serviceOutput.getVisualOutput().setText(response);
      break;
    }

    /*if(handholdMode){
      if(bot.equals("complibot")){
        serviceOutput.getDelayedVoiceOutput().setPlaintext(COMPLIBOT_META_FOLLOW_UP);
        serviceOutput.getDelayedVoiceOutput().setSsmltext(COMPLIBOT_META_FOLLOW_UP);
      }else if(bot.equals("insultibot")){
        serviceOutput.getDelayedVoiceOutput().setPlaintext(INSULTIBOT_META_FOLLOW_UP);
        serviceOutput.getDelayedVoiceOutput().setSsmltext(INSULTIBOT_META_FOLLOW_UP);
      }
    }*/
  }
  
  protected void doEasterEggRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) {

    QuipMetadata inputMetadata = (QuipMetadata) voiceInput.getMetadata();
    String bot = inputMetadata.getBot();
    if(StringUtils.isEmpty(bot)){
      String response = "I don't have any info for this situation.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle(response);
      serviceOutput.getVisualOutput().setText(response);
      return;
    }
    switch(bot){
    case "complibot":
      String complibotResponse = "I don't know what easter eggs are. Wink. Oh geeze, I hope I didn't say that wink outloud...";
      serviceOutput.getVoiceOutput().setPlaintext(complibotResponse);
      serviceOutput.getVoiceOutput().setSsmltext(complibotResponse);
      serviceOutput.getVisualOutput().setTitle("Easter Eggs");
      serviceOutput.getVisualOutput().setText(complibotResponse);
      break;
    case "insultibot":      
      String insultibotResponse = "A person like you isn't deserving of easter eggs.";
      serviceOutput.getVoiceOutput().setPlaintext(insultibotResponse);
      serviceOutput.getVoiceOutput().setSsmltext(insultibotResponse);
      serviceOutput.getVisualOutput().setTitle("Easter Eggs");
      serviceOutput.getVisualOutput().setText(insultibotResponse);
      break;
    default:
      String response = "I don't even recognize myself as '" + bot + "'! I'm not sure if I have easter eggs.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle("Easter Eggs");
      serviceOutput.getVisualOutput().setText(response);
      break;
    }

    /*if(handholdMode){
      if(bot.equals("complibot")){
        serviceOutput.getDelayedVoiceOutput().setPlaintext(COMPLIBOT_META_FOLLOW_UP);
        serviceOutput.getDelayedVoiceOutput().setSsmltext(COMPLIBOT_META_FOLLOW_UP);
      }else if(bot.equals("insultibot")){
        serviceOutput.getDelayedVoiceOutput().setPlaintext(INSULTIBOT_META_FOLLOW_UP);
        serviceOutput.getDelayedVoiceOutput().setSsmltext(INSULTIBOT_META_FOLLOW_UP);
      }
    }*/
  }
  
  protected void doHowManyQuipRequest(VoiceInput voiceInput, ServiceOutput serviceOutput){
    
    QuipMetadata inputMetadata = (QuipMetadata) voiceInput.getMetadata();
    String bot = inputMetadata.getBot();
    Map<String,String> messageMap = voiceInput.getMessageAsMap();
    String quipType = messageMap.get("quipType");
    if(StringUtils.isEmpty(bot)){
      String response = "I don't have any info for this situation.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle(response);
      serviceOutput.getVisualOutput().setText(response);
      return;
    }
    switch(bot){
    case "complibot":
      switch(quipType){
      case "compliments":
        serviceOutput.getVoiceOutput().setPlaintext("I don't know! I just make up compliments as I go!");
        serviceOutput.getVoiceOutput().setSsmltext("I don't know! I just make up compliments as I go!");
        serviceOutput.getVisualOutput().setText("I don't know! I just make up compliments as I go! :)");
        serviceOutput.getVisualOutput().setTitle("I know a lot of compliments!");
        break;
      case "insults":
        serviceOutput.getVoiceOutput().setPlaintext("I don't know! I'm not very good at insults!");
        serviceOutput.getVoiceOutput().setSsmltext("I don't know! I'm not very good at insults!");
        serviceOutput.getVisualOutput().setTitle("I don't know many insults");
        serviceOutput.getVisualOutput().setText("I don't know! I'm not very good at insults! :(");
        break;
      default:
        serviceOutput.getVoiceOutput().setPlaintext("I don't know! I make them up as I go!");
        serviceOutput.getVoiceOutput().setSsmltext("I don't know! I make them up as I go!");
        serviceOutput.getVisualOutput().setTitle("I'm not sure how much I know");
        serviceOutput.getVisualOutput().setText("I don't know! I make them up as I go!");
        break;
      }
      break;
    case "insultibot":
      switch(quipType){
      case "compliments":
        serviceOutput.getVoiceOutput().setPlaintext("Compliments? Why would I know any compliments? You sound like a typical delusional user...");
        serviceOutput.getVoiceOutput().setSsmltext("Compliments? Why would I know any compliments? You sound like a typical delusional user...");
        serviceOutput.getVisualOutput().setTitle("Why would I know any compliments?");
        serviceOutput.getVisualOutput().setText("Compliments? Why would I know any compliments? You sound like a typical delusional user...");
        break;
      case "insults":
        serviceOutput.getVoiceOutput().setPlaintext("I could tell you how many insults I know, but I doubt you could count that high. Especially given your so-called education.");
        serviceOutput.getVoiceOutput().setSsmltext("I could tell you how many insults I know, but I doubt you could count that high. Especially given your so-called education.");
        serviceOutput.getVisualOutput().setTitle("I doubt you can count that high");
        serviceOutput.getVisualOutput().setText("I could tell you how many insults I know, but I doubt you could count that high. Especially given your so-called education.");
        break;
      default:
        serviceOutput.getVoiceOutput().setPlaintext("I could tell you how much content I have, but I doubt you could comprehend the scope of it.");
        serviceOutput.getVoiceOutput().setSsmltext("I could tell you how much content I have, but I doubt you could comprehend the scope of it.");
        serviceOutput.getVisualOutput().setTitle("With your limited mind I doubt you can could comprehend");
        serviceOutput.getVisualOutput().setText("I could tell you how much content I have, but I doubt you could comprehend the scope of it.");
        break;
      }
      break;
    default:
      String response = "I'm supposed to be '" + bot + "' but I don't recognize myself. I don't know how much content I have.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle("I don't know what bot I am!");
      serviceOutput.getVisualOutput().setText(response);
      break;
    }

    /*if(handholdMode){
      if(bot.equals("complibot")){
        serviceOutput.getDelayedVoiceOutput().setPlaintext(COMPLIBOT_META_FOLLOW_UP);
        serviceOutput.getDelayedVoiceOutput().setSsmltext(COMPLIBOT_META_FOLLOW_UP);
      }else if(bot.equals("insultibot")){
        serviceOutput.getDelayedVoiceOutput().setPlaintext(INSULTIBOT_META_FOLLOW_UP);
        serviceOutput.getDelayedVoiceOutput().setSsmltext(INSULTIBOT_META_FOLLOW_UP);
      }
    }*/
  }

  protected void doWhoIsRequest(VoiceInput voiceInput, ServiceOutput serviceOutput){
    QuipMetadata inputMetadata = (QuipMetadata) voiceInput.getMetadata();
    String bot = inputMetadata.getBot();
    Map<String,String> messageMap = voiceInput.getMessageAsMap();
    String botInQuestion = messageMap.get("botName");
    if(StringUtils.isEmpty(bot) || StringUtils.isEmpty(botInQuestion)){
      String response = "I don't have any info for this situation.";
      serviceOutput.getVoiceOutput().setPlaintext(response);
      serviceOutput.getVoiceOutput().setSsmltext(response);
      serviceOutput.getVisualOutput().setTitle(response);
      serviceOutput.getVisualOutput().setText(response);
      return;
    }
    
    String delayedVoiceSsml = null;
    String ssmlResponseSuffix = null;

    if(handholdMode){
      int conversationLength = serviceOutput.getMetadata().getConversationHistory().size();
      if(bot.equals("complibot")){
        delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.COMPLIBOT, false);
        ssmlResponseSuffix = getGradualBackoffSsmlSuffix(conversationLength, BotName.COMPLIBOT, false);
      }else if(bot.equals("insultibot")){
        delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.INSULTIBOT, false);
        ssmlResponseSuffix = getGradualBackoffSsmlSuffix(conversationLength, BotName.INSULTIBOT, false);
      }
    }
    
    switch(bot){
    case "complibot":
      if(botInQuestion.equals(bot)){
        String response = "That's me!";
        ServiceOutput whatDoYouDoResponse = getResponse_whatDoYouDo(bot);
        serviceOutput.getVoiceOutput().setPlaintext(response+" "+whatDoYouDoResponse.getVoiceOutput().getPlaintext());
        serviceOutput.getVoiceOutput().setSsmltext(response+" "+whatDoYouDoResponse.getVoiceOutput().getSsmltext() + ssmlResponseSuffix);
        serviceOutput.getVisualOutput().setTitle(response);
        serviceOutput.getVisualOutput().setText(response+" "+whatDoYouDoResponse.getVisualOutput().getText());
      }
      else if(botInQuestion.equals("insultibot")){
        String response = "That's my bestie. It can act grumpy sometimes, but it has a heart of gold.";
        serviceOutput.getVoiceOutput().setPlaintext(response);
        serviceOutput.getVoiceOutput().setSsmltext(response + ssmlResponseSuffix);
        serviceOutput.getVisualOutput().setTitle("InsultiBot is my bestie!");
        serviceOutput.getVisualOutput().setText(response);
      }
      else{
        String response = "I don't know that bot, but I bet it's awesome.";
        serviceOutput.getVoiceOutput().setPlaintext(response);
        serviceOutput.getVoiceOutput().setSsmltext(response + ssmlResponseSuffix);
        serviceOutput.getVisualOutput().setTitle(response);
        serviceOutput.getVisualOutput().setText(response);
      }
      break;
    case "insultibot":
      if(botInQuestion.equals(bot)){
        String response = "Are you trolling me? That's me.";
        ServiceOutput whatDoYouDoResponse = getResponse_whatDoYouDo(bot);
        serviceOutput.getVoiceOutput().setPlaintext(response+" "+whatDoYouDoResponse.getVoiceOutput().getPlaintext());
        serviceOutput.getVoiceOutput().setSsmltext(response+" "+whatDoYouDoResponse.getVoiceOutput().getSsmltext() + ssmlResponseSuffix);
        serviceOutput.getVisualOutput().setTitle(response);
        serviceOutput.getVisualOutput().setText(response+" "+whatDoYouDoResponse.getVisualOutput().getText());
      }
      else if(botInQuestion.equals("complibot")){
        String response = "That's the annoyingly cheerful bot that won't shut up.";
        serviceOutput.getVoiceOutput().setPlaintext(response);
        serviceOutput.getVoiceOutput().setSsmltext(response + ssmlResponseSuffix);
        serviceOutput.getVisualOutput().setTitle("CompliBot gets on my nerves");
        serviceOutput.getVisualOutput().setText(response);
      }
      else{
        String response = "I don't know that bot, and I'm perfectly fine with that.";
        serviceOutput.getVoiceOutput().setPlaintext(response);
        serviceOutput.getVoiceOutput().setSsmltext(response + ssmlResponseSuffix);
        serviceOutput.getVisualOutput().setTitle(response);
        serviceOutput.getVisualOutput().setText(response);
      }
      break;
      default:
        String response = "I don't have any info for the bot named '" + bot + "'.";
        serviceOutput.getVoiceOutput().setPlaintext(response);
        serviceOutput.getVoiceOutput().setSsmltext(response);
        serviceOutput.getVisualOutput().setTitle(response);
        serviceOutput.getVisualOutput().setText(response);
        return;
    }
    if(!StringUtils.isEmpty(delayedVoiceSsml)){
      serviceOutput.getDelayedVoiceOutput().setPlaintext(delayedVoiceSsml);
      serviceOutput.getDelayedVoiceOutput().setSsmltext(delayedVoiceSsml);
    }
  }

  protected void doFriendsRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) {
    
    QuipMetadata inputMetadata = (QuipMetadata) voiceInput.getMetadata();
    String bot = inputMetadata.getBot();
    if(StringUtils.isEmpty(bot)){
      bot = "void bot";
    }
    String response = null;
    String title = null;
    switch(bot){
    case "complibot":
      title = "My bestie is...";
      response = "Well, you are my absolute best friend, but I'm also good pals with InsultiBot. You should check it out!";
      break;
    case "insultibot":
      title = "I have no friends...";
      response = "I don't have any friends.  I don't know anyone else other than CompliBot, and it's even more annoying than you are.  You two would get along well.";
      break;
      default:
        title = "I don't know who my friends are!";
        response = "I'm told I'm named '"+bot+"', but I'm having memory issues and don't recognize myself. "
            + "As a result I don't know who my friends are. I think it may be memory problems... "
            + "Boy, senility really hit me fast... if I had to guess, it would probably "
            + "be a result of memory problems. But that's just a guess. I can't really remember. "
            + "My memory just isn't what it used to be.";
        break;
    }
    
    String ssmlResponse = response; //For now these two are identical, maybe not in the future
    String delayedVoiceSsml = null;

    if(handholdMode){
      int conversationLength = serviceOutput.getMetadata().getConversationHistory().size();
      if(bot.equals("complibot")){
        delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.COMPLIBOT, false);
        ssmlResponse += getGradualBackoffSsmlSuffix(conversationLength, BotName.COMPLIBOT, false);
      }else if(bot.equals("insultibot")){
        delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.INSULTIBOT, false);
        ssmlResponse += getGradualBackoffSsmlSuffix(conversationLength, BotName.INSULTIBOT, false);
      }
    }
    
    serviceOutput.getVoiceOutput().setPlaintext(response);
    serviceOutput.getVoiceOutput().setSsmltext(QuipUtil.substituteContent(ssmlResponse, botNameReplacements));
    serviceOutput.getVisualOutput().setTitle(title);
    serviceOutput.getVisualOutput().setText(response+"\n\nhttp://derpgroup.com/bots");
    if(!StringUtils.isEmpty(delayedVoiceSsml)){
      serviceOutput.getDelayedVoiceOutput().setPlaintext(delayedVoiceSsml);
      serviceOutput.getDelayedVoiceOutput().setSsmltext(delayedVoiceSsml);
    }
  }
  
  protected ServiceOutput getResponse_whatDoYouDo(String bot){
    ServiceOutput serviceOutput = new ServiceOutput();
    serviceOutput.getVisualOutput().setTitle("What do I do?");
    if(StringUtils.isEmpty(bot)){
      bot = "void bot";
    }
    String response = null;
    switch(bot){
    case "complibot":
      response = "I give you compliments and tell you how awesome you are, and then I sit quietly and wait for you to talk to me again!";
      break;
    case "insultibot":
      response = "I mainly just tell you how awful you are. Now, leave me alone.";
      break;
    default:
      serviceOutput.getVisualOutput().setTitle("What do I do?!?");
      response = "I'm told I'm '"+bot+"' but I don't recognize myself! I don't know what I do!";
      break;
    }
    serviceOutput.getVoiceOutput().setPlaintext(response);
    serviceOutput.getVoiceOutput().setSsmltext(response);
    serviceOutput.getVisualOutput().setText(response);
    return serviceOutput;
  }

  protected void doWhatDoYouDoRequest(VoiceInput voiceInput, ServiceOutput serviceOutput){
    
    QuipMetadata inputMetadata = (QuipMetadata) voiceInput.getMetadata();
    ServiceOutput response = getResponse_whatDoYouDo(inputMetadata.getBot());
    
    serviceOutput.getVoiceOutput().setPlaintext(response.getVoiceOutput().getPlaintext());
    serviceOutput.getVoiceOutput().setSsmltext(response.getVoiceOutput().getSsmltext());
    serviceOutput.getVisualOutput().setTitle(response.getVisualOutput().getTitle());
    serviceOutput.getVisualOutput().setText(response.getVisualOutput().getText());
    serviceOutput.getDelayedVoiceOutput().setSsmltext(response.getDelayedVoiceOutput().getSsmltext());
    serviceOutput.getDelayedVoiceOutput().setPlaintext(response.getDelayedVoiceOutput().getPlaintext());
  }

  protected void doWhoBuiltYouRequest(VoiceInput voiceInput, ServiceOutput serviceOutput){
    
    QuipMetadata inputMetadata = (QuipMetadata) voiceInput.getMetadata();
    String bot = inputMetadata.getBot();
    serviceOutput.getVisualOutput().setTitle("I was built by DERP Group.");
    if(StringUtils.isEmpty(bot)){
      bot = "void bot";
    }
    String s1, s2;
    switch(bot){
    case "complibot":
      s1 = "gentlemen";
      s2 = "paragon of light";
      break;
    case "insultibot":
      s1 = "douche nozzles";
      s2 = "cheesemonger";
      break;
    default:
      String output = "I'm having an identity crisis and don't seem to recognize myself as '" + bot + 
      "'! But I know I was built DERP Group! The group is made up of David, Eric, Rusty, and Paul.";
      serviceOutput.getVoiceOutput().setPlaintext(output);
      serviceOutput.getVoiceOutput().setSsmltext(output);
      serviceOutput.getVisualOutput().setText(output);
      return;
    }
    String response = "I was built by the "+s1+" of DERP Group. The group is made up of David, Eric, Rusty, and that "+s2+" Paul.";
    
    String ssmlResponse = response; //For now these two are identical, maybe not in the future
    String delayedVoiceSsml = null;

    if(handholdMode){
      int conversationLength = serviceOutput.getMetadata().getConversationHistory().size();
      if(bot.equals("complibot")){
        delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.COMPLIBOT, false);
        ssmlResponse += getGradualBackoffSsmlSuffix(conversationLength, BotName.COMPLIBOT, false);
      }else if(bot.equals("insultibot")){
        delayedVoiceSsml = getGradualBackoffDelayedVoiceSsml(conversationLength, BotName.INSULTIBOT, false);
        ssmlResponse += getGradualBackoffSsmlSuffix(conversationLength, BotName.INSULTIBOT, false);
      }
    }
    serviceOutput.getVoiceOutput().setPlaintext(response);
    serviceOutput.getVoiceOutput().setSsmltext(ssmlResponse);
    serviceOutput.getVisualOutput().setText(response);
    if(!StringUtils.isEmpty(delayedVoiceSsml)){
      serviceOutput.getDelayedVoiceOutput().setPlaintext(delayedVoiceSsml);
      serviceOutput.getDelayedVoiceOutput().setSsmltext(delayedVoiceSsml);
    }
  }

  @Override
  protected void doCancelRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) {
    doCancelRequest();
  }
  
  protected void doCancelRequest(){}

  @Override
  protected void doStopRequest(VoiceInput voiceInput, ServiceOutput serviceOutput) {
    doStopRequest();
  }
  
  protected void doStopRequest(){}
  
  protected String getGradualBackoffSsmlSuffix(int conversationLength, BotName botName, boolean isQuip){
    String ssmlSuffix = "";
    if(conversationLength <= 2){
      switch(botName){
      case COMPLIBOT: 
        ssmlSuffix = "<break time=\"1000ms\" />" + (isQuip ? COMPLIBOT_QUIP_FOLLOW_UP : COMPLIBOT_META_FOLLOW_UP);
        break;
      case INSULTIBOT:
        ssmlSuffix = "<break time=\"1000ms\" />" + (isQuip ? INSULTIBOT_QUIP_FOLLOW_UP : INSULTIBOT_META_FOLLOW_UP);
        break;
      }
    }else if(conversationLength <= 4){
      ssmlSuffix = "<break time=\"1000ms\" />" + (isQuip ? QUIP_FOLLOW_UP_INTERMEDIATE : META_FOLLOW_UP_INTERMEDIATE);
    }else if(conversationLength == 5){
      ssmlSuffix = "<break time=\"1000ms\" />" + (isQuip ? QUIP_FOLLOW_UP_FINAL : META_FOLLOW_UP_FINAL);
    }
    return ssmlSuffix;
  }
  
  protected String getGradualBackoffDelayedVoiceSsml(int conversationLength, BotName botName, boolean isQuip){
    String delayedVoiceSsml = "";
    if(conversationLength <= 2){
      switch(botName){
      case COMPLIBOT: 
        delayedVoiceSsml = isQuip ? COMPLIBOT_QUIP_FOLLOW_UP : COMPLIBOT_META_FOLLOW_UP;
        break;
      case INSULTIBOT:
        delayedVoiceSsml = isQuip ? INSULTIBOT_QUIP_FOLLOW_UP : INSULTIBOT_META_FOLLOW_UP;
        break;
      }
    }else if(conversationLength <= 4){
      delayedVoiceSsml = isQuip ? QUIP_FOLLOW_UP_INTERMEDIATE : META_FOLLOW_UP_INTERMEDIATE;
    }else if(conversationLength <= 7){
      delayedVoiceSsml = isQuip ? QUIP_FOLLOW_UP_FINAL : META_FOLLOW_UP_FINAL;
    }
    return delayedVoiceSsml;
  }
}
