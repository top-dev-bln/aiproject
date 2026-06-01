package com.pig4cloud.plugin.excel.mock;

import org.apache.fesod.sheet.annotation.ExcelProperty;
import lombok.Data;

/**
 * Mock数据实体类
 *
 * @author lengleng
 * @date 2025/12/08
 */
@Data
public class MockData {

	/**
	 * 用户ID
	 */
	@ExcelProperty(value = "用户ID", index = 0)
	private Long userId;

	/**
	 * 用户名
	 */
	@ExcelProperty(value = "用户名", index = 1)
	private String username;

	/**
	 * 邮箱
	 */
	@ExcelProperty(value = "邮箱", index = 2)
	private String email;

	/**
	 * 年龄
	 */
	@ExcelProperty(value = "年龄", index = 3)
	private Integer age;

	/**
	 * 电话号码
	 */
	@ExcelProperty(value = "电话号码", index = 4)
	private String phone;

	/**
	 * 地址
	 */
	@ExcelProperty(value = "地址", index = 5)
	private String address;

	/**
	 * 部门
	 */
	@ExcelProperty(value = "部门", index = 6)
	private String department;

	/**
	 * 薪资
	 */
	@ExcelProperty(value = "薪资", index = 7)
	private Double salary;

}
