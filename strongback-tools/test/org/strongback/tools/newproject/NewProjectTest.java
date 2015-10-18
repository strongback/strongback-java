/*
 * Strongback
 * Copyright 2015, Strongback and individual contributors by the @authors tag.
 * See the COPYRIGHT.txt in the distribution for a full listing of individual
 * contributors.
 *
 * Licensed under the MIT License; you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://opensource.org/licenses/MIT
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.strongback.tools.newproject;

import static org.fest.assertions.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.Test;

/**
 * @author Randall Hauch
 *
 */
public class NewProjectTest {

    @Test
    public void shouldEscapeUserlibTemplateCorrectly() throws IOException {
        boolean print = false;
        File template = new File("../templates/Strongback.userlibraries.template");
        if (!template.exists()) {
            template = new File("templates/Strongback.userlibraries.template");
        }
        assertThat(template.exists()).isTrue();

        List<String> lines = Files.readAllLines(template.toPath(), StandardCharsets.UTF_8);
        List<String> updated = lines.stream().collect(Collectors.toList());
        if (print) updated.forEach(System.out::println);
        String content = NewProject.combineAndEscape(updated,"/Users/jsmith/strongback");
        if (print) System.out.println(content);
        assertThat(content.indexOf("STRONGBACK")).isEqualTo(-1);
        // assertThat(content.indexOf(" ")).isEqualTo(-1);

        Properties props = new Properties();
        props.setProperty("template", content);
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        props.store(ostream, "");
        String propFileContents = new String(ostream.toByteArray());
        assertThat(propFileContents.contains(">\\n\\t\\t<attributes")).isTrue();
        assertThat(propFileContents.contains("\"file\\:/Users/jsmith")).isTrue();
    }

}
