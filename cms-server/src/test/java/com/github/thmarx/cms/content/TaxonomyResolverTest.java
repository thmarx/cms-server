package com.github.thmarx.cms.content;

import com.github.thmarx.cms.api.db.DB;
import com.github.thmarx.cms.api.db.taxonomy.Taxonomies;
import com.github.thmarx.cms.api.db.taxonomy.Taxonomy;
import com.github.thmarx.cms.api.feature.features.RequestFeature;
import com.github.thmarx.cms.api.mapper.ContentNodeMapper;
import com.github.thmarx.cms.api.request.RequestContext;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author t.marx
 */
@ExtendWith(MockitoExtension.class)
public class TaxonomyResolverTest {
	
	@Mock
	DB db;
	
	@Mock
	ContentRenderer contentRenderer;
	
	@Mock
	ContentNodeMapper contentNodeMapper;
	
	@Mock
	Taxonomies taxonomies;
	
	TaxonomyResolver taxonomyResolver;
	
	@BeforeEach
	public void setup () {
		Mockito.lenient().when(db.getTaxonomies()).thenReturn(taxonomies);
		Mockito.lenient().when(taxonomies.forSlug("tags")).thenReturn(Optional.of(new Taxonomy()));
		
		taxonomyResolver = new TaxonomyResolver(contentRenderer, db, contentNodeMapper);
	}

	@Test
	public void test_is_taxonomy() {
		RequestContext requestContext = new RequestContext();
		RequestFeature requestFeature = new RequestFeature("tags", Map.of());	
		requestContext.add(RequestFeature.class, requestFeature);
	
		Assertions.assertThat(taxonomyResolver.isTaxonomy(requestContext)).isTrue();
	}
	
	@Test
	public void test_is_taxonomy_with_value() {
		RequestContext requestContext = new RequestContext();
		RequestFeature requestFeature = new RequestFeature("tags/red", Map.of());	
		requestContext.add(RequestFeature.class, requestFeature);
	
		Assertions.assertThat(taxonomyResolver.isTaxonomy(requestContext)).isTrue();
	}
	
	@Test
	public void test_get_taxonomy_value() {
		RequestContext requestContext = new RequestContext();
		RequestFeature requestFeature = new RequestFeature("tags/red", Map.of());	
		requestContext.add(RequestFeature.class, requestFeature);
	
		Assertions.assertThat(taxonomyResolver.getTaxonomyValue(requestContext))
				.isPresent()
				.hasValue("red")
				;
	}
}
