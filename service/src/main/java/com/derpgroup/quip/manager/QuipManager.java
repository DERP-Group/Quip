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
		LIKE_YOU("<speak><p><s>I wish everyone could be like you...</s></p></speak>"),
		MONUMENT("<speak><p><s>They should name a monument after you.<break strength=\"strong\" />Seriously.</s></p></speak>");
		
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
		SOMETHING_NICE("<speak><p><s>I had something nice to say a minute ago.<break strength=\"x-strong\" /> It wasn't for you though.</s></p></speak>"),
		LIFE_CHOICES("<speak><p><s>I'm not saying that you make poor life choices, but<break strength=\"strong\" /> alright, that's pretty much what I'm saying.</s></p></speak>");
		
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

}
