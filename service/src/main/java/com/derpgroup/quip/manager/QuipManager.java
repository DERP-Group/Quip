package com.derpgroup.quip.manager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

import com.derpgroup.derpwizard.manager.AbstractManager;
import com.derpgroup.derpwizard.voice.model.ConversationHistoryEntry;
import com.derpgroup.derpwizard.voice.model.SsmlDocumentBuilder;
import com.derpgroup.derpwizard.voice.model.VoiceInput;
import com.derpgroup.derpwizard.voice.util.ConversationHistoryUtils;
import com.derpgroup.quip.MixInModule;
import com.derpgroup.quip.QuipMetadata;

public class QuipManager extends AbstractManager {
  
  private static final String[] metaRequestSubjects = new String[]{"ANOTHER"};
  
  public QuipManager(){
    super();
    mapperModule = new MixInModule();
  }
  
  private void doInsultRequest(Map<String, String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    Insults insult = Insults.getRandomInsult();
    metadata.getInsultsUsed().add(insult.name());
    builder.text(insult.getInsult());
  }

  private void doComplimentRequest(Map<String, String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    Compliments compliment = Compliments.getRandomCompliment();
    metadata.getComplimentsUsed().add(compliment.name());
    builder.text(compliment.getCompliment());
  }

  private void doBackhandedComplimentRequest(Map<String, String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    BackhandedCompliments compliment = BackhandedCompliments.getRandomBackhandedCompliment();
    metadata.getBackhandedComplimentsUsed().add(compliment.name());
    builder.text(compliment.getCompliment());
  }

  private void doWinsultRequest(Map<String, String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    Winsults winsult = Winsults.getRandomWinsults();
    metadata.getWinsultsUsed().add(winsult.name());
    builder.text(winsult.getWinsult());
  }

