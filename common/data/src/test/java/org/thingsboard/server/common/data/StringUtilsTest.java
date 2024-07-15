/**
 * Copyright © 2016-2024 The Winstarcloud Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.winstarcloud.server.common.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "\000", "\u0000", " \000", " \000 ", "\000 ", "\000\000", "\000 \000",
            "世\000界", "F0929906\000\000\000\000\000\000\000\000\000",
    })
    void testContains0x00_thenTrue(String sample) {
        assertThat(StringUtils.contains0x00(sample)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "abc", "世界", "\001", "\uD83D\uDC0C"})
    void testContains0x00_thenFalse(String sample) {
        assertThat(StringUtils.contains0x00(sample)).isFalse();
    }

    @Test
    void testTruncate() {
        int maxLength = 5;
        assertThat(StringUtils.truncate(null, maxLength)).isNull();
        assertThat(StringUtils.truncate("", maxLength)).isEmpty();
        assertThat(StringUtils.truncate("123", maxLength)).isEqualTo("123");
        assertThat(StringUtils.truncate("1234567", maxLength)).isEqualTo("12345...[truncated 2 symbols]");
        assertThat(StringUtils.truncate("1234567", 0)).isEqualTo("1234567");
    }

}
