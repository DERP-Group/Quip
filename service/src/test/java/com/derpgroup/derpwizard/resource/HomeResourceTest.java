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

package com.derpgroup.derpwizard.resource;

import static org.junit.Assert.assertNotNull;

import java.util.Map;

import io.dropwizard.setup.Environment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.derpgroup.derpwizard.configuration.MainConfig;

@RunWith(MockitoJUnitRunner.class)
public class HomeResourceTest {
  HomeResource resource;
  
  @Mock MainConfig mockConfig;
  @Mock Environment mockEnv;

  @Before
  public void before() throws Exception {
    resource = new HomeResource(mockConfig, mockEnv);
  }

  @Test
  public void homeSuccess() throws Exception {
    // Unit under test
    Map<String, String> result = resource.home();

    // Verify results
    assertNotNull("Result was null", result);
  }
}
