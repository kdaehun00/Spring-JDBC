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
