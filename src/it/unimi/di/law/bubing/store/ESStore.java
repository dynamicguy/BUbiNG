package it.unimi.di.law.bubing.store;

import it.unimi.di.law.bubing.RuntimeConfiguration;
import it.unimi.di.law.warc.io.ParallelBufferedWarcWriter;
import it.unimi.di.law.warc.records.HttpResponseWarcRecord;
import it.unimi.di.law.warc.records.WarcHeader;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import net.dankito.readability4j.Article;
import net.dankito.readability4j.Readability4J;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.message.HeaderGroup;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ESStore implements Closeable, Store {
    private final static Logger LOGGER = LoggerFactory.getLogger(ESStore.class);

    public final int OUTPUT_STREAM_BUFFER_SIZE = 1024 * 1024;
    public final static String STORE_NAME = "store.warc.gz";
    public final static String DIGESTS_NAME = "digests.bloom";
    public final static int NUM_GZ_WARC_RECORDS = 16;

    private final FastBufferedOutputStream outputStream;

    private final ParallelBufferedWarcWriter writer;

    private final RestHighLevelClient esClient;

    private static final String esDocType = "_doc";

    private static final String esIndexName = "crawler";

    public ESStore(final RuntimeConfiguration rc) throws IOException {
        final File file = new File(rc.storeDir, STORE_NAME);

        if (rc.crawlIsNew) {
            if (file.exists() && file.length() != 0)
                throw new IOException("Store exists and it is not empty, but the crawl is new; it will not be overwritten: " + file);
            outputStream = new FastBufferedOutputStream(new FileOutputStream(file), OUTPUT_STREAM_BUFFER_SIZE);
        } else {
            if (!file.exists())
                throw new IOException("Store does not exist, but the crawl is not new; it will not be created: " + file);
            outputStream = new FastBufferedOutputStream(new FileOutputStream(file, true), OUTPUT_STREAM_BUFFER_SIZE);
        }
        writer = new ParallelBufferedWarcWriter(outputStream, true);

        // TODO: move this in StartupConfiguration
        esClient = ESHighLevelClient.getClient(rc.storeDir.getAbsolutePath().concat("/../../certs/bubing.jks"), "admin",
                "admin", "localhost");
    }

    @Override
    public void store(final URI uri, final HttpResponse response, final boolean isDuplicate, final byte[] contentDigest, final String guessedCharset) throws IOException, InterruptedException {
        if (contentDigest == null) throw new NullPointerException("Content digest is null");

        LOGGER.info("StatusLine was: " + response.getStatusLine().toString());
        final HttpResponseWarcRecord record = new HttpResponseWarcRecord(uri, response);
        HeaderGroup warcHeaders = record.getWarcHeaders();
        warcHeaders.updateHeader(new WarcHeader(WarcHeader.Name.WARC_PAYLOAD_DIGEST, "bubing:" + Hex.encodeHexString(contentDigest)));
        if (guessedCharset != null)
            warcHeaders.updateHeader(new WarcHeader(WarcHeader.Name.BUBING_GUESSED_CHARSET, guessedCharset));
        if (isDuplicate) warcHeaders.updateHeader(new WarcHeader(WarcHeader.Name.BUBING_IS_DUPLICATE, "true"));

        try {
            String html = EntityUtils.toString(response.getEntity(), guessedCharset);
            Readability4J readability4J = new Readability4J(record.getWarcTargetURI().toString(), html);
            Article article = readability4J.parse();
            String extractedContentHtml = article.getContent();
            String extractedContentHtmlWithUtf8Encoding = article.getContentWithUtf8Encoding();
            String extractedContentPlainText = article.getTextContent();
            String title = article.getTitle();
            String byline = article.getByline();
            String excerpt = article.getExcerpt();

            LOGGER.info("parsed html content was: " + extractedContentHtml);
            LOGGER.info("title was: " + title);
            LOGGER.info("extractedContentPlainText was: " + extractedContentPlainText);
            LOGGER.info("byline was: " + byline);
            LOGGER.info("excerpt was: " + excerpt);

            Map<String, Object> document = new HashMap<>();
            document.put("id", record.getWarcTargetURI().toString());
            document.put("title", title);
            document.put("name", byline);
            document.put("excerpt", excerpt);
            document.put("text", extractedContentPlainText);
            document.put("html", extractedContentHtml);
            document.put("html_with_utf8", extractedContentHtmlWithUtf8Encoding);
            document.put("digest", Hex.encodeHexString(contentDigest));
            document.put("warc_id", record.getWarcRecordId().toString());
            document.put("date", record.getWarcDate());
            document.put("status", response.getStatusLine().toString());

            IndexRequest indexRequest = new IndexRequest(esIndexName, esDocType, record.getWarcTargetURI().toString()).source(document);
            IndexResponse indexResponse = esClient.index(indexRequest, RequestOptions.DEFAULT);
            LOGGER.info("ES index response  was: " + indexResponse.toString());

        } catch (Exception ex) {
            LOGGER.info("Error occurred during ES indexing: " + ex.getMessage());
        }

        writer.write(record);
    }

    @Override
    public synchronized void close() throws IOException {
        try {
            writer.close();
        } catch (IOException shouldntHappen) {
            LOGGER.error("Interrupted while closing parallel output stream");
        }
        outputStream.close();
    }
}
