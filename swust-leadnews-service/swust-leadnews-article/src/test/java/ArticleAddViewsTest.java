import com.swust.article.ArticleApplication;
import com.swust.common.redis.CacheService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = ArticleApplication.class)
@RunWith(SpringRunner.class)
public class ArticleAddViewsTest {
    @Autowired
    CacheService cacheService;

    @Test
    public void testAddViews() {
        String key = "rank:read";
        for (int i = 0; i < 10; i++) {
            cacheService.zIncrementScore(key, "123", 1);
        }
    }
}
