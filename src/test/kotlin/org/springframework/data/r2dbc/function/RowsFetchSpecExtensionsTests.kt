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
package org.springframework.data.r2dbc.function

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import reactor.core.publisher.Mono

/**
 * Unit tests for [RowsFetchSpec] extensions.
 *
 * @author Sebastien Deleuze
 */
class RowsFetchSpecExtensionsTests {

	@Test // gh-63
	fun awaitOne() {

		val spec = mockk<RowsFetchSpec<String>>()
		every { spec.one() } returns Mono.just("foo")

		runBlocking {
			assertThat(spec.awaitOne()).isEqualTo("foo")
		}

		verify {
			spec.one()
		}
	}

	@Test // gh-63
	fun awaitFirst() {

		val spec = mockk<RowsFetchSpec<String>>()
		every { spec.first() } returns Mono.just("foo")

		runBlocking {
			assertThat(spec.awaitFirst()).isEqualTo("foo")
		}

		verify {
			spec.first()
		}
	}
}
