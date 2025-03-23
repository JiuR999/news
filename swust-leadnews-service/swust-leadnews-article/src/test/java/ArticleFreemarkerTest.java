
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.swust.article.ArticleApplication;
import com.swust.article.mapper.ApArticleContentMapper;
import com.swust.article.mapper.ApArticleMapper;
import com.swust.common.constants.KafkaMessageConstants;
import com.swust.model.article.pojos.ApArticle;
import com.swust.model.article.pojos.ApArticleContent;
import com.swust.service.FileStorageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = ArticleApplication.class)
@RunWith(SpringRunner.class)
public class ArticleFreemarkerTest {

    @Autowired
    private Configuration configuration;

    @Autowired
    private FileStorageService fileStorageService;


    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Test
    public void testSendMsg() {
        kafkaTemplate.send(KafkaMessageConstants.WM_NEWS_PUBLISH_TOPIC,"520");
    }

    @Test
    public void createStaticUrlTest() throws Exception {
        //1.获取文章内容
        ApArticleContent apArticleContent = apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, 1302862387124125698L));
        if (apArticleContent != null && StringUtils.isNotBlank(apArticleContent.getContent())) {
            //2.文章内容通过freemarker生成html文件
            StringWriter out = new StringWriter();
            Template template = configuration.getTemplate("article.ftl");

            Map<String, Object> params = new HashMap<>();
            params.put("content", JSONArray.parseArray(apArticleContent.getContent()));

            template.process(params, out);
            InputStream is = new ByteArrayInputStream(out.toString().getBytes());

            //3.把html文件上传到minio中
            String path = fileStorageService.uploadHtmlFile("", apArticleContent.getArticleId() + ".html", is);

            //4.修改ap_article表，保存static_url字段
            ApArticle article = new ApArticle();
            article.setId(String.valueOf(apArticleContent.getArticleId()));
            article.setStaticUrl(path);
            apArticleMapper.updateById(article);

        }
    }

    @Test
    public void convert() {
        List<ApArticleContent> content = apArticleContentMapper.selectList(new LambdaQueryWrapper<ApArticleContent>().lt(ApArticleContent::getId, 1383828014650150914L));
        System.out.println(content.size());
        for (ApArticleContent articleContent : content) {
            JSONArray array = JSON.parseArray(articleContent.getContent());
            StringBuilder builder = new StringBuilder();
            String pTmplate = "<p>?</p>";

            for (Object o : array) {
                int cursor = 0;
                JSONObject jsonObject = JSON.parseObject(o.toString());
                String type = (String) jsonObject.get("type");
                String value = (String) jsonObject.get("value");
                if (type.equals("text")) {
                    for (int i = 0; i < value.length(); i++) {
                        if (value.charAt(i) == '\n' || i == value.length() - 1) {
                            builder.append(pTmplate.replaceAll("\\?", value.substring(cursor, i + 1)));
                            cursor = i + 1;
                        }
                    }
                }
                if (type.equals("image")) {

                    builder.append("<img src=\"" + value + "\">");
                    builder.append("<br>");

                }
            }
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setId(articleContent.getId());
            apArticleContent.setContent(builder.toString());
            apArticleContentMapper.updateById(apArticleContent);
//            System.out.println(builder);
        }
    }

    @Test
    //下载文章内容
    public void testDownArticle() throws IOException {
        ApArticleContent content = apArticleContentMapper.selectById(1383828014650150929L);
        System.out.println(content.getContent());
        FileOutputStream fout = new FileOutputStream("D:\\test.html");
        byte[] bytes = content.getContent().getBytes();
        int cur = 0;
        int len = 1000;
        while (cur < bytes.length) {
            System.out.println((cur * 100 / bytes.length + 1) + "%");
            if (cur + len < bytes.length) {
                fout.write(bytes, cur, len);
            } else {
                fout.write(bytes, cur, bytes.length - cur);
            }
            cur += len;
        }

        fout.close();
    }
}