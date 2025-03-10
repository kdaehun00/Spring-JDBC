# 구현순서

## Controller
```
package com.example.demo.controller;

import com.example.demo.domain.Member;
import com.example.demo.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MemberController {

    private MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("hello")
    public String hello(@RequestParam String name, @RequestParam int age, @RequestParam String gender) {
        Member member = new Member();
        member.setName(name);
        member.setAge(age);
        member.setGender(gender);
        memberService.join(member);
        return "create user" + member;
    }
}

```
처음에는 @Controller와 @ResponseBody 넣고 return 값을 받았다.
> ❓🤔 생각해보기
(@Controller + @ResponseBody VS @RestController)
@RestController로 바꾸어서 했는데, 문득 두 방식의 차이점에 대해 궁금해 생각해보았다. 기능은 같지만, @Controller로 하면 @ResponseBody를 선언한 메서드만 JSON 형식으로 주고 받고 나머지 메서드는 ViewResolver를 사용한다. 반면 @RestController를 사용하면 모든 메서드가 JSON 형식으로 주고 받는다. API 개발을 할 때는 ViewResolver를 사용할 일이 없으니 @RestController를 사용하는 것이 좋을 것 같다고 생각했는데 실제로 그럴지는 잘 모르겠다.

그 다음 Service를 만들었는데, Controller에서 Service를 사용하므로 미리 의존성을 주입해주었다.

## Service
```
package com.example.demo.service;

import com.example.demo.domain.Member;
import com.example.demo.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    String message;
    public Long join(Member member) {
        isDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    private void isDuplicateMember(Member member) {
        //중복 검사할 게 있다면 작성
    }
}

```
Service는 MemberRepository를 사용하므로 객체를 생성하지 않고 의존성 주입을 해주었다.

## Repository (JDBC)

```
package com.example.demo.repository;

import com.example.demo.domain.Member;


public interface MemberRepository {
    Member save(Member member);
}

```
일반 Memory나 JPA 등으로 변경할 때 쉽게 갈아끼울 수 있도록 인터페이스 먼저 만들어주고, Class가 상속받도록 하였다.

```
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

```

JDBC를 사용하였는데, 코딩하다가 오류 및 고민이 생겼다.

이렇게 Request로는 이름, 나이, 성별을 보냈었는데
![](https://velog.velcdn.com/images/kdaehun00/post/0264a0b7-ece6-4c0e-815a-d3bb71049fef/image.png)

실제 Insert문에서는 user_id까지 넣으니까 파라미터 갯수가 부족하며, 마지막 컬럼의 Default값을 설정해야한다는 에러가 떴다. user_id는 내가 넘겨주는 것이 아닌 자동으로 올라가서 DB에 들어가게 하고 싶어서 찾아보았다.

![](https://velog.velcdn.com/images/kdaehun00/post/60540304-c98d-4981-b4d1-ba05c86ce904/image.png)

그래서 찾아보니 table 세팅 시 AUTO_INCREMENT를 추가하면 되었다. 초반에 테이블을 이렇게 세팅했었는데, 여기 PRIMARY KEY 값에 자동으로 증가하도록 해주면 id값을 따로 관리하지 않아도 저절로 올라가는 것이다.
```
create table Member (
    user_id     BIGINT          PRIMARY KEY ,
    user_name   VARCHAR(255)    NOT NULL ,
    user_age    INTEGER         NOT NULL ,
    user_gender CHAR(3)         NOT NULL
)
```

```
ALTER TABLE Member MODIFY COLUMN user_id BIGINT AUTO_INCREMENT;

```
이걸로 컬럼을 변경해주고 값을 넘겨보니
![](https://velog.velcdn.com/images/kdaehun00/post/c230c60d-3198-4f4e-8e4f-092e7cf2f8bb/image.png)

테이블에 잘 들어온 것을 확인할 수 있었다.
![](https://velog.velcdn.com/images/kdaehun00/post/fb0dfdd6-b8d1-45d9-aa55-9ea176021f0c/image.png)

또, SetId를 통해 직접 id를 넣어주어야 했었는데 이 부분을 몰랐다. 그냥 keyholder가 자동으로 id값을 늘려주고 DB에 넣어주는 게 아닌가라고 생각했는데, 그게 아니라 DB에서 자동으로 숫자가 올라가니 그 값을 가져와서 member 객체에 넣어주어야 return 했을 때 null이 아닌 member 객체가 반환될 수 있는 것이었다. 이걸 안 해주니까 return 값이 null로 나와서 당황했다..

이런 두 가지 문제를 왜 인지 못했었는지 생각해보니
첫 번째 문제는 JPA에서는 이미 save가 정의되어있어 직접 sql쿼리를 짜지 않았고 자동 AUTO_INCREAMENT값을 인지하고 저장해주었기 때문에 이걸 빠뜨리고 테이블을 세팅했음을 인지하지 못했다. 직접 쿼리를 짜보고 들어가는 방식을 아니 JPA의 원리?나 편리함을 느끼게 되었다.
두 번째 문제는 JPA에서 @GeneratedValue와 @Id로 위의 작업을 다 해주니 저 과정을 잘 몰랐다.
실제로 해보니까 왜 저런 코드를 짜야하고 각 어노테이션들이 어떤 역할을 해주는 것인지 이해가 돼서 개념이 채워진 느낌이었다.
