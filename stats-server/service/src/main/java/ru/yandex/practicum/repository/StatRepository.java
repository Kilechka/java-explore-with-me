package ru.yandex.practicum.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.ViewStats;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class StatRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ViewStats> getStatistics(LocalDateTime start, LocalDateTime end, String[] uris, boolean unique) {
        String sql = "SELECT app, uri, COUNT(DISTINCT ip) AS hits FROM stats WHERE created BETWEEN ? AND ? ";
        if (!unique) {
            sql = "SELECT app, uri, COUNT(*) AS hits FROM stats WHERE created BETWEEN ? AND ? ";
        }

        if (uris != null && uris.length > 0) {
            sql += "AND uri = ANY(?) ";
        }

        sql += "GROUP BY app, uri ORDER BY hits DESC";

        String finalSql = sql;
        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(finalSql);
            ps.setObject(1, start);
            ps.setObject(2, end);
            if (uris != null && uris.length > 0) {
                Array array = con.createArrayOf("text", uris);
                ps.setArray(3, array);
            }
            return ps;
        }, (rs, rowNum) -> {
            ViewStats viewStats = new ViewStats();
            viewStats.setApp(rs.getString("app"));
            viewStats.setUri(rs.getString("uri"));
            viewStats.setHits(rs.getInt("hits"));
            return viewStats;
        });
    }
}