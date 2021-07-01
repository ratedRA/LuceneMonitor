package com.luceneMonitor;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.monitor.MatchingQueries;
import org.apache.lucene.monitor.Monitor;
import org.apache.lucene.monitor.MonitorConfiguration;
import org.apache.lucene.monitor.MonitorQuery;
import org.apache.lucene.monitor.MonitorQuerySerializer;
import org.apache.lucene.monitor.QueryMatch;
import org.apache.lucene.monitor.TermFilteredPresearcher;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;


public class PersistenceLuceneMonitor {
    public static void main(String[] args) {
        try {
            MonitorConfiguration monitorConfiguration = getMonitorConfiguration(getParserFunction());
            Monitor monitor = new Monitor(new StandardAnalyzer(), getTermFilteredPresearcher(), monitorConfiguration);

            /**
             * uncomment the below monitor register code if you are registering the monitorQueries for the first time.
             */

            /*monitor.register(getNewMonitorQuery("Gr_ID", "id:g1", Collections.singletonMap("customer", "124"), getParserFunction()));
            monitor.register(getNewMonitorQuery("Gr", "color:green", Collections.singletonMap("customer", "123"), getParserFunction()));
            monitor.register(getNewMonitorQuery("Bl_title", "sky", Collections.singletonMap("createdBy", "aman"), getParserFunction()));
            Map<String, String> metaData = new HashMap<>();
            metaData.put("published", "true");
            metaData.put("engineer", "aman");
            monitor.register(getNewMonitorQuery("Bl_title", "sky", metaData, getParserFunction()));*/

            Document withoutFqs = newDoc("id", "g1", "color", "green", "title", "Grass");
            MatchingQueries<QueryMatch> gm = monitor.match(withoutFqs, QueryMatch.SIMPLE_MATCHER);
            System.out.println("1st document, without filter queries (fqs), matches count: "+gm.getMatchCount());
            gm.getMatches().stream().forEach(m -> System.out.println(m.getQueryId()));

            Document withFqs = newDoc("id", "g1", "color", "green", "title", "Grass", "customer", "124");
            MatchingQueries<QueryMatch> fqMatch = monitor.match(withFqs, QueryMatch.SIMPLE_MATCHER);
            System.out.println("2nd document, with filter queries (fqs), will only fetch the query with customer equal to 123, matches count: "+fqMatch.getMatchCount());
            fqMatch.getMatches().stream().forEach(m -> System.out.println(m.getQueryId()));

            Document withMultiParamFqs = newDoc("id", "g1", "color", "green", "title", "Grass", "query", "sky", "published", "true", "engineer", "aman");
            MatchingQueries<QueryMatch> fqMatchMultiParam = monitor.match(withMultiParamFqs, QueryMatch.SIMPLE_MATCHER);
            System.out.println("3rd document, with multi filter queries (fqs), will only fetch the query with published equals to true and engineer equal to 1, matches count: "+fqMatchMultiParam.getMatchCount());
            fqMatchMultiParam.getMatches().stream().forEach(m -> System.out.println(m.getQueryId()));


            monitor.close();

        } catch (Exception ex){
            throw new RuntimeException("monitor object error");
        }

    }

    private static TermFilteredPresearcher getTermFilteredPresearcher() {
        Set<String> filterFields = new HashSet<String>();
        filterFields.add("customer");
        filterFields.add("createdBy");
        filterFields.add("published");
        filterFields.add("engineer");
        return new TermFilteredPresearcher(TermFilteredPresearcher.DEFAULT_WEIGHTOR, Collections.emptyList(), filterFields);
    }

    private static MonitorConfiguration getMonitorConfiguration(Function<String, Query> parserFunction) {
        try {
            MonitorConfiguration monitorConfiguration = new MonitorConfiguration();
            // change the path according to yourself
            Path indexPath = Paths.get("/Users/amanprasad/Documents/queryIndex/");
            monitorConfiguration.setIndexPath(indexPath, MonitorQuerySerializer.fromParser(parserFunction));
            return monitorConfiguration;
        } catch (Exception ex){
            throw new RuntimeException("error while getting the index path, update the path address according to yourseld", ex);
        }
    }


    private static MonitorQuery getNewMonitorQuery(String id, String q, Map<String, String> metadata, Function<String, Query> parserFunction) throws ParseException {
        Query query = parserFunction.apply(q);
        return new MonitorQuery(id, query, q, metadata);
    }

    private static Function<String, Query> getParserFunction(){
        return (query) -> {
            try {
                QueryParser queryParser = new QueryParser("query", new StandardAnalyzer());
                Query parsedQuery = queryParser.parse(query);
                return parsedQuery;
            } catch (Exception ex){
                throw new RuntimeException("parsing error");
            }
        };
    }

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
