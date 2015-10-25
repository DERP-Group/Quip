package com.derpgroup.complibot.manager;

import java.util.Random;

import com.derpgroup.derpwizard.voice.model.VoiceInput;

public class ComplibotManager {
	
	public String handleRequest(VoiceInput vi) {

		ComplibotRequestTypes requestType = vi.getRequestName(ComplibotRequestTypes.class);

		switch (requestType) {
		case COMPLIMENT:
			return doComplimentRequest();
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

}
