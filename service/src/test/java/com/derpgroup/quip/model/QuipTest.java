package com.derpgroup.quip.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class QuipTest {

  @Test
  public void testQuip_isTargetable_nullTargetable(){
    Quip quip = new Quip();
    quip.setQuipGroup("TEST_QUIP");
    quip.setText("Sample quip text");
    quip.setSsml("Sample quip <break /> text");
    assertEquals(false,quip.isTargetable());
  }
  
  @Test
  public void testQuip_isTargetable_emptyTargetable(){
    Quip quip = new Quip();
    quip.setQuipGroup("TEST_QUIP");
    quip.setText("Sample quip text");
    quip.setSsml("Sample quip <break /> text");
    quip.setTargetableText("");
    quip.setTargetableSsml("");
    assertEquals(false,quip.isTargetable());
  }
  
  @Test
  public void testQuip_isTargetable_malformedHalfTargetable1(){
    Quip quip = new Quip();
    quip.setQuipGroup("TEST_QUIP");
    quip.setText("Sample quip text");
    quip.setSsml("Sample quip <break /> text");
    quip.setTargetableText(null);
    quip.setTargetableSsml("");
    assertEquals(false,quip.isTargetable());
  }
  
  @Test
  public void testQuip_isTargetable_malformedHalfTargetable2(){
    Quip quip = new Quip();
    quip.setQuipGroup("TEST_QUIP");
    quip.setText("Sample quip text");
    quip.setSsml("Sample quip <break /> text");
    quip.setTargetableText("");
    quip.setTargetableSsml(null);
    assertEquals(false,quip.isTargetable());
  }
  
  @Test
  public void testQuip_isTargetable_completeTargetable(){
    Quip quip = new Quip();
    quip.setQuipGroup("TEST_QUIP");
    quip.setText("Sample quip text");
    quip.setSsml("Sample quip <break /> text");
    quip.setTargetableText("Sample targetable quip text");
    quip.setTargetableSsml("Sample targetable quip <break /> text");
    assertEquals(true,quip.isTargetable());
  }
}
