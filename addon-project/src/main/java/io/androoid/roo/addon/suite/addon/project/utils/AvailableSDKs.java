package io.androoid.roo.addon.suite.addon.project.utils;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This enum contains all SDKs that Androoid Spring Roo Add-On Suite allows to
 * use on Android project generation.
 * 
 * @author Juan Carlos García
 * @since 1.0
 */
public enum AvailableSDKs {
	ICE_CREAM_SANDWICH(14), ICE_CREAM_SANDWICH_MR1(15), JELLY_BEAN(16), JELLY_BEAN_MR1(17), JELLY_BEAN_MR2(18), KITKAT(
			19), LOLLIPOP(21), LOLLIPOP_MR1(22);

	private Integer apiLevel;

	private AvailableSDKs(Integer apiLevel) {
		Validate.notNull(apiLevel, "Property name required");
		this.apiLevel = apiLevel;
	}

	public Integer getApiLevel() {
		return apiLevel;
	}

	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("apiLevel", apiLevel);
		return builder.toString();
	}
}