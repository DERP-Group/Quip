package com.derpgroup.quip.manager;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.derpgroup.quip.configuration.QuipConfig;
import com.derpgroup.quip.manager.QuipManager;
import com.derpgroup.quip.model.Quip;
import com.derpgroup.quip.model.QuipStore;

public class QuipValidationTest {
  
  QuipManager manager;
  static List<Quip> quips;
  
  @BeforeClass
  public static void beforeClass_setup() throws IOException {
    QuipStore quipStore = QuipStore.getInstance();
    if(!quipStore.isInitialized()){

      QuipConfig config = new QuipConfig();
      config.setRefreshRate(10);
      File currentDir = new File(".");
      config.setComplimentsFile(currentDir.getCanonicalPath()+"/src/main/resources/quips/complibot/compliments.json");
      config.setWinsultsFile(currentDir.getCanonicalPath()+"/src/main/resources/quips/complibot/winsults.json");
      config.setInsultsFile(currentDir.getCanonicalPath()+"/src/main/resources/quips/insultibot/insults.json");
      config.setBackhandedComplimentsFile(currentDir.getCanonicalPath()+"/src/main/resources/quips/insultibot/backhandedCompliments.json");
      quipStore.init(config);
    }
    quips = new ArrayList<Quip>();
    quips.addAll(QuipStore.getInstance().getQuips(QuipType.COMPLIMENT));
    quips.addAll(QuipStore.getInstance().getQuips(QuipType.WINSULT));
    quips.addAll(QuipStore.getInstance().getQuips(QuipType.INSULT));
    quips.addAll(QuipStore.getInstance().getQuips(QuipType.BACKHANDED_COMPLIMENT));
  }
  
  @Test
  public void noInvalidNullValues() throws IOException{
    for(Quip quip : quips){
      String plainText = quip.getText();
      String ssmlText = quip.getSsml();
      String targetedPlainText = quip.getTargetableText();
      String targetedSsmlText = quip.getTargetableSsml();
      
      // Content cannot be null, ever
      assertTrue(plainText!=null);
      assertTrue(ssmlText!=null);
      
      // Must either both be null, or both have content
      assertEquals(targetedPlainText!=null,targetedSsmlText!=null);
    }
  }

  @Test
  public void noSSMLInPlaintext() {
    for(Quip quip : quips){
      String plainText = quip.getText();
      String targetedPlainText = quip.getTargetableText();
      
      // No SSML tags in plaintext
      assertFalse(plainText.toLowerCase().contains("<break"));
      assertFalse(plainText.toLowerCase().contains("<phoneme"));
      
      // If the targeted fields exist, no SSML tags in them
      if(targetedPlainText!=null && plainText!=null){
        assertFalse(targetedPlainText.toLowerCase().contains("<break"));
        assertFalse(targetedPlainText.toLowerCase().contains("<phoneme"));
      }
    }
  }
}
