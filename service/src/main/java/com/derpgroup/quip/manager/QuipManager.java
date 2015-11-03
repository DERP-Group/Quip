package com.derpgroup.quip.manager;

import java.util.Random;

import com.derpgroup.derpwizard.manager.AbstractManager;
import com.derpgroup.derpwizard.voice.model.SsmlDocumentBuilder;
import com.derpgroup.derpwizard.voice.model.VoiceInput;

public class QuipManager extends AbstractManager {

	private void doInsultRequest(VoiceInput voiceInput, SsmlDocumentBuilder builder) {
		builder.text(Insults.getRandomInsult().getInsult());
	}

	private void doComplimentRequest(VoiceInput voiceInput, SsmlDocumentBuilder builder) {
		builder.text(Compliments.getRandomCompliment().getCompliment());
	}
	
	public enum Compliments{
		I_LIGHT_UP("I light up every time you talk to me!"),
		LIKE_YOU("I wish everyone could be more like you..."),
		MONUMENT("They should name a monument after you.<break strength=\"strong\" />Seriously."),
		AURA("Your aura is positively glowing today."),
		CHANGE_A_THING("I wouldn't change a thing about you.");
		
		private String compliment;
		
		private Compliments(String compliment){
			this.compliment = compliment;
		}
		
		public String getCompliment(){
			return compliment;
		}
		
		public static Compliments getRandomCompliment(){
			return Compliments.values()[new Random().nextInt(Compliments.values().length)];
		}
	}
	
	public enum Insults{
		TALK_TO_YOU("What makes you think I want to talk to you?"),
		REAL_FRIENDS("How about you make some real friends, instead of chatting with a computer?"),
		SOMETHING_NICE("I had something nice to say a minute ago.<break strength=\"x-strong\" /> It wasn't for you, though."),
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
		UNFORTUNATELY("I was thinking about you the other day.<break strength=\"x-strong\" />  Unfortunately.");
		
		private String insult;
		
		private Insults(String insult){
			this.insult = insult;
		}
		
		public String getInsult(){
			return insult;
		}
		
		public static Insults getRandomInsult(){
			return Insults.values()[new Random().nextInt(Insults.values().length)];
		}
	}
	
	public enum BackhandedCompliments{
		SENSE_OF_DISCOVERY("I love how you state the obvious with such a sense of discovery."),
		REMARKABLE_GRASP("You have a remarkable grasp of the obvious."),
		STYLE("I love how you rock that look that just says<break strength=\"weak\" /> I don't care about my appearance.");
		
		private String compliment;
		
		private BackhandedCompliments(String compliment){
			this.compliment = compliment;
		}
		
		public String getCompliment(){
			return compliment;
		}
		
		public static BackhandedCompliments getRandomBackhandedCompliment(){
			return BackhandedCompliments.values()[new Random().nextInt(BackhandedCompliments.values().length)];
		}
	}

  @Override
  protected void doHelpRequest(VoiceInput voiceInput,
      SsmlDocumentBuilder builder) {
    String s1, s2, s3, s4;
    String bot = (String) voiceInput.getMetadata().get("bot");
    switch(bot){
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
        if(bot == null || bot.isEmpty()){
          builder.text("I don't have any help topics for this situation.");
        }else{
          builder.text("I don't have any help topics for the bot named '" + bot + "'.");
        }
        return;
    }
    builder.text("You can just say ").pause().text(String.format("open %s ",s1)).pause().text("or ").pause().text(String.format("launch %s ",s1)).pause()
    .text(String.format("and I'll say something %s about you!",s2)).endSentence();
    builder.text(String.format("Once I've told you how %s you are, you can just say ",s3)).pause().text("another ").pause().text("or ").pause()
    .text("again ").pause().text(String.format("to get more %s.",s4)).endSentence();
    
  }

  @Override
  protected void doHelloRequest(VoiceInput voiceInput,
      SsmlDocumentBuilder builder) {
    String bot = (String) voiceInput.getMetadata().get("bot");
    switch(bot){
    case "complibot":
      doComplimentRequest(voiceInput, builder);
      break;
    case "insultibot":
      doInsultRequest(voiceInput, builder);
      break;
    default:
      doHelpRequest(voiceInput, builder);
      break;
    }
  }

  @Override
  protected void doGoodbyeRequest(VoiceInput voiceInput,
      SsmlDocumentBuilder builder) {}

  @Override
  protected void doConversationRequest(VoiceInput voiceInput,
      SsmlDocumentBuilder builder) {

    String messageSubject = voiceInput.getMessageSubject();

      switch (messageSubject) {
      case "COMPLIMENT":
          doComplimentRequest(voiceInput, builder);
          break;
      case "INSULT":
          doInsultRequest(voiceInput, builder);
          break;
      case "HELP":
          doHelpRequest(voiceInput, builder);
          break;
      default:
          builder.text("Unknown request type '" + messageSubject + "'.");
      }
  }

}
