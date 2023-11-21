package de.uni_marburg.schematch.matching.sota;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.PythonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeapmeMatcher extends Matcher {
    private Integer serverPort;
    private String information;
    private String features;

    @Override
    public float[][] match(TablePair tablePair) {
        getLogger().debug("Running LEAPME matcher for tables '{}' as source and '{}' as target.",
                tablePair.getSourceTable().getPath(), tablePair.getTargetTable().getPath());
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(String.format(
                            "http://127.0.0.1:%d/match?prediction_used_info_types=%s&prediction_used_features=%s&table1=%s&table2=%s",
                            serverPort,
                            information,
                            features,
                            URLEncoder.encode(tablePair.getSourceTable().pathRelativeToDataDirectory(), StandardCharsets.UTF_8),
                            URLEncoder.encode(tablePair.getTargetTable().pathRelativeToDataDirectory(), StandardCharsets.UTF_8)
                    ))).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                getLogger().error("Running LEAPME matcher failed with status code {}", response.statusCode());
                return tablePair.getEmptySimMatrix();
            }

            return PythonUtils.readMatcherOutput(Arrays.stream(response.body().split("\n")).toList(), tablePair);
        } catch (Exception e) {
            getLogger().error("Running LEAPME matcher failed with exception. Is the LEAPME server running?", e);
            return tablePair.getEmptySimMatrix();
        }
    }
}
