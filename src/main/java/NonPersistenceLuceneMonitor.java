import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.monitor.MatchingQueries;
import org.apache.lucene.monitor.Monitor;
import org.apache.lucene.monitor.MonitorConfiguration;
import org.apache.lucene.monitor.MonitorQuery;
import org.apache.lucene.monitor.QueryMatch;
import org.apache.lucene.monitor.TermFilteredPresearcher;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.util.Collections;
import java.util.Map;

public class NonPersistenceLuceneMonitor {
    public static void main(String[] args) {
        try {
            MonitorConfiguration monitorConfiguration = new MonitorConfiguration();
            Monitor monitor = new Monitor(new StandardAnalyzer(), new TermFilteredPresearcher(), monitorConfiguration);
            monitor.register(getNewMonitorQuery("Gr", "color:green", Collections.singletonMap("customer", "123")));
            monitor.register(getNewMonitorQuery("Gr_ID", "id:g1", Collections.singletonMap("customer", "124")));
            monitor.register(getNewMonitorQuery("Bl", "color:blue^2.0", Collections.singletonMap("customer", "123")));
            monitor.register(getNewMonitorQuery("Bl_title", "sky", Collections.singletonMap("customer", "124")));

            Document greenDoc = newDoc("id", "g1", "color", "green", "title", "Grass");
            MatchingQueries<QueryMatch> gm = monitor.match(greenDoc, QueryMatch.SIMPLE_MATCHER);
            System.out.println("matches count: " + gm.getMatchCount());
            gm.getMatches().stream().forEach(m -> System.out.println(m.getQueryId()));

            monitor.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static MonitorQuery getNewMonitorQuery(String id, String queryString, Map<String, String> metadata) throws ParseException {
        QueryParser qp = new QueryParser("title", new StandardAnalyzer());
        Query q = qp.parse(queryString);

        return new MonitorQuery(id, q, queryString, metadata);
    }

    /*private static MonitorQuery getMonitorQueryForSolrQuery(){
        try {
            ExtendedDismaxQParser extendedDismaxQParser = new ExtendedDismaxQParser(null, null, null, null);
            Query q = extendedDismaxQParser.parse();

            return new MonitorQuery("id", q);
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }*/

    private static Document newDoc(String... kvPairs) {
        Document doc = new Document();
        boolean isKey = true;
        String key = null;
        for (String kv : kvPairs) {
            if (isKey) {
                key = kv;
                isKey = false;
            } else {
                doc.add(new TextField(key, kv, Field.Store.YES));
                isKey = true;
            }
        }
        return doc;
    }
}
