package com.example.zuiliuab.controller;

import com.example.zuiliuab.service.NameSimilarityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class NameSimilarityController {
    
    private final NameSimilarityService nameSimilarityService;
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @PostMapping("/analyze")
    public String analyze(@RequestParam("name1") String name1, 
                         @RequestParam("name2") String name2,
                         HttpServletRequest request,
                         Model model) {
        try {
            NameSimilarityService.SimilarityResult result = nameSimilarityService.analyzeSimilarity(name1, name2, request);
            model.addAttribute("name1", name1);
            model.addAttribute("name2", name2);
            model.addAttribute("similarity", result.getSimilarity());
            model.addAttribute("evaluation", result.getEvaluation());
            model.addAttribute("timestamp", result.getTimestamp());
        } catch (Exception e) {
            model.addAttribute("error", "分析过程中出现错误: " + e.getMessage());
        }
        return "result";
    }
}