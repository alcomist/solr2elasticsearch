package store.unitedapp.solr2elasticsearch.service;

import org.apache.http.HttpHost;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.SolrParams;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.stereotype.Service;

@Service
public class DataMigrationService {

    private final SolrClient solrClient;
    private final RestHighLevelClient elasticsearchClient;

    public DataMigrationService() {
        // Solr 클라이언트 초기화
        this.solrClient = new HttpSolrClient.Builder("http://localhost:8983/solr/testproducts").build();

        // Elasticsearch 클라이언트 초기화
        RestClientBuilder builder = RestClient.builder(new HttpHost("elasticsearch-server", 9200));
        this.elasticsearchClient = new RestHighLevelClient(builder);
    }

    public void migrateData() {
        try {
            // Solr에서 데이터 검색
            SolrParams params = null;
            QueryResponse response = solrClient.query(params);

            // Elasticsearch에 색인할 데이터를 담을 BulkRequest 생성
            BulkRequest bulkRequest = new BulkRequest();

            // 검색 결과를 반복하여 Elasticsearch에 색인
            for (SolrDocument document : response.getResults()) {
                // 데이터 변환 및 색인
                IndexRequest indexRequest = new IndexRequest("indexName");
                indexRequest.source("field1", document.getFieldValue("field1"),
                        "field2", document.getFieldValue("field2")
                        // 필요한 만큼 계속 추가
                );
                bulkRequest.add(indexRequest);
            }

            // Elasticsearch에 BulkRequest 전송
            elasticsearchClient.bulk(bulkRequest);

            // 리소스 정리
            solrClient.close();
            elasticsearchClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
