package io.androoid.roo.addon.suite.dependency.manager;

import io.androoid.roo.addon.suite.dependency.manager.providers.DependencyManagerProvider;
import io.androoid.roo.addon.suite.dependency.manager.providers.DependencyManagerProviderId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;

@Component
@Service
@Reference(name = "provider", strategy = ReferenceStrategy.EVENT, policy = ReferencePolicy.DYNAMIC, referenceInterface = DependencyManagerProvider.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
public class DependencyManagerOperationsImpl implements
		DependencyManagerOperations {

	private List<DependencyManagerProvider> providers = new ArrayList<DependencyManagerProvider>();
	private List<DependencyManagerProviderId> providersId = null;

	/** {@inheritDoc} **/
	public DependencyManagerProviderId getProviderByName(String name) {
		DependencyManagerProviderId provider = null;
		for (DependencyManagerProvider tmpProvider : providers) {
			if (tmpProvider.getName().equals(name)) {
				provider = new DependencyManagerProviderId(tmpProvider);
			}
		}

		return provider;
	}

	/** {@inheritDoc} **/
	public List<DependencyManagerProviderId> getProvidersId() {
		if (providersId == null) {
			providersId = new ArrayList<DependencyManagerProviderId>();
			for (DependencyManagerProvider tmpProvider : providers) {
				providersId.add(new DependencyManagerProviderId(tmpProvider));
			}
			providersId = Collections.unmodifiableList(providersId);
		}
		return providersId;
	}

	/** {@inheritDoc} **/
	public DependencyManagerProvider getInstalledProvider() {
		DependencyManagerProvider provider = null;
		for (DependencyManagerProvider tmpProvider : providers) {
			if (tmpProvider.isInstalled()) {
				provider = tmpProvider;
			}
		}
		return provider;
	}

	/**
	 * This method load new providers
	 * 
	 * @param provider
	 */
	protected void bindProvider(final DependencyManagerProvider provider) {
		providers.add(provider);
	}

	/**
	 * This method remove providers
	 * 
	 * @param provider
	 */
	protected void unbindProvider(final DependencyManagerProvider provider) {
		providers.remove(provider);
	}
}
