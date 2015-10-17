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

package com.derpgroup.derpwizard;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.derpgroup.derpwizard.configuration.MainConfig;
import com.derpgroup.derpwizard.health.BasicHealthCheck;
import com.derpgroup.derpwizard.resource.HomeResource;

@RunWith(MockitoJUnitRunner.class)
public class AppTest {
  App app;

  @Mock MainConfig mockConfig;
  @Mock Environment mockEnv;
  @Mock HealthCheckRegistry mockHealthChecks;
  @Mock JerseyEnvironment mockJersey;

  @Before
  public void before() throws Exception {
    app = new App();

    when(mockEnv.healthChecks()).thenReturn(mockHealthChecks);
    when(mockEnv.jersey()).thenReturn(mockJersey);
  }

  @Test
  public void runSuccess() throws Exception {
    // Unit under test
    app.run(mockConfig, mockEnv);

    // Verify that health checks and resources are correctly registered
    verify(mockHealthChecks).register(Matchers.matches("basics"), Matchers.any(BasicHealthCheck.class));
    verify(mockJersey).register(Matchers.any(HomeResource.class));
  }
}
