package com.condation.cms.extensions.hooks;

/*-
 * #%L
 * CMS Extensions
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */


import com.condation.cms.api.hooks.HookSystem;
import com.condation.cms.api.hooks.Hooks;
import com.condation.cms.api.scheduler.CronJobScheduler;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author t.marx
 */
@RequiredArgsConstructor
public class GlobalHooks {
	private final HookSystem globalHookSystem;
	
	private final CronJobScheduler scheduler;
	
	public void registerCronJob () {
		globalHookSystem.execute(Hooks.SCHEDULER_REGISTER.hook(), Map.of("scheduler", scheduler));
	}
	public void removeCronJob () {
		globalHookSystem.execute(Hooks.SCHEDULER_REMOVE.hook(), Map.of("scheduler", scheduler));
	}
}
