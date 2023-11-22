package de.uni_marburg.schematch.matching.sota;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.PythonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class EmbdiMatcher extends Matcher {
    Logger log = LogManager.getLogger(EmbdiMatcher.class);
    private Integer serverPort;


    @Override
    public float[][] match(TablePair tablePair) {
        log.info("Running EmbDI matcher for tables '{}' as source and '{}' as target.",
                tablePair.getSourceTable().getName(), tablePair.getTargetTable().getName());

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(String.format(
                    "http://127.0.0.1:%d/match?table1=%s&table2=%s",
                    serverPort,
                    URLEncoder.encode(tablePair.getSourceTable().pathRelativeToDataDirectory(), StandardCharsets.UTF_8),
                    URLEncoder.encode(tablePair.getTargetTable().pathRelativeToDataDirectory(), StandardCharsets.UTF_8)
            ))).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                getLogger().error("Running EmbDI matcher failed with status code {}", response.statusCode());
                return tablePair.getEmptySimMatrix();
            }
            return PythonUtils.readMatcherOutput(Arrays.stream(response.body().split("\n")).toList(), tablePair);
        } catch (Exception e) {
            getLogger().error("Running EmdDI matcher failed with exception. Is the EmbDI server running?", e);
            return tablePair.getEmptySimMatrix();
        }
    }
}
