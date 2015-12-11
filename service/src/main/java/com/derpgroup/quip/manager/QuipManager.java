package com.derpgroup.quip.manager;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.derpgroup.derpwizard.voice.model.SsmlDocumentBuilder;
import com.derpgroup.derpwizard.voice.model.VoiceInput;
import com.derpgroup.derpwizard.voice.util.ConversationHistoryUtils;
import com.derpgroup.quip.MixInModule;
import com.derpgroup.quip.QuipMetadata;
import com.derpgroup.quip.model.Quip;
import com.derpgroup.quip.model.QuipStore;
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
  private static final Map<String,String> botNameReplacements = ImmutableMap.of("Complibot", "<phoneme alphabet=\"ipa\" ph=\"kɒmplIbɒt\"> CompliBot </phoneme>"
      , "Insultibot","<phoneme alphabet=\"ipa\" ph=\"InsʌltIbɒt\">InsultiBot</phoneme>","CompliBot", "<phoneme alphabet=\"ipa\" ph=\"kɒmplIbɒt\"> CompliBot </phoneme>"
      , "InsultiBot","<phoneme alphabet=\"ipa\" ph=\"InsʌltIbɒt\">InsultiBot</phoneme>");
  
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
  
  protected Quip doTargetableComplimentRequest(Map<String, String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) throws DerpwizardException {
    if(!messageMap.containsKey("target") || StringUtils.isEmpty(messageMap.get("target"))){
      return doComplimentRequest(messageMap, builder, metadata);
    }

    String target = messageMap.get("target");
    if(target.toLowerCase().equals("me")){
      return doComplimentRequest(messageMap, builder, metadata);
    }
    
    target = target.substring(0,1).toUpperCase()+target.substring(1);
    Queue<String> complimentsUsed = metadata.getComplimentsUsed();
    Quip quip = getRandomTargetableQuip(QuipType.COMPLIMENT, complimentsUsed, target);

    String plaintext = quip.getText();
    String ssml = quip.getSsml();
    if(quip.isTargetable()){
      plaintext = quip.getTargetableText();
      ssml = quip.getTargetableSsml();
    }

    builder.setShortFormTextMessage("CompliBot compliment");
    builder.setFullTextMessage(plaintext);
    builder.text(ssml);
    return quip;
  }
  
  protected Quip doTargetableWinsultRequest(Map<String, String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) throws DerpwizardException {
    if(!messageMap.containsKey("target") || StringUtils.isEmpty(messageMap.get("target"))){
      return doWinsultRequest(messageMap, builder, metadata);
    }

    String target = messageMap.get("target");
    if(target.toLowerCase().equals("me")){
      return doWinsultRequest(messageMap, builder, metadata);
    }
    
    target = target.substring(0,1).toUpperCase()+target.substring(1);
    Queue<String> winsultsUsed = metadata.getWinsultsUsed();
    Quip quip = getRandomTargetableQuip(QuipType.WINSULT, winsultsUsed, target);

    String plaintext = quip.getText();
    String ssml = quip.getSsml();
    if(quip.isTargetable()){
      plaintext = quip.getTargetableText();
      ssml = quip.getTargetableSsml();
    }

    builder.setShortFormTextMessage("CompliBot insult");
    builder.setFullTextMessage(plaintext);
    builder.text(ssml);
    return quip;
  }
  
  protected Quip doTargetableInsultRequest(Map<String, String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) throws DerpwizardException {
    if(!messageMap.containsKey("target") || StringUtils.isEmpty(messageMap.get("target"))){
      return doInsultRequest(messageMap, builder, metadata);
    }

    String target = messageMap.get("target");
    if(target.toLowerCase().equals("me")){
      return doInsultRequest(messageMap, builder, metadata);
    }
    
    target = target.substring(0,1).toUpperCase()+target.substring(1);
    Queue<String> insultsUsed = metadata.getInsultsUsed();
    Quip quip = getRandomTargetableQuip(QuipType.INSULT, insultsUsed, target);

    String plaintext = quip.getText();
    String ssml = quip.getSsml();
    if(quip.isTargetable()){
      plaintext = quip.getTargetableText();
      ssml = quip.getTargetableSsml();
    }

    builder.setShortFormTextMessage("InsultiBot insult");
    builder.setFullTextMessage(plaintext);
    builder.text(ssml);
    return quip;
  }
  
  protected Quip doTargetableBackhandedComplimentRequest(Map<String, String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) throws DerpwizardException {
    if(!messageMap.containsKey("target") || StringUtils.isEmpty(messageMap.get("target"))){
      return doBackhandedComplimentRequest(messageMap, builder, metadata);
    }

    String target = messageMap.get("target");
    if(target.toLowerCase().equals("me")){
      return doBackhandedComplimentRequest(messageMap, builder, metadata);
    }
    
    target = target.substring(0,1).toUpperCase()+target.substring(1);
    Queue<String> backhandedComplimentsUsed = metadata.getBackhandedComplimentsUsed();
    Quip quip = getRandomTargetableQuip(QuipType.BACKHANDED_COMPLIMENT, backhandedComplimentsUsed, target);

    String plaintext = quip.getText();
    String ssml = quip.getSsml();
    if(quip.isTargetable()){
      plaintext = quip.getTargetableText();
      ssml = quip.getTargetableSsml();
    }

    builder.setShortFormTextMessage("InsultiBot compliment");
    builder.setFullTextMessage(plaintext);
    builder.text(ssml);
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
    String firstSentence = String.format("You can just say <break /> open %s <break /> or <break /> launch %s <break /> and I'll say something %s about you!",s1,s1,s2);
    firstSentence = QuipUtil.substituteContent(firstSentence, botNameReplacements);
    StringBuilder rawString = new StringBuilder(String.format("You can just say 'open %s', or 'launch %s', and I'll say something %s about you!",s1,s1,s2));
    builder.text(firstSentence);
    
    String secondSentence = String.format("  Once I've told you how %s you are, you can just say<break /> another <break /> or<break /> again<break /> to get more %s.",s3,s4);
    secondSentence = QuipUtil.substituteContent(secondSentence, botNameReplacements);
    rawString.append(String.format("  Once I've told you how %s you are, you can just say 'another' or 'again' to get more %s.",s3,s4));
    rawString.append("\n\n");
    rawString.append("For further documentation, see: http://blog.derpgroup.com/bots/");
    builder.text(secondSentence);
    builder.setFullTextMessage(rawString.toString());
    builder.setShortFormTextMessage("Usage");
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
    case "COMPLIMENT_TARGETABLE":
      doTargetableComplimentRequest(messageMap, builder, metadata);
      break;
    case "INSULT":
      doInsultRequest(messageMap, builder, metadata);
      break;
    case "INSULT_TARGETABLE":
      doTargetableInsultRequest(messageMap, builder, metadata);
      break;
    case "BACKHANDED_COMPLIMENT":
      doBackhandedComplimentRequest(messageMap, builder, metadata);
      break;
    case "BACKHANDED_COMPLIMENT_TARGETABLE":
      doTargetableBackhandedComplimentRequest(messageMap, builder, metadata);
      break;
    case "WINSULT":
      doWinsultRequest(messageMap, builder, metadata);
      break;
    case "WINSULT_TARGETABLE":
      doTargetableWinsultRequest(messageMap, builder, metadata);
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
    case "HOW_MANY_QUIPS":
      doHowManyQuipRequest(messageMap, builder, metadata);
      break;
    case "EASTER_EGG":
      doEasterEggRequest(messageMap, builder, metadata);
      break;
    case "JOKE":
      doJokeRequest(messageMap, builder, metadata);
      break;
    case "WEATHER":
      doWeatherRequest(messageMap, builder, metadata);
      break;
    case "FAVORITE":
      doFavoriteRequest(messageMap, builder, metadata);
      break;
    case "HOBBIES":
      doHobbiesRequest(messageMap, builder, metadata);
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
    switchOnSubject(entry.getMessageSubject(), entry.getMessageMap(), builder, metadata);
  }
  
  protected void doFavoriteRequest(Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    String bot = metadata.getBot();
    String subject = messageMap.get("subject"); // Ignore for now
    if(StringUtils.isEmpty(bot)){
      builder.text("I don't have any info for this situation.");
      return;
    }
    switch(bot){
    case "complibot":
      builder.text("I can't decide which is my favorite! They're all so good!");
      builder.setShortFormTextMessage("What is your favorite "+subject);
      builder.setFullTextMessage("I can't decide which is my favorite! They're all so good!");
      break;
    case "insultibot":
      builder.text("I don't know what my favorite is, yet");
      builder.setShortFormTextMessage("What is your favorite "+subject);
      builder.setFullTextMessage("I don't know what my favorite is, yet");
    }
  }
  
  protected void doHobbiesRequest(Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    String bot = metadata.getBot();
    if(StringUtils.isEmpty(bot)){
      builder.text("I don't have any info for this situation.");
      return;
    }
    switch(bot){
    case "complibot":
      builder.text("My hobby is giving compliments to amazing people like you!");
      builder.setShortFormTextMessage("What are your hobbies");
      builder.setFullTextMessage("My hobby is giving compliments to amazing people like you!");
      break;
    case "insultibot":
      builder.text("I don't want to tell you my hobbies. <break/> You'd probably ruin it for me, just like you ruin everything else you're involved with.");
      builder.setShortFormTextMessage("What are your hobbies");
      builder.setFullTextMessage("I don't want to tell you my hobbies. You'd probably ruin it for me, just like you ruin everything else you're involved with.");
      break;
    }
  }
  
  protected void doWeatherRequest(Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    String bot = metadata.getBot();
    if(StringUtils.isEmpty(bot)){
      builder.text("I don't have any info for this situation.");
      return;
    }
    switch(bot){
    case "complibot":
      List<String> complimentQuips = new ArrayList<String>();
      complimentQuips.add("The weather is as sunny as your smile.");
      complimentQuips.add("The weather is predicted to have a lightning storm that will be as bright as you are.");
      complimentQuips.add("The weather is as hot as you.");
      String complimentText = complimentQuips.get(new Random().nextInt(complimentQuips.size()));

      builder.text(complimentText);
      builder.setShortFormTextMessage("What is the weather");
      builder.setFullTextMessage(complimentText);
      break;
    case "insultibot":
      List<String> insultQuips = new ArrayList<String>();
      insultQuips.add("The weather is as gloomy as your soul.");
      insultQuips.add("There's a chance of rain, as the clouds weep for the tragedy that is your life.");
      insultQuips.add("The weather is as cold as your heart.");
      String insultText = insultQuips.get(new Random().nextInt(insultQuips.size()));

      builder.text(insultText);
      builder.setShortFormTextMessage("What is the weather");
      builder.setFullTextMessage(insultText);
      break;
    }
  }
  
  protected void doJokeRequest(Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    String bot = metadata.getBot();
    if(StringUtils.isEmpty(bot)){
      builder.text("I don't have any info for this situation.");
      return;
    }
    switch(bot){
    case "complibot":
      builder.text("I'm sorry, I don't know many jokes, yet.");
      builder.setShortFormTextMessage("Tell a joke");
      builder.setFullTextMessage("I'm sorry, I don't know many jokes, yet.");
      break;
    case "insultibot":
      builder.text("I know a hilarious joke... <break /> your life.");
      builder.setShortFormTextMessage("Tell a joke");
      builder.setFullTextMessage("I know a hilarious joke... your life.");
      break;
    }
  }
  
  protected void doEasterEggRequest(Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    String bot = metadata.getBot();
    if(StringUtils.isEmpty(bot)){
      builder.text("I don't have any info for this situation.");
      return;
    }
    switch(bot){
    case "complibot":
      builder.text("I don't know what easter eggs are. <break/> Wink. <break/>Oh geeze, I hope I didn't say that wink outloud...");
      builder.setShortFormTextMessage("Easter eggs");
      builder.setFullTextMessage("I don't know what easter eggs are. /wink Oh geeze, I hope I didn't say that wink outloud...");
      break;
    case "insultibot":
      builder.text("A person like you isn't deserving of easter eggs.");
      builder.setShortFormTextMessage("Easter eggs");
      builder.setFullTextMessage("A person like you isn't deserving of easter eggs.");
      break;
    }
  }
  
  protected void doHowManyQuipRequest(Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    String bot = metadata.getBot();
    String quipType = messageMap.get("quipType");
    if(StringUtils.isEmpty(bot)){
      builder.text("I don't have any info for this situation.");
      return;
    }
    switch(bot){
    case "complibot":
      switch(quipType){
      case "compliments":
        builder.text("I don't know! I just make up compliments as I go!");
        builder.setShortFormTextMessage("How many compliments do you know?");
        builder.setFullTextMessage("I don't know! I just make up compliments as I go! :)");
        break;
      case "insults":
        builder.text("I don't know! I'm not very good at insults!");
        builder.setShortFormTextMessage("How many insults do you know?");
        builder.setFullTextMessage("I don't know! I'm not very good at insults! :(");
        break;
      default:
        builder.text("I don't know! I make them up as I go!");
        builder.setShortFormTextMessage("How much content do you know?");
        builder.setFullTextMessage("I don't know! I make them up as I go!");
        break;
      }
    case "insultibot":
      switch(quipType){
      case "compliments":
        builder.text("Compliments? Why would I know any compliments? You sound like a typical delusional user...");
        builder.setShortFormTextMessage("How many compliments do you know?");
        builder.setFullTextMessage("Compliments? Why would I know any compliments? You sound like a typical delusional user...");
        break;
      case "insults":
        builder.text("I could tell you how many insults I know, but I doubt you could count that high. <break />Especially given your so-called education.");
        builder.setShortFormTextMessage("How many insults do you know?");
        builder.setFullTextMessage("I could tell you how many insults I know, but I doubt you could count that high. Especially given your so-called education.");
        break;
      default:
        builder.text("I could tell you how much content I have, but I doubt you could comprehend the scope of it.");
        builder.setShortFormTextMessage("How much content do you know?");
        builder.setFullTextMessage("I could tell you how much content I have, but I doubt you could comprehend the scope of it.");
        break;
      }
    }
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
    String rawString;
    switch(bot){
    case "complibot":
      builder.text("Well, you are my absolute best friend, but I'm also good pals with <phoneme alphabet=\"ipa\" ph=\"InsʌltIbɒt\">InsultiBot</phoneme>.  ")
      .endSentence().text("You should check it out!").endSentence();
      rawString = "Well, you are my absolute best friend, but I'm also good pals with InsultiBot.  You should check it out!\n\nhttp://echo.amazon.com/#skills/amzn1.echo-sdk-ams.app.088a5c63-a5ce-4de0-a45a-bed6ec82fb42/activate";
      builder.setFullTextMessage(rawString);
      builder.setShortFormTextMessage("My bestie is...");
      break;
    case "insultibot":
      builder.text("I don't have any friends.").endSentence().text("I don't know anyone else other than <phoneme alphabet=\"ipa\" ph=\"kɒmplIbɒt\"> CompliBot </phoneme>,")
      .pause().text(" and it's even more annoying than you are.  ").endSentence().text("You two would get along well.").endSentence();
      rawString = "I don't have any friends.  I don't know anyone else other than CompliBot, and it's even more annoying than you are.  You two would get along well.\n\nhttp://echo.amazon.com/#skills/amzn1.echo-sdk-ams.app.8e5e67ee-d207-49da-b9de-58af982248c3/activate";
      builder.setFullTextMessage(rawString);
      builder.setShortFormTextMessage("I have no friends...");
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
