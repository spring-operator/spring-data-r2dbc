/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.r2dbc.testing;

import io.r2dbc.spi.ConnectionFactory;

import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.springframework.data.r2dbc.testing.ExternalDatabase.ProvidedDatabase;

import org.testcontainers.containers.MySQLContainer;

import com.github.jasync.r2dbc.mysql.JasyncConnectionFactory;
import com.github.jasync.sql.db.Configuration;
import com.github.jasync.sql.db.mysql.pool.MySQLConnectionFactory;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 * Utility class for testing against MySQL.
 *
 * @author Mark Paluch
 */
public class MySqlTestSupport {

	private static ExternalDatabase testContainerDatabase;

	public static String CREATE_TABLE_LEGOSET = "CREATE TABLE legoset (\n" //
			+ "    id          integer PRIMARY KEY,\n" //
			+ "    name        varchar(255) NOT NULL,\n" //
			+ "    manual      integer NULL\n" //
			+ ") ENGINE=InnoDB;";

	public static String CREATE_TABLE_LEGOSET_WITH_ID_GENERATION = "CREATE TABLE legoset (\n" //
			+ "    id          integer AUTO_INCREMENT PRIMARY KEY,\n" //
			+ "    name        varchar(255) NOT NULL,\n" //
			+ "    manual      integer NULL\n" //
			+ ") ENGINE=InnoDB;";

	/**
	 * Returns a database either hosted locally at {@code postgres:@localhost:5432/postgres} or running inside Docker.
	 *
	 * @return information about the database. Guaranteed to be not {@literal null}.
	 */
	public static ExternalDatabase database() {

		if (Boolean.getBoolean("spring.data.r2dbc.test.preferLocalDatabase")) {

			return getFirstWorkingDatabase( //
					MySqlTestSupport::local, //
					MySqlTestSupport::testContainer //
			);
		} else {

			return getFirstWorkingDatabase( //
					MySqlTestSupport::testContainer, //
					MySqlTestSupport::local //
			);
		}
	}

	@SafeVarargs
	private static ExternalDatabase getFirstWorkingDatabase(Supplier<ExternalDatabase>... suppliers) {

		return Stream.of(suppliers).map(Supplier::get) //
				.filter(ExternalDatabase::checkValidity) //
				.findFirst() //
				.orElse(ExternalDatabase.unavailable());
	}

	/**
	 * Returns a locally provided database at {@code postgres:@localhost:5432/postgres}.
	 */
	private static ExternalDatabase local() {

		return ProvidedDatabase.builder() //
				.hostname("localhost") //
				.port(3306) //
				.database("mysql") //
				.username("root") //
				.password("my-secret-pw").build();
	}

	/**
	 * Returns a database provided via Testcontainers.
	 */
	private static ExternalDatabase testContainer() {

		if (testContainerDatabase == null) {

			try {
				MySQLContainer mySQLContainer = new MySQLContainer("mysql:5.6.43");
				mySQLContainer.start();

				testContainerDatabase = ProvidedDatabase.builder() //
						.hostname("localhost") //
						.port(mySQLContainer.getFirstMappedPort()) //
						.database(mySQLContainer.getDatabaseName()) //
						.username("root") //
						.password(mySQLContainer.getPassword()).build();
			} catch (IllegalStateException ise) {
				// docker not available.
				testContainerDatabase = ExternalDatabase.unavailable();
			}
		}

		return testContainerDatabase;
	}

	/**
	 * Creates a new {@link ConnectionFactory} configured from the {@link ExternalDatabase}..
	 */
	public static ConnectionFactory createConnectionFactory(ExternalDatabase database) {

		MySQLConnectionFactory jasync = new MySQLConnectionFactory(new Configuration(database.getUsername(),
				database.getHostname(), database.getPort(), database.getPassword(), database.getDatabase()));
		return new JasyncConnectionFactory(jasync);
	}

	/**
	 * Creates a new {@link DataSource} configured from the {@link ExternalDatabase}.
	 */
	public static DataSource createDataSource(ExternalDatabase database) {

		MysqlDataSource dataSource = new MysqlDataSource();

		dataSource.setUser(database.getUsername());
		dataSource.setPassword(database.getPassword());
		dataSource.setDatabaseName(database.getDatabase());
		dataSource.setServerName(database.getHostname());
		dataSource.setPortNumber(database.getPort());

		return dataSource;
	}

}
