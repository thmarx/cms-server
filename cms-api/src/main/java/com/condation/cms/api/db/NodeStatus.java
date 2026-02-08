package com.condation.cms.api.db;

/*-
 * #%L
 * cms-api
 * %%
 * Copyright (C) 2023 - 2026 CondationCMS
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

import com.condation.cms.api.Constants;
import com.condation.cms.api.utils.DateRange;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author thmar
 */
public class NodeStatus {
	
	public static Status get (Map<String, Object> meta) {
		var published = (boolean) meta.getOrDefault(Constants.MetaFields.PUBLISHED, false);
		var publish_date = (Date) meta.getOrDefault(Constants.MetaFields.PUBLISH_DATE, Date.from(Instant.now()));
		
		var unpublish_date = (Date) meta.getOrDefault(Constants.MetaFields.UNPUBLISH_DATE, null);
		
		return new Status(published, DateRange.isNowWithin(publish_date, unpublish_date));
	}
	
	public static record Status (boolean published, boolean withinSchedule){};
}
