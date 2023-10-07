/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/EmptyTestNGTest.java to edit this template
 */
package com.github.thmarx.cms.utils;

import org.assertj.core.api.Assertions;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author t.marx
 */
public class DateUtilNGTest {
	
	public DateUtilNGTest() {
	}

	@Test
	public void testSomeMethod() {
		Assertions.assertThat(DateUtil.isDate("2023-10-07")).isTrue();
		Assertions.assertThat(DateUtil.isDate("07-10-2023")).isFalse();
	}
	
}
