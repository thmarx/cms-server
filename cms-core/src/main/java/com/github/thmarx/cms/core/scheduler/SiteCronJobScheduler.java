package com.github.thmarx.cms.core.scheduler;

/*-
 * #%L
 * cms-core
 * %%
 * Copyright (C) 2023 - 2024 Marx-Software
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.github.thmarx.cms.api.scheduler.CronJob;
import com.github.thmarx.cms.api.scheduler.CronJobContext;
import com.github.thmarx.cms.api.scheduler.CronJobScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
@Slf4j
public class SiteCronJobScheduler implements CronJobScheduler {

	private final Scheduler scheduler;
	private final CronJobContext context;
	
	@Override
	public void schedule(String cronExpression, CronJob job) {
		JobDataMap data = new JobDataMap();
		data.put(CronJobRunner.DATA_CRONJOB, job);
		data.put(CronJobRunner.DATA_CONTEXT, context);
		JobDetail jobDetail = JobBuilder
				.newJob(CronJobRunner.class)
				.usingJobData(data)
				.build();
		
		CronTrigger trigger = TriggerBuilder.newTrigger()
				.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
				.startNow()
				.forJob(jobDetail)
				.build();
		
		try {
			scheduler.scheduleJob(jobDetail, trigger);
		} catch (SchedulerException ex) {
			log.error(null, ex);
			throw new RuntimeException(ex);
		}
	}
	
}
