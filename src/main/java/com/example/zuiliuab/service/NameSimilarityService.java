package com.example.zuiliuab.service;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;

@Service
@Slf4j
public class NameSimilarityService {

    @Value("${zhipu.api.key}")
    private String apiKey;

    public SimilarityResult analyzeSimilarity(String name1, String name2, HttpServletRequest request) {
        // 获取用户真实IP地址
        String userIp = getClientIpAddress(request);
        
        // 初始化智普AI客户端
        ZhipuAiClient client = ZhipuAiClient.builder()
                .apiKey(apiKey)
                .build();

        // 🔥 精心构造的 Prompt：真实 + 幽默 + JSON 格式
        String prompt = String.format(
                """
                        你是一位融合语言学、汉字文化和冷面笑匠气质的名字分析师。请分析以下两个名字之间的亲密度，要求：
                        - 基于音律、字形、字义、常见搭配等维度做出真实评估，不强行浪漫
                        - 相似度分数在0-100之间，合理分布（可低可高）
                        - 评价要简洁（30字内），允许带一点温和幽默或人间清醒式吐槽（如‘像跨服聊天’‘建议合拍短视频’），但不人身攻击
                        - 以严格的JSON格式返回，仅包含 similarity 和 evaluation 两个字段
                        
                        名字1：%s
                        名字2：%s
                        
                        示例输出：
                        {"similarity": 78, "evaluation": "音调和谐，建议合开正能量有限公司"}
                        
                        现在请分析这对名字，输出JSON：""",
                name1, name2
        );

        // 创建聊天请求
        ChatCompletionCreateParams requestParams = ChatCompletionCreateParams.builder()
                .model("glm-4.5-flash") // 使用GLM-4.5-Flash免费模型
                .messages(Arrays.asList(
                        ChatMessage.builder()
                                .role(ChatMessageRole.SYSTEM.value())
                                .content("你是一个专业的名字关系分析师。根据用户提供的两个名字，分析它们之间的缘分和相似程度，给出一个0-100之间的相似度分数和一段简短的评价。请以严格的JSON格式返回结果，只包含similarity和evaluation两个字段，不要使用任何Markdown标记或额外的说明文字。例如：{\"similarity\": 80, \"evaluation\": \"这两个名字很有缘分\"}")
                                .build(),
                        ChatMessage.builder()
                                .role(ChatMessageRole.USER.value())
                                .content(prompt) // 使用我们升级版的 prompt
                                .build()
                ))
                .build();

        try {
            // 发送请求
            ChatCompletionResponse response = client.chat().createChatCompletion(requestParams);

            if (response.isSuccess()) {
                String content = response.getData().getChoices().getFirst().getMessage().getContent().toString();
                //日志
                String currentTime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                log.info("分析时间: {}, 登录ip地址: {}, 用户名字1: {}, 用户名字2: {}, AI回复: {}", currentTime, userIp, name1, name2, content);

                // 解析JSON响应
                return parseResult(content);
            } else {
                log.error("API调用失败: {}, 错误码: {}", response.getMsg(), response.getCode());
                throw new RuntimeException("API调用失败: " + response.getMsg());
            }
        } catch (ai.z.openapi.service.model.ZAiHttpException e) {
            log.error("API调用异常: {}", e.getMessage());
            if (e.getMessage().contains("Insufficient balance")) {
                throw new RuntimeException("余额不足，请充值后再试");
            } else if (e.getMessage().contains("429")) {
                throw new RuntimeException("请求过于频繁，请稍后再试");
            }
            throw new RuntimeException("API调用异常: " + e.getMessage());
        } catch (Exception e) {
            log.error("其他异常: {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("分析过程中出现错误: " + e.getMessage());
        }
    }

    /**
     * 获取客户端真实IP地址
     * 按照以下顺序获取IP地址:
     * 1. X-Forwarded-For
     * 2. X-Real-IP
     * 3. request.getRemoteAddr()
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // X-Forwarded-For可能包含多个IP地址，第一个是客户端真实IP
            int index = xForwardedFor.indexOf(',');
            if (index != -1) {
                return xForwardedFor.substring(0, index);
            } else {
                return xForwardedFor;
            }
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 手动解析AI返回的JSON字符串（简单实现，生产环境建议用Jackson）
     */
    private SimilarityResult parseResult(String content) {
        try {
            // 处理可能的代码块包裹
            String jsonContent = content.trim();
            if (jsonContent.startsWith("```json")) {
                jsonContent = jsonContent.substring(7);
            }
            if (jsonContent.endsWith("```")) {
                jsonContent = jsonContent.substring(0, jsonContent.length() - 3);
            }
            jsonContent = jsonContent.trim();

            // 提取 similarity
            int similarityStart = findKey(jsonContent, "similarity");
            int similarity = getSimilarity(similarityStart, jsonContent);

            // 提取 evaluation
            int evalStart = findKey(jsonContent, "evaluation");
            String evaluation = getString(evalStart, jsonContent);

            return new SimilarityResult(similarity, evaluation);
        } catch (Exception e) {
            log.warn("解析AI响应失败，使用随机值替代: {}", content, e);
            int randomSimilarity = 60 + (int)(Math.random() * 40);
            String[] fallbacks = {
                    "名字读起来挺顺，至少不会叫错。",
                    "属于能一起点奶茶的关系，但未必能一起还房贷。",
                    "字义不冲突，算是安全牌组合。",
                    "建议先从微信聊天开始测试兼容性。",
                    "虽然名字不搭，但心动往往不讲道理。"
            };
            String randomEvaluation = fallbacks[(int)(Math.random() * fallbacks.length)];
            return new SimilarityResult(randomSimilarity, randomEvaluation);
        }
    }

    private static String getString(int evalStart, String jsonContent) {
        if (evalStart == -1) throw new IllegalArgumentException("Missing 'evaluation' field");
        int evalColon = jsonContent.indexOf(':', evalStart);
        int evalEnd = jsonContent.lastIndexOf('}');
        String evaluation = jsonContent.substring(evalColon + 1, evalEnd).trim();

        // 清理字符串：去首尾引号，处理转义
        evaluation = evaluation.replaceAll("^\"|\"$", "")  // 去除首尾双引号
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\t", " ")
                .trim();
        return evaluation;
    }

    private static int getSimilarity(int similarityStart, String jsonContent) {
        if (similarityStart == -1) throw new IllegalArgumentException("Missing 'similarity' field");
        int colon = jsonContent.indexOf(':', similarityStart);
        int comma = jsonContent.indexOf(',', similarityStart);
        int close = jsonContent.indexOf('}', similarityStart);
        int end = (comma != -1 && comma < close) ? comma : close;
        String similarityStr = jsonContent.substring(colon + 1, end).trim();
        similarityStr = similarityStr.replaceAll("[\"\\s]", ""); // 去引号空格
        return Integer.parseInt(similarityStr);
    }

    // 辅助方法：更鲁棒地查找 JSON 字段
    private int findKey(String json, String key) {
        String keyPattern = "\"" + key + "\"";
        int index = json.indexOf(keyPattern);
        if (index == -1) index = json.indexOf(key); // 兼容无引号
        return index;
    }

    // 内部静态类，用于封装相似度结果
    @Getter
    public static class SimilarityResult {
        private final int similarity;
        private final String evaluation;
        private final String timestamp;

        public SimilarityResult(int similarity, String evaluation) {
            this.similarity = similarity;
            this.evaluation = evaluation;
            this.timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

    }
}