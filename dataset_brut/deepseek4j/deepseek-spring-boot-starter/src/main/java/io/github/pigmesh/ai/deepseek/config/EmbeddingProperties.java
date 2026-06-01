package io.github.pigmesh.ai.deepseek.config;

import io.github.pigmesh.ai.deepseek.core.DeepSeekConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lengleng
 * @date 2025/2/6
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "embedding")
public class EmbeddingProperties extends DeepSeekConfig {

	/**
	 * 基本 URL
	 */
	private String baseUrl = "http://127.0.0.1:11434/v1";

}
