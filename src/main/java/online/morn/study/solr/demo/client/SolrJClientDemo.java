package online.morn.study.solr.demo.client;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SolrJClientDemo {

	// baseSolrUrl 示例
	private static String baseSolrUrl = "http://localhost:8983/solr/";
	private static String baseSolrUrlWithCollection = "http://localhost:8983/solr/techproducts";

	/**
	 * HttpSolrClient:与一个solr Server 通过http进行通信
	 */
	public static SolrClient getHttpSolrClient(String baseSolrUrl) {
		return new HttpSolrClient.Builder(baseSolrUrl)
				.withConnectionTimeout(1000).withSocketTimeout(6000).build();
	}

	public static SolrClient getHttpSolrClient() {
		return new HttpSolrClient.Builder(baseSolrUrl)
				.withConnectionTimeout(1000).withSocketTimeout(6000).build();
	}

	/**
	 * LBHttpSolrClient: 负载均衡的httpSolrClient <br>
	 * 负载均衡方式： 轮询给定的多个solr server url。
	 * 当某个url不通时，url地址会从活跃列表移到死亡列表中，用下一个地址再次发送请求。<br>
	 * 对于死亡列表中的url地址，会定期（默认每隔1分钟，可设置）去检测是否变活了，再加入到活跃列表中。 <br>
	 * 注意： <br>
	 * 1、不可用于主从结构master/slave 的索引场景，因为主从结构必须通过主节点来更新。 <br>
	 * 2、对于SolrCloud(leader/replica)，使用CloudSolrClient更好。
	 * 在solrCloud中可用它来进行索引更新，solrCloud中的节点会将请求转发到对应的leader。
	 */
	public static SolrClient getLBHttpSolrClient(String... solrUrls) {
		return new LBHttpSolrClient.Builder().withBaseSolrUrls(solrUrls)
				.build();
	}

	private static String baseSolrUrl2 = "http://localhost:7001/solr/";

	public static SolrClient getLBHttpSolrClient() {
		return new LBHttpSolrClient.Builder()
				.withBaseSolrUrls(baseSolrUrl, baseSolrUrl2).build();
	}

	/**
	 * 访问SolrCloud集群用CloudSolrClient<br>
	 * CloudSolrClient 实例通过访问zookeeper得到集群中集合的节点列表，<br>
	 * 然后通过LBHttpSolrClient来负载均衡地发送请求。<br>
	 * 注意：这个类默认文档的唯一键字段为“id”，如果不是的，通过 setIdField(String)方法指定。
	 */
	public static SolrClient getCloudSolrClient(List<String> zkHosts,
			Optional<String> zkChroot) {
		return new CloudSolrClient.Builder(zkHosts, zkChroot).build();
	}

	private static String zkServerUrl = "localhost:9983";

	public static SolrClient getCloudSolrClient() {
		List<String> zkHosts = new ArrayList<String>();
		zkHosts.add(zkServerUrl);
		Optional<String> zkChroot = Optional.empty();
		return new CloudSolrClient.Builder(zkHosts, zkChroot).build();
	}

	public static void main(String[] args) throws Exception {

		// HttpSolrClient 示例：
		SolrClient client = SolrJClientDemo.getHttpSolrClient();

		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", UUID.randomUUID().toString());
		doc.addField("name", "HttpSolrClient");

		UpdateResponse updateResponse = client.add("techproducts", doc);
		// 记得要提交
		client.commit("techproducts");

		System.out.println("------------ HttpSolrClient ------------");
		System.out.println("add doc:" + doc);
		System.out.println("response: " + updateResponse.getResponse());

		client.close();

		// LBHttpSolrClient 示例
		client = SolrJClientDemo.getLBHttpSolrClient();
		doc.clear();
		doc.addField("id", UUID.randomUUID().toString());
		doc.addField("name", "LBHttpSolrClient");

		updateResponse = client.add("techproducts", doc);
		// 记得要提交
		client.commit("techproducts");
		System.out.println("------------ LBHttpSolrClient ------------");
		System.out.println("add doc:" + doc);
		System.out.println("response: " + updateResponse.getResponse());

		client.close();

		// CloudSolrClient 示例
		client = SolrJClientDemo.getCloudSolrClient();
		doc.clear();
		doc.addField("id", UUID.randomUUID().toString());
		doc.addField("name", "CloudSolrClient");

		updateResponse = client.add("techproducts", doc);
		// 记得要提交
		client.commit("techproducts");
		System.out.println("------------ CloudSolrClient ------------");
		System.out.println("add doc:" + doc);
		System.out.println("response: " + updateResponse.getResponse());

		client.close();
	}

}
