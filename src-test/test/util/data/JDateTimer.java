/*******************************************************************************
 * Copyright (C) 2011 Julio Leite
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as 
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package test.util.data;


import static org.junit.Assert.*;

import java.util.Date;

import leitej.util.DateUtil;
import leitej.util.data.DateFieldEnum;
import leitej.util.data.DateTimer;
import leitej.util.data.DateTimerItf;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JDateTimer {
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public final void testNextStepTimer() {
		DateTimerItf dTimer;
		Date result;
		Date now;
		Date date2assert;
		
		result = (new DateTimer()).nextStepTimer();
		date2assert = null;
		assertTrue(result==date2assert);
		
		result = (new DateTimer(DateFieldEnum.MILLISECOND, 1)).nextStepTimer();
		now = DateUtil.now();
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH), 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND));	
		assertTrue(date2assert.compareTo(result)<=0);
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH), 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND)+2);	
		assertTrue(date2assert.compareTo(result)>0);
		
		result = (new DateTimer(DateFieldEnum.MILLISECOND, 10, 1)).nextStepTimer();
		now = DateUtil.now();
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH), 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND));	
		assertTrue(date2assert.compareTo(result)<=0);
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH), 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND)+2);	
		assertTrue(date2assert.compareTo(result)>0);
		
		result = (new DateTimer(DateFieldEnum.MILLISECOND, 1000)).nextStepTimer();
		now = DateUtil.now();
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH), 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND));	
		assertTrue(date2assert.compareTo(result)<=0);
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH), 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND)+2);	
		assertTrue(date2assert.compareTo(result)>0);
		
		result = (new DateTimer(DateFieldEnum.DAY_OF_MONTH, 1, 0)).nextStepTimer();
		date2assert = null;
		assertTrue(result==date2assert);
		
		result = (new DateTimer(DateFieldEnum.DAY_OF_MONTH, 1, 123)).nextStepTimer();
		now = DateUtil.now();
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH)+1, 
								0, 
								0,
								0);	
		assertTrue(date2assert.compareTo(result)<=0);
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH)+2, 
								0, 
								0,
								0);	
		assertTrue(date2assert.compareTo(result)>0);
		
		result = (new DateTimer(DateFieldEnum.DAY_OF_MONTH, 1, DateUtil.now())).nextStepTimer();
		date2assert = null;
		assertTrue(result==date2assert);
		
		Date d = DateUtil.now();
		DateUtil.add(d, DateFieldEnum.DAY_OF_MONTH, 1);
		result = (new DateTimer(DateFieldEnum.DAY_OF_MONTH, 1, d)).nextStepTimer();
		now = DateUtil.now();
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH)+1, 
								0, 
								0,
								0);	
		assertTrue(date2assert.compareTo(result)<=0);
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH)+2, 
								0, 
								0,
								0);	
		assertTrue(date2assert.compareTo(result)>0);
		
		dTimer = new DateTimer(DateUtil.now(), DateFieldEnum.DAY_OF_MONTH, 1, 1, d);
		result = dTimer.nextStepTimer();
		now = DateUtil.now();
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH)+1, 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND));	
		assertTrue(date2assert.compareTo(result)<=0);
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH)+2, 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND));	
		assertTrue(date2assert.compareTo(result)>0);
		
	}
	
	@Test
	public final void testNextStepTimer_rep_1S_2() {
		DateTimerItf dTimer;
		Date result;
		Date now;
		Date date2assert;
		
		dTimer = new DateTimer(DateFieldEnum.SECOND, 1, 2);
		result = dTimer.nextStepTimer();
		now = DateUtil.now();
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH), 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND));	
		assertTrue(date2assert.compareTo(result)<=0);
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH), 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND)+2);	
		assertTrue(date2assert.compareTo(result)>0);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		result = dTimer.nextStepTimer();
		now = DateUtil.now();
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH), 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND));	
		assertTrue(date2assert.compareTo(result)<=0);
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH), 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND)+2);	
		assertTrue(date2assert.compareTo(result)>0);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		result = dTimer.nextStepTimer();
		date2assert = null;
		assertTrue(result==date2assert);
		
	}
	
	@Test
	public final void testNextStepTimer_rep_1S_tilNOW2S() {
		DateTimerItf dTimer;
		Date result;
		Date now;
		Date date2assert;
		
		Date d = DateUtil.now();
		DateUtil.add(d, DateFieldEnum.SECOND, 2);
		dTimer = new DateTimer(DateFieldEnum.SECOND, 1, d);
		result = dTimer.nextStepTimer();
		now = DateUtil.now();
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH), 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND));	
		assertTrue(date2assert.compareTo(result)<=0);
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH), 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND)+2);	
		assertTrue(date2assert.compareTo(result)>0);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		result = dTimer.nextStepTimer();
		now = DateUtil.now();
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH), 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND));	
		assertTrue(date2assert.compareTo(result)<=0);
		date2assert = DateUtil.newDate(
								DateUtil.get(now, DateFieldEnum.YEAR), 
								DateUtil.get(now, DateFieldEnum.MONTH), 
								DateUtil.get(now, DateFieldEnum.DAY_OF_MONTH), 
								DateUtil.get(now, DateFieldEnum.HOUR_OF_DAY), 
								DateUtil.get(now, DateFieldEnum.MINUTE),
								DateUtil.get(now, DateFieldEnum.SECOND)+2);	
		assertTrue(date2assert.compareTo(result)>0);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		result = dTimer.nextStepTimer();
		date2assert = null;
		assertTrue(result==date2assert);
		
	}

}
