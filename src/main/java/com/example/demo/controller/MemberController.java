package com.example.demo.controller;

import com.example.demo.domain.Member;
import com.example.demo.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MemberController {

    private MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("hello")
    public String hello(@RequestParam String name, @RequestParam int age, @RequestParam String gender) {
        Member member = new Member();
        member.setName(name);
        member.setAge(age);
        member.setGender(gender);
        memberService.join(member);
        return "create user" + member;
    }
}
