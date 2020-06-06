package ru.krasmed.servicemix.rosatom.parser.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DataManager {
    private MapSqlParameterSource paramDataList;
    private NamedParameterJdbcTemplate jdbcTemplate;

    //Конструктор
    public DataManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.paramDataList = new MapSqlParameterSource();
    }
    //Сиквенсы
    private String sequenceQuery(String seq) {
        return  "select nextval('"+seq+"')";
    }

    //Базовые запросы
    private String insertFileQuery() {
        return  "INSERT INTO files (id, id_post, filename)\n" +
                "VALUES (:id, :id_post, :filename)";
    }

    private String insertPostQuery() {
        return  "INSERT INTO posts (id, name)\n" +
                "VALUES (:id, :name)";
    }

    private String insertItemsQuery() {
        return  "INSERT INTO items (id, name)\n" +
                "VALUES (:id, :name)";
    }

    private String insertInstructionQuery() {
        return  "INSERT INTO instructions (id, id_post)\n" +
                "VALUES (:id, :id_post);";
    }

    private String insertInstructionContentQuery() {
        return  "INSERT INTO instructions_content (id, id_instruction, id_item,id_section)\n" +
                "VALUES (:id,:id_instruction, :id_item,:id_section);";
    }

    public BigDecimal getNextwal(String seq) {
        String sql = sequenceQuery(seq);
        List<Integer> seqList = jdbcTemplate.query( sql, new RowMapper<Integer>() {
                    @Override
                    public Integer mapRow(ResultSet resultSet, int i) throws SQLException {
                        return resultSet.getInt("nextval");
                    }
                });
        return new BigDecimal(seqList.get(0));
    }

    public void insertFile(MapSqlParameterSource params) {
        String sql =this.insertFileQuery();
        jdbcTemplate.update(sql, params);
    }

    public void insertPost(MapSqlParameterSource params) {
        String sql =this.insertPostQuery();
        jdbcTemplate.update(sql, params);
    }

    public void insertItems(MapSqlParameterSource params) {
        String sql =this.insertItemsQuery();
        jdbcTemplate.update(sql, params);
    }

    public void insertInstruction(MapSqlParameterSource params) {
        String sql =this.insertInstructionQuery();
        jdbcTemplate.update(sql, params);
    }

    public void insertInstructionContent(MapSqlParameterSource params) {
        String sql =this.insertInstructionContentQuery();
        jdbcTemplate.update(sql, params);
    }
}
