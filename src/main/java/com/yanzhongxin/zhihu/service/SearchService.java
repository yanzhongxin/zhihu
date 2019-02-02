package com.yanzhongxin.zhihu.service;

import com.yanzhongxin.zhihu.model.Question;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Service;

import javax.management.Query;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

/**
 * @author www.yanzhongxin.com
 * @date 2019/2/2 16:50
 */
@Service
public class SearchService {
    private static final String QUESTION_TITLE_FIELD = "question_title";
    private static final String QUESTION_CONTENT_FIELD = "question_content";

    public static final String solr_server_url="http://192.168.25.128:8080/solr";

    //1 创建一个solrServer对象
    private static SolrServer solrServer=new HttpSolrServer(solr_server_url);
    public  List<Question> searchQuestion(String keyword,int offset,int count,
                                         String h1Pre,String hlPos){
        List<Question> questionList=new ArrayList<>();
        // 2 设置要给SolrQuery对象,传递关键字
        SolrQuery solrQuery=new SolrQuery(keyword);
        //设置起始位置，以及查询个数
        solrQuery.setRows(count);
        solrQuery.setStart(offset);
        //设置高亮显示
        solrQuery.setHighlight(true);
        solrQuery.setHighlightSimplePre(h1Pre);
        solrQuery.setHighlightSimplePost(hlPos);
        //指定默认搜索域
        solrQuery.set("df", "item_keywords");

        //设置查询的域
        solrQuery.set("hl.fl", QUESTION_TITLE_FIELD + "," + QUESTION_CONTENT_FIELD);
        
        //执行查询
        try {
            QueryResponse query = solrServer.query(solrQuery);
            for (Map.Entry<String, Map<String, List<String>>> entry : query.getHighlighting().entrySet()) {
                Question q = new Question();
                q.setId(Integer.parseInt(entry.getKey())); //获得每个document对象的id
                if (entry.getValue().containsKey(QUESTION_CONTENT_FIELD)) {
                    List<String> contentList = entry.getValue().get(QUESTION_CONTENT_FIELD);//获得高亮后的字符串content
                    if (contentList.size() > 0) {
                        q.setContent(contentList.get(0));
                    }
                }
                if (entry.getValue().containsKey(QUESTION_TITLE_FIELD)) {
                    List<String> titleList = entry.getValue().get(QUESTION_TITLE_FIELD);//获得高亮后的字符串title
                    if (titleList.size() > 0) {
                        q.setTitle(titleList.get(0));
                    }
                }
                questionList.add(q);
            }
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("搜索失败");
        }
        return questionList;

    }

    public   void addQuestionToIndexDB(int qid,String title,String content)  {
        // 1、把solrJ的jar包添加到工程。
        // 2、创建一个SolrServer对象。创建一个和sorl服务的连接。HttpSolrServer。

        // 3、创建一个文档对象。SolrInputDocument。
        SolrInputDocument document = new SolrInputDocument();
        // 4、向文档对象中添加域。必须有一个id域。而且文档中使用的域必须在schema.xml中定义。
        document.addField("id", qid);
        document.addField("question_id",qid);//把问题id,问题标题，问题内容加入到索引库
        document.addField("question_title", title);
        document.addField("question_content", content);
        // 5、把文档添加到索引库
        try {
            solrServer.add(document);
            // 6、Commit。
            solrServer.commit();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void deleteMessage(String did){
        try {
            solrServer.deleteById(did);

        }catch (Exception e){

        }
           }

}
