/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.oddjob.values.types;

import java.util.Date;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.util.Clock;
import org.oddjob.util.DefaultClock;

/**
 * 
 * @oddjob.description The date now.
 * <p>
 * @oddjob.example
 * 
 * Display the time now. Note the clock variable is passed into oddjob during 
 * the testing of this example so the time can be fixed, but run as is it
 * will be null and so the current time will be displayed.
 *
 * {@oddjob.xml.resource org/oddjob/values/types/NowExample.xml}
 * 
 * @author Rob
 */
public class NowType implements ArooaValue {

	public static class Conversions implements ConversionProvider {
		public void registerWith(ConversionRegistry registry) {
			registry.register(NowType.class, Date.class, new Convertlet<NowType, Date>() {
				public Date convert(NowType from) throws ConvertletException {
					return from.toDate();
				}
			});
		}
	}
	
	/**
	 * @oddjob.property
	 * @oddjob.description The clock to use. 
	 * @oddjob.required. No. Defaults to the system clock.
	 */
	private Clock clock = new DefaultClock();

	/** Used for toString. */
	private Date lastDate;
	
    Date toDate() {

			lastDate = clock.getDate();
			
			return lastDate;
    }

    public Clock getClock() {
		return clock;
	}

	public void setClock(Clock clock) {
		this.clock = clock;
	}

	@Override
	public String toString() {
		return "Now: " + lastDate;
	}
}
