/**
 * Copyright (C) 2015 David Phillips
 * Copyright (C) 2015 Eric Olson
 * Copyright (C) 2015 Rusty Gerard
 * Copyright (C) 2015 Paul Winters
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.derpgroup.quip;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.IOException;

import com.derpgroup.quip.bots.complibot.resource.CompliBotAlexaFlashBriefingResource;
import com.derpgroup.quip.bots.complibot.resource.CompliBotAlexaResource;
import com.derpgroup.quip.bots.insultibot.resource.InsultiBotAlexaFlashBriefingResource;
import com.derpgroup.quip.bots.insultibot.resource.InsultiBotAlexaResource;
import com.derpgroup.quip.configuration.MainConfig;
import com.derpgroup.quip.health.BasicHealthCheck;
import com.derpgroup.quip.model.QuipStore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Main method for spinning up the HTTP server.
 *
 * @author Rusty Gerard
 * @since 0.0.1
 */
public class App extends Application<MainConfig> {

  public static void main(String[] args) throws Exception {
	  if(args.length == 0){
		  args = new String[]{"server", "../quip_local.json"};
	  }
    new App().run(args);
  }

  @Override
  public void initialize(Bootstrap<MainConfig> bootstrap) {}

  @Override
  public void run(MainConfig config, Environment environment) throws IOException {
    ObjectMapper mapper = environment.getObjectMapper();
    if (config.isPrettyPrint()) {
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    if (config.isIgnoreUnknownJsonProperties()) {
      mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    // Health checks
    environment.healthChecks().register("basics", new BasicHealthCheck(config, environment));
    
    QuipStore quipStore = QuipStore.getInstance();
    quipStore.init(config.getQuipConfig());

    // Resources
    environment.jersey().register(new CompliBotAlexaResource(config, environment));
    environment.jersey().register(new InsultiBotAlexaResource(config, environment));
    environment.jersey().register(new CompliBotAlexaFlashBriefingResource(config, environment));
    environment.jersey().register(new InsultiBotAlexaFlashBriefingResource(config, environment));
  }
}
