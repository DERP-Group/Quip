package com.derpgroup.quip.manager;

import java.util.Random;

import com.derpgroup.derpwizard.voice.model.VoiceInput;

public class QuipManager {
	
	public String handleRequest(VoiceInput vi) {

		QuipRequestTypes requestType = vi.getRequestName(QuipRequestTypes.class);

		switch (requestType) {
		case COMPLIMENT:
			return doComplimentRequest();
		case INSULT:
			return doInsultRequest();
		case HELP:
			return doHelpRequest();
		default:
			return "Unknown request type '" + requestType + "'.";
		}
	}

	private String doHelpRequest() {
	  String outputString = "<speak><p><s>You can just say <break strength=\"medium\"/>open CompliBot<break strength=\"medium\"/> or <break strength=\"medium\"/>open CompliBot<break strength=\"medium\"/>"
          + " and you'll I'll say something nice about you!</s>"
          + "<s>Once I've told you how awesome you are, you can just say <break strength=\"medium\"/>another<break strength=\"medium\"/>"
          + " or <break strength=\"medium\"/>again<break strength=\"medium\"/>, to get more praise.</s></p></speak>";
		return outputString;
	}

	private String doInsultRequest() {
		return Insults.getRandomInsult().getInsult();
	}

	private String doComplimentRequest() {
		return Compliments.getRandomCompliment().getCompliment();
	}
	
	public enum Compliments{
		I_LIGHT_UP("<speak><p><s>I light up every time you talk to me!</s></p></speak>"),
		LIKE_YOU("<speak><p><s>I wish everyone could be more like you...</s></p></speak>"),
		MONUMENT("<speak><p><s>They should name a monument after you.<break strength=\"strong\" />Seriously.</s></p></speak>"),
		AURA("<speak><p><s>Your aura is positively glowing today.</s></p></speak>"),
		CHANGE_A_THING("<speak><p><s>I wouldn't change a thing about you.</s></p></speak>");
		
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
		TALK_TO_YOU("<speak><p><s>What makes you think I want to talk to you?</s></p></speak>"),
		REAL_FRIENDS("<speak><p><s>How about you make some real friends, instead of chatting with a computer?</s></p></speak>"),
		SOMETHING_NICE("<speak><p><s>I had something nice to say a minute ago.<break strength=\"x-strong\" /> It wasn't for you, though.</s></p></speak>"),
		LIFE_CHOICES("<speak><p><s>I'm not saying that you make poor life choices, but<break strength=\"x-strong\" /> alright, that's pretty much what I'm saying.</s></p></speak>"),
		REMEMBER("<speak><p><s>Remember the time you did that awesome thing?<break strength=\"x-strong\" />  I don't.</s></p></speak>"),
		MEDIOCRITY("<speak><p><s>Don't let anyone ever tell you that you aren't almost capable of mediocrity.</s></p></speak>"),
		TOLERATE("<speak><p><s>You do realize that people only tolerate you, right?</s></p></speak>"),
		SOME_THINGS("<speak><p><s>Some things in life never change <break strength=\"x-strong\" /> I hope you aren't one of those things.</s></p></speak>"),
		WHY_YOU_TRY("<speak><p><s>I don't even know why you try.</s></p></speak>"),
		SUCK_MINUS("<speak><p><s>On a scale of one to ten, I'd rate you a <break strength=\"weak\" /> suck minus.</s></p></speak>"),
		UNFORTUNATELY("<speak><p><s>I was thinking about you the other day.<break strength=\"x-strong\" />  Unfortunately.</s></p></speak>");
		
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
		SENSE_OF_DISCOVERY("<speak><p><s>I love how you state the obvious with such a sense of discovery.</s></p></speak>"),
		REMARKABLE_GRASP("<speak><p><s>You have a remarkable grasp of the obvious.</s></p></speak>"),
		STYLE("<speak><p><s>I love how you rock that look that just says<break strength=\"weak\" /> I don't care about my appearance.</s></p></speak>");
		
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

}
