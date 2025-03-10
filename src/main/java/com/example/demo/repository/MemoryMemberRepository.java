package com.example.demo.repository;

import com.example.demo.domain.Member;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

@Repository
public class MemoryMemberRepository implements MemberRepository {
    private final JdbcTemplate template;
    public MemoryMemberRepository(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    public Member save(Member member) {
        String sql = "INSERT INTO Member(user_name, user_age, user_gender) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, member.getName());   // 1번 ? - user_name
            ps.setInt(2, member.getAge());       // 2번 ? - user_age
            ps.setString(3, member.getGender()); // 3번 ? - user_gender
            return ps;
        }, keyHolder);

        // keyholder를 사용하면 자동 생성된 id값을 가져와서 key값을 id에 넣어줘야한다.
        long key = Objects.requireNonNull(keyHolder.getKey()).longValue();
        member.setId(key);
        return member;
    }
}
