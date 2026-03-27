package org.github.bootcamp.microservice.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Programmatic Sentinel flow-control rules using Java text blocks for JSON definition.
 *
 * @author zhuling
 */
@Slf4j
@Configuration
public class SentinelRuleConfig {

  // Java 15+ text block — human-readable inline JSON for Sentinel rules
  private static final String FLOW_RULES_JSON =
      """
        [
          {
            "resource": "/api/v1/news/trending",
            "grade": 1,
            "count": 100,
            "strategy": 0,
            "controlBehavior": 0,
            "limitApp": "default"
          },
          {
            "resource": "/api/v1/news/trending/refresh",
            "grade": 1,
            "count": 10,
            "strategy": 0,
            "controlBehavior": 0,
            "limitApp": "default"
          }
        ]
        """;

  @PostConstruct
  public void initFlowRules() {
    try {
      var mapper = new ObjectMapper();
      List<FlowRule> rules =
          mapper.readValue(FLOW_RULES_JSON, new TypeReference<List<FlowRule>>() {});
      // Ensure grade constants are set correctly
      rules.forEach(rule -> rule.setGrade(RuleConstant.FLOW_GRADE_QPS));
      FlowRuleManager.loadRules(rules);
      log.info("Sentinel flow rules loaded: {} rules", rules.size());
    } catch (Exception e) {
      log.error("Failed to load Sentinel flow rules", e);
    }
  }
}
