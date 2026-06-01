package com.pig4cloud.plugin.excel.handler;

import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.read.listener.ReadListener;
import com.pig4cloud.plugin.excel.converters.DictTypeConvert;

/**
 * dict cache clear analysis 事件监听器
 *
 * @author lengleng
 * @date 2024/08/31
 */
public class DictCacheClearAnalysisEventListener implements ReadListener<Object> {

	@Override
	public void invoke(Object o, AnalysisContext analysisContext) {
	}

	@Override
	public void doAfterAllAnalysed(AnalysisContext analysisContext) {
		DictTypeConvert.cache.clear();
	}

}