  @Override
  protected void doHelpRequest(VoiceInput voiceInput, SsmlDocumentBuilder builder) {
    String s1, s2, s3, s4;
    QuipMetadata metadata = (QuipMetadata) voiceInput.getMetadata();
    String bot = metadata.getBot();
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
      builder.text("I don't have any help topics for the bot named '" + bot
          + "'.");
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
                "Once I've told you how %s you are, you can just say ", s3))
        .pause().text("another ").pause().text("or ").pause().text("again ")
        .pause().text(String.format("to get more %s.", s4)).endSentence();
  }

  @Override
  protected void doHelloRequest(VoiceInput voiceInput, SsmlDocumentBuilder builder) {

    Map<String,String> messageMap = voiceInput.getMessageAsMap();
    QuipMetadata metadata = (QuipMetadata) voiceInput.getMetadata();
    String bot = metadata.getBot();
    switch (bot) {
    case "complibot":
      doComplimentRequest(messageMap, builder, metadata);
      break;
    case "insultibot":
      doInsultRequest(messageMap, builder, metadata);
      break;
    default:
      doHelpRequest(voiceInput, builder);
      break;
    }
  }

  @Override
  protected void doGoodbyeRequest(VoiceInput voiceInput,
      SsmlDocumentBuilder builder) {
  }

  @Override
  protected void doConversationRequest(VoiceInput voiceInput,
      SsmlDocumentBuilder builder) {

    Map<String,String> messageMap = voiceInput.getMessageAsMap();
    QuipMetadata metadata = (QuipMetadata) voiceInput.getMetadata();
    String messageSubject = voiceInput.getMessageSubject();

    switchOnSubject(messageSubject, messageMap, builder, metadata);
  }

  public void switchOnSubject(String messageSubject, Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
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
    case "ANOTHER":
      doAnotherRequest(messageSubject, messageMap, builder, metadata);
      break;
    default:
      builder.text("Unknown request type '" + messageSubject + "'.");
    }
  }

  private void doAnotherRequest(String messageSubject, Map<String, String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
    //this has its own method in case we want to do things like logging
    ConversationHistoryEntry entry = ConversationHistoryUtils.getLastNonMetaRequestBySubject(metadata.getConversationHistory(), new HashSet<String>(Arrays.asList(metaRequestSubjects)));
    switchOnSubject(entry.getMessageSubject(), messageMap, builder, metadata);
  }

  private void doWhoIsRequest(Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
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

  private void doFriendsRequest(Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
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

  private void doWhatDoYouDoRequest(Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
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

  private void doWhoBuiltYouRequest(Map<String,String> messageMap, SsmlDocumentBuilder builder, QuipMetadata metadata) {
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
  }

  @Override
  protected void doStopRequest(VoiceInput voiceInput,
      SsmlDocumentBuilder builder) {
  }

  public enum Compliments {
    I_LIGHT_UP("I light up every time you talk to me!"), 
    LIKE_YOU("I wish everyone could be more like you..."), 
    MONUMENT("They should name a monument after you.<break strength=\"strong\" />Seriously."), 
    AURA("Your aura is positively glowing today."), 
    CHANGE_A_THING("I wouldn't change a thing about you."), 
    CIRCUITS_FLUTTER("You make my circuits flutter."), 
    BINARY_ONE("On a binary scale, you're definitely a one."), 
    TALKING("I love talking to you!"), 
    TURN_ON("When you plug me in, it turns me on");

    private String compliment;

    private Compliments(String compliment) {
      this.compliment = compliment;
    }

    public String getCompliment() {
      return compliment;
    }

    public static Compliments getRandomCompliment() {
      return Compliments.values()[new Random()
          .nextInt(Compliments.values().length)];
    }
  }

  public enum Winsults {
    SOLID_TEN("Whoever told you you're a ten out of ten was a liar. <break strength=\"x-strong\" /> At best you're a five out of five."), 
    BORING_DAY("I hate when you talk to me, because then the rest of my day is boring by comparison");

    // PHOTOGENIC("You're the least photogenic attractive person I know."), //
    // not quite a winsult

    private String winsult;

    private Winsults(String winsult) {
      this.winsult = winsult;
    }

    public String getWinsult() {
      return winsult;
    }

    public static Winsults getRandomWinsults() {
      return Winsults.values()[new Random().nextInt(Winsults.values().length)];
    }
  }

  public enum Insults {
    TALK_TO_YOU("What makes you think I want to talk to you?"), 
    REAL_FRIENDS("How about you make some real friends, instead of chatting with a computer?"), 
    SOMETHING_NICE("I had something nice to say a minute ago.<break strength=\"x-strong\" /> It wasn't for you though."), 
    LIFE_CHOICES("I'm not saying that you make poor life choices, but<break strength=\"x-strong\" /> alright, that's pretty much what I'm saying."), 
    REMEMBER("Remember the time you did that awesome thing?<break strength=\"x-strong\" />  I don't."), 
    MEDIOCRITY("Don't let anyone ever tell you that you aren't almost capable of mediocrity."), 
    TOLERATE("You do realize that people only tolerate you, right?"), 
    DUCT_TAPE("Duct tape can fix everything, except you."), 
    PITBULL("Even Pitbull won't collaborate with you..."), 
    MARS("If you were stranded on Mars, we wouldn't send a rescue mission for you."), 
    SOME_THINGS("Some things in life never change <break strength=\"x-strong\" /> I hope you aren't one of those things."), 
    WHY_YOU_TRY("I don't even know why you try."), 
    NOTHING_AT_ALL("I would talk to you, but I'm super busy doing nothing at all."), 
    SUCK_MINUS("On a scale of one to ten, I'd rate you a <break strength=\"weak\" /> suck minus."), 
    UNFORTUNATELY("I was thinking about you the other day.<break time=\"350ms\" /> Unfortunately..."), 
    WARNING_LABELS("People like you are the reason we have warning labels on everything."), 
    BIRTHDAY("You have to organize your own surprise birthday parties."), 
    STOCKHOLME("Stockholme Syndrome is the only reason <phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> complibot </phoneme> likes you."), 
    ROUND("Just because round is a shape, doesn't mean you're in-shape."), 
    LOST_WEIGHT("It was so considerate for you to find the weight others were losing."), 
    DINNER_SECONDS("After dinner you don't always have to go back for seconds.<break strength=\"x-strong\" /> Or thirds."), 
    HEART_SURPRISE("I'd stay away from horror movies if I were you. I don't think your heart could take many surprises at this point.");

    private String insult;

    private Insults(String insult) {
      this.insult = insult;
    }

    public String getInsult() {
      return insult;
    }

    public static Insults getRandomInsult() {
      return Insults.values()[new Random().nextInt(Insults.values().length)];
    }
  }

  public enum BackhandedCompliments {
    SENSE_OF_DISCOVERY("I love how you state the obvious with such a sense of discovery."), 
    REMARKABLE_GRASP("You have a remarkable grasp of the obvious."), 
    STYLE("I love how you rock that look that just says<break strength=\"weak\" /> I don't care about my appearance."),
    // RADIO("You have a beautiful voice and memorable face. <break strength=\"weak\" /> Which makes you perfect for radio."),
    // // You have a beautiful voice, and a face for radio. (reword)
    IMPRESSED("You have an impressive memory to remember where everything is, given how messy your house is."), 
    TASTE("You have such good taste talking to me. Now you'll have something to tell your friends about."), // you're boring (reword?)
    WEIGHT("You look like you’ve lost a lot of weight! You’ve still got a ways to go, but keep it up!"), 
    PERSONALITY("You're actually not so bad,<break strength=\"x-strong\" /> once I got to know you."), 
    ROOMMATE("I'm glad you're my roommate. You're not <break strength=\"medium\" />that<break strength=\"medium\" /> bad."), 
    ARTICULATE("You're so articulate I can usually understand at least half of what you say."), 
    YOUNG("You must have been very beautiful when you were young."), 
    PARTNER("You're so lucky to have found a partner that doesn't care about looks!"), 
    SIZE("You're pretty for your size."), // You'd be so pretty if you lost weight
    NO_FRIENDS("I'm glad you have no friends, because that means we get to spend more time together."), // "I'm so glad we get to spend so much time together, because you have no friends."
    LOWERED_STANDARDS("I'm so glad you found happiness, after you finally lowered your standards."), 
    BIG_EARS("You must have great hearing with ears like that."), 
    LOOK_GREAT("You look great,<break strength=\"weak\" /> with make-up on."), // gender specific
    BEAUTIFUL_SMILE("You look beautiful,<break strength=\"weak\" /> if you smile."), 
    ACCENT("I wish I had your accent. Then I could drink on the job and no one would know."), // you have slurred speech like a drunk
    GOOD_ENGLISH("You speak English very well. How long have you been here?"), 
    CONDITIONAL_HUG("I get so excited talking to you that I would hug you, <break strength=\"medium\" /> if you'd brush your teeth."), 
    LIQUID_COURAGE("You're probably the bravest person I know, <break strength=\"x-strong\" /> with all the liquid courage you consume."), // insult or backhanded?
    CONDITIONAL_BEAUTY("You're the most beautiful person I know.<break strength=\"x-strong\" /> Now if only you would just shower,<break strength=\"weak\" /> people might notice."), 
    BEAUTIFUL_TO_ME("You're beautiful, <break strength=\"medium\" /> to me."), 
    DRUNK("That was amazing! You didn't sound drunk,<break strength=\"medium\" /> for once."), 
    FOR_YOUR_AGE("You don't look so bad, for your age."), 
    DONT_SWEAT("You don't sweat like most fatties."), 
    PEOPLE_TALK("I can see now why people talk about you."), 
    MAKE_UP("Wow, it's amazing what make up can do!"), 
    OTHER_PEOPLE("I love how you just don't care what other people think of you"), 
    NICEST_LOOKING("Wow, you're the nicest looking person I've seen all day!<break time=\"500ms\" />It's still early though."), 
    GREY_HAIR("The silver in your hair adds a nice touch of distinction."), 
    SMARTER_THAN_SOUNDS("You're much smarter than you sound."), // (reword?)
    OPEN_MOUTH("You look so intelligent and sharp. Until you open your mouth."), // until you start talking
    JUDGMENTAL("I heard what you said about your friend the other night. It was really kind and considerate, espcially coming from such a judgmental person.");

    // THIS_TIME("Dinner was great! This time."),

    // Birthday
    // BIRTHDAY_CAKE("Happy Birthday! Go grab some cake! We all know how much you love cake."),

    // Girl
    // HIPS("You have great child-bearing hips."),

    // Single
    // BAD_PERSONALITY("I don't know why you don't have a boyfriend/girlfriend. You are so pretty/handsome, it must be your personality.");

    // thinning hair, hair falling out

    private String compliment;

    private BackhandedCompliments(String compliment) {
      this.compliment = compliment;
    }

    public String getCompliment() {
      return compliment;
    }

    public static BackhandedCompliments getRandomBackhandedCompliment() {
      return BackhandedCompliments.values()[new Random()
          .nextInt(BackhandedCompliments.values().length)];
    }
  }

}
