package cn.ponfee.scheduler.db;

import cn.ponfee.scheduler.common.util.Files;
import cn.ponfee.scheduler.common.util.Jsons;
import cn.ponfee.scheduler.common.util.MavenProjects;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.PathUtils;
import org.h2.server.web.DbStarter;
import org.h2.tools.RunScript;
import org.junit.Assert;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * H2 database server
 *
 * @author Ponfee
 */
public class EmbeddedH2DatabaseServer {

    public static void main(String[] args) throws Exception {
        String jdbcUrl = buildJdbcUrl("test");

        System.out.println("Embedded h2 database starting...");
        //new JakartaDbStarter(); // error
        new DbStarter();
        //new WebServer().start();
        //new TcpServer().start();
        //new PgServer().start();
        System.out.println("Embedded h2 database started!");

        JdbcTemplate jdbcTemplate = DBTools.createJdbcTemplate(jdbcUrl, "sa", "");

        System.out.println("\n\n--------------------------------------------------------testDatabase");
        DBTools.testNativeConnection("org.h2.Driver", jdbcUrl, "sa", "");

        System.out.println("\n\n--------------------------------------------------------testJdbcTemplate");
        DBTools.testJdbcTemplate(jdbcTemplate);

        System.out.println("\n\n--------------------------------------------------------testScript");
        testScript(jdbcTemplate);
    }

    private static String buildJdbcUrl(String dbName) throws IOException {
        String dataDir = MavenProjects.getProjectBaseDir() + "/target/h2/";

        File file = new File(dataDir);
        if (file.exists()) {
            PathUtils.deleteDirectory(file.toPath());
        }
        Files.mkdir(file);
        return "jdbc:h2:" + dataDir + dbName;
    }

    private static void testScript(JdbcTemplate jdbcTemplate) throws Exception {
        String scriptPath = MavenProjects.getProjectBaseDir() + "/src/test/DB/H2/H2_SCRIPT.sql";

        // 加载脚本方式一：
        //jdbcTemplate.execute("RUNSCRIPT FROM '" + scriptPath + "'");

        // 加载脚本方式二：
        //jdbcTemplate.execute(IOUtils.toString(new FileInputStream(scriptPath), StandardCharsets.UTF_8));

        // 加载脚本方式二：
        jdbcTemplate.execute((ConnectionCallback<Void>) conn -> {
            try {
                String script = IOUtils.toString(new FileInputStream(scriptPath), StandardCharsets.UTF_8);
                RunScript.execute(conn, new StringReader(script));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });

        List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT * FROM test1");
        Assert.assertEquals("fdsaf23r23", result.get(0).get("NAME"));
        System.out.println("Query result: " + Jsons.toJson(result));

        new CountDownLatch(1).await();
    }

}
