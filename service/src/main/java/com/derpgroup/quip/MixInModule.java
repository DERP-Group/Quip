package com.derpgroup.quip;

import com.derpgroup.derpwizard.voice.model.CommonMetadata;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class MixInModule extends SimpleModule {
  
  public MixInModule(){
    super("QuipModule"); 
  }
  
  @Override
   public void setupModule(SetupContext context)
     {
       context.setMixInAnnotations(CommonMetadata.class, CommonMetadataMixIn.class);
    }
}
