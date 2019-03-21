/*
 * Copyright 2018-2019 the original author or authors.
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
package org.springframework.data.r2dbc.function;

import io.r2dbc.spi.Connection;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import org.springframework.dao.IncorrectResultSizeDataAccessException;

/**
 * Default implementation of {@link FetchSpec}.
 *
 * @author Mark Paluch
 */
@RequiredArgsConstructor
class DefaultFetchSpec<T> implements FetchSpec<T> {

	private final ConnectionAccessor connectionAccessor;
	private final String sql;
	private final Function<Connection, Flux<T>> resultFunction;
	private final Function<Connection, Mono<Integer>> updatedRowsFunction;

	/* (non-Javadoc)
	 * @see org.springframework.data.jdbc.core.function.FetchSpec#one()
	 */
	@Override
	public Mono<T> one() {

		return all().buffer(2) //
				.flatMap(it -> {

					if (it.isEmpty()) {
						return Mono.empty();
					}

					if (it.size() > 1) {
						return Mono.error(new IncorrectResultSizeDataAccessException(
								String.format("Query [%s] returned non unique result.", this.sql), 1));
					}

					return Mono.just(it.get(0));
				}).next();
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.jdbc.core.function.FetchSpec#first()
	 */
	@Override
	public Mono<T> first() {
		return all().next();
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.jdbc.core.function.FetchSpec#all()
	 */
	@Override
	public Flux<T> all() {
		return connectionAccessor.inConnectionMany(resultFunction);
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.jdbc.core.function.FetchSpec#rowsUpdated()
	 */
	@Override
	public Mono<Integer> rowsUpdated() {
		return connectionAccessor.inConnection(updatedRowsFunction);
	}
}
