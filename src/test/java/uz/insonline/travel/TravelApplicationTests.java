package uz.insonline.travel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

@SpringBootTest
class TravelApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void inspectColumns() {
        try {
            System.out.println("Querying all columns of INS_KURS table...");
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT OWNER, COLUMN_NAME, DATA_TYPE " +
                "FROM ALL_TAB_COLUMNS " +
                "WHERE TABLE_NAME = 'INS_KURS' " +
                "ORDER BY OWNER, COLUMN_ID"
            );
            for (Map<String, Object> col : columns) {
                System.out.println("Owner: " + col.get("OWNER") + 
                                   ", Column: " + col.get("COLUMN_NAME") + 
                                   ", Type: " + col.get("DATA_TYPE"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
