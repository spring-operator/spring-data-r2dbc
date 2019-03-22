/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.r2dbc.repository;

import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.r2dbc.repository.query.Query;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory;
import org.springframework.data.r2dbc.testing.ExternalDatabase;
import org.springframework.data.r2dbc.testing.MySqlTestSupport;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests for {@link LegoSetRepository} using {@link R2dbcRepositoryFactory} against MySQL.
 *
 * @author Mark Paluch
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@Ignore("Jasync does not consider generated keys retrieval on Statement.returnGeneratedValues(), see jasync-sql/jasync-sql#105")
public class MySqlR2dbcRepositoryIntegrationTests extends AbstractR2dbcRepositoryIntegrationTests {

	@ClassRule public static final ExternalDatabase database = MySqlTestSupport.database();

	@Configuration
	@EnableR2dbcRepositories(considerNestedRepositories = true,
			includeFilters = @Filter(classes = MySqlLegoSetRepository.class, type = FilterType.ASSIGNABLE_TYPE))
	static class IntegrationTestConfiguration extends AbstractR2dbcConfiguration {

		@Override
		public ConnectionFactory connectionFactory() {
			return MySqlTestSupport.createConnectionFactory(database);
		}
	}

	@Override
	protected DataSource createDataSource() {
		return MySqlTestSupport.createDataSource(database);
	}

	@Override
	protected ConnectionFactory createConnectionFactory() {
		return MySqlTestSupport.createConnectionFactory(database);
	}

	@Override
	protected String getCreateTableStatement() {
		return MySqlTestSupport.CREATE_TABLE_LEGOSET_WITH_ID_GENERATION;
	}

	@Override
	protected Class<? extends LegoSetRepository> getRepositoryInterfaceType() {
		return MySqlLegoSetRepository.class;
	}

	interface MySqlLegoSetRepository extends LegoSetRepository {

		@Override
		@Query("SELECT * FROM legoset WHERE name like ?")
		Flux<LegoSet> findByNameContains(String name);

		@Override
		@Query("SELECT * FROM legoset")
		Flux<Named> findAsProjection();

		@Override
		@Query("SELECT * FROM legoset WHERE manual = :manual")
		Mono<LegoSet> findByManual(int manual);

		@Override
		@Query("SELECT id FROM legoset")
		Flux<Integer> findAllIds();
	}
}