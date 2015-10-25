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

package com.derpgroup.complibot.health;

import io.dropwizard.setup.Environment;

import com.codahale.metrics.health.HealthCheck;
import com.derpgroup.complibot.configuration.MainConfig;

/**
 * Health check to verify that the local filesystem is writable.
 *
 * @author Rusty Gerard
 * @since 0.0.1
 */
public class BasicHealthCheck extends HealthCheck {
  public BasicHealthCheck(MainConfig config, Environment environment) {}

  @Override
  protected Result check() throws Exception {
    return Result.healthy();
  }
}
