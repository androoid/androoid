package io.androoid.roo.addon.suite.dependency.manager.providers;

import io.androoid.roo.addon.suite.dependency.manager.DependencyManagerOperations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.shell.Completion;
import org.springframework.roo.shell.Converter;
import org.springframework.roo.shell.MethodTarget;

/**
 * 
 * DependencyManager Provider ID converter.
 * 
 * This class allows developers to obtain avalable dependency manager providers.
 * 
 * @author Juan Carlos García
 * @since 1.0.0
 *
 */
@Component
@Service
public class ProviderIdConverter implements
		Converter<DependencyManagerProviderId> {
	
	@Reference 
	DependencyManagerOperations operations;

	public DependencyManagerProviderId convertFromText(String value,
			Class<?> targetType, String optionContext) {
		return operations.getProviderByName(value);
	}

	public boolean getAllPossibleValues(List<Completion> completions,
			Class<?> targetType, String existingData, String optionContext,
			MethodTarget target) {
		for (final DependencyManagerProviderId id : operations.getProvidersId()) {
			if (existingData.isEmpty() || id.getId().equals(existingData)
					|| id.getId().startsWith(existingData)) {
				completions.add(new Completion(id.getId()));
			}
		}
		return true;
	}

	public boolean supports(Class<?> type, String optionContext) {
		return DependencyManagerProviderId.class.isAssignableFrom(type);
	}

}