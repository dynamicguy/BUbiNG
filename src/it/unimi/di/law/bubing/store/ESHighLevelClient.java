package it.unimi.di.law.bubing.store;


import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ESHighLevelClient {
    private static final String type = "_doc";

    public static void main(String[] args) throws IOException {

        RestHighLevelClient client = getClient("/Users/ferdous/projects/dynamicguy/BUbiNG/certs/bubing.jks", "admin", "admin", "localhost");

        String index = "bubing";

        // Create a document that simulates a simple log line from a web server
        Map<String, Object> document = new HashMap<>();
        List<Double> myVector = new ArrayList<>();
        myVector.add(1.5);
        myVector.add(2.5);

        document.put("id", "5");
        document.put("name", "Nurul Ferdous");
        document.put("price", 19);
        document.put("my_vector", myVector);
        document.put("timestamp", "10/Nov/2019:14:56:14 -0700");

        System.out.println("Demoing a single index request:");
        String id = "1";
        IndexRequest indexRequest = new IndexRequest(index, type, id).source(document);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(indexResponse.toString());
        client.close();
    }

    private static RestHighLevelClient getClient(String keystorePath, String username, String password, String hostname) {
        System.setProperty("javax.net.ssl.trustStore", keystorePath);

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(hostname, 9200, "https"))
                .setHttpClientConfigCallback(new HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });

        return new RestHighLevelClient(builder);
    }
}
