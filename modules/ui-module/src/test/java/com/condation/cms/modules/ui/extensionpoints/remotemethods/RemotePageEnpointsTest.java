package com.condation.cms.modules.ui.extensionpoints.remotemethods;

/*-
 * #%L
 * UI Module
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

import com.condation.cms.api.db.Content;
import com.condation.cms.api.db.ContentNode;
import com.condation.cms.api.db.ContentQuery;
import com.condation.cms.api.db.DB;
import com.condation.cms.api.db.Page;
import com.condation.cms.api.feature.features.DBFeature;
import com.condation.cms.api.module.SiteModuleContext;
import com.condation.cms.api.ui.rpc.RPCException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RemotePageEnpointsTest {

    @Mock
    private SiteModuleContext moduleContext;

    @Mock
    private DB db;

    @Mock
    private Content content;

    @Mock
    private ContentQuery query;
    
    @Mock
    private ContentQuery.Sort sort;

    private RemotePageEnpoints pageEndpoints;

    @BeforeEach
    public void setUp() {
        pageEndpoints = new RemotePageEnpoints();
        pageEndpoints.setContext(moduleContext);
    }

    @Test
    public void testFilterPages_Success() throws RPCException {
        // Arrange
        when(moduleContext.get(DBFeature.class)).thenReturn(new DBFeature(db));
        when(db.getContent()).thenReturn(content);
        when(content.query(any())).thenReturn(query);
        
        List<ContentNode> expectedNodes = List.of(mock(ContentNode.class));
        when(query.get()).thenReturn(expectedNodes);
        when(query.orderby(anyString())).thenReturn(sort);
        when(sort.desc()).thenReturn(query);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("contentType", "page");
        parameters.put("query", "title:test");
        parameters.put("excerpt", 100);
        
        List<Map<String, Object>> where = new ArrayList<>();
        Map<String, Object> clause = new HashMap<>();
        clause.put("field", "author");
        clause.put("value", "admin");
        clause.put("operator", "=");
        where.add(clause);
        parameters.put("where", where);
        
        parameters.put("orderby", "title");
        parameters.put("order", "desc");

        // Act
        Object result = pageEndpoints.filterPages(parameters);

        // Assert
        assertThat(result).isInstanceOf(List.class);
        assertThat((List<?>) result).hasSize(1);
        assertThat((List<?>) result).isEqualTo(expectedNodes);

        verify(query).contentType("page");
        verify(query).expression("title:test");
        verify(query).excerpt(100);
        verify(query).where("author", "=", "admin");
        verify(query).orderby("title");
        verify(sort).desc();
    }
    
    @Test
    public void testFilterPages_WithPagination() throws RPCException {
        // Arrange
        when(moduleContext.get(DBFeature.class)).thenReturn(new DBFeature(db));
        when(db.getContent()).thenReturn(content);
        when(content.query(any())).thenReturn(query);

        Page<ContentNode> expectedPage = new Page<>(100, 10, 10, 1, new ArrayList<>());
        when(query.page(1L, 10L)).thenReturn(expectedPage);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", "1");
        parameters.put("size", "10");

        // Act
        Object result = pageEndpoints.filterPages(parameters);

        // Assert
        assertThat(result).isInstanceOf(Page.class);
        assertThat(result).isEqualTo(expectedPage);
        verify(query).page(1L, 10L);
    }

    @Test
    public void testFilterPages_InvalidNumberFormats() throws RPCException {
        // Arrange
        when(moduleContext.get(DBFeature.class)).thenReturn(new DBFeature(db));
        when(db.getContent()).thenReturn(content);
        when(content.query(any())).thenReturn(query);
        when(query.get()).thenReturn(new ArrayList<>());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", "invalid");
        parameters.put("size", "10");
        parameters.put("excerpt", "not-a-number");

        // Act
        Object result = pageEndpoints.filterPages(parameters);

        // Assert
        assertThat(result).isInstanceOf(List.class);
        verify(query).get(); // Should fallback to get() because page parsing fails
    }
}
