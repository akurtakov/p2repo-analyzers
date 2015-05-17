/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.simrel.tests.common.checker;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.simrel.tests.common.CheckReport;
import org.eclipse.simrel.tests.common.P2RepositoryDescription;
import org.eclipse.simrel.tests.common.ReportType;
import org.eclipse.simrel.tests.common.utils.IUUtil;

/**
 * @author Dennis Huebner
 *
 *
 */
public class ProviderNameChecker implements IInstalationUnitChecker {

	private static final String OLD_PROVIDER_NAME = "Eclipse.org";

	private String[] EXPECTED_PROVIDER_NAMES = { "Eclipse Equinox Project", "Eclipse PTP", "Eclipse Orbit",
			"Eclipse Web Tools Platform", "Eclipse CDT", "Eclipse Agent Modeling Platform", "Eclipse BIRT Project",
			"Eclipse Data Tools Platform", "Eclipse Modeling Project", "Eclipse Mylyn", "Eclipse Memory Analyzer",
			"Eclipse Linux Tools", "Eclipse Jubula", "Eclipse Jetty Project", "Eclipse Gyrex", "Eclipse EGit",
			"Eclipse JGit", "Eclipse Agent Modeling Project", "Eclipse Packaging Project", "Eclipse Scout Project",
			"Eclipse Sequoyah", "Eclipse TM Project", "Eclipse SOA", "Eclipse Koneki", "Eclipse Model Focusing Tools",
			"Eclipse Code Recommenders", "Eclipse RTP", "Eclipse Stardust", "Eclipse JWT", "Eclipse Xtend" };
	private Set<String> knownProviderNames = null;

	@Override
	public void check(final Consumer<? super CheckReport> consumer, final P2RepositoryDescription descr,
			final IInstallableUnit iu) {
		// ignore categories
		boolean isCategory = "true".equals(iu.getProperty("org.eclipse.equinox.p2.type.category"));
		// TODO: should we exclude fragments?
		boolean isFragment = "true".equals(iu.getProperty("org.eclipse.equinox.p2.type.fragment"));

		// || iu.getId().endsWith("feature.group")
		if (!isCategory && !IUUtil.isSpecial(iu) && !isFragment) {
			CheckReport checkReport = new CheckReport(ProviderNameChecker.class, iu);

			String providerName = iu.getProperty(IInstallableUnit.PROP_PROVIDER, null);
			if (providerName == null) {
				incorrectProviderName(checkReport);
			}
			// common errors and misspellings
			else if (providerName.startsWith("%") || providerName.equals("Eclipse")
					|| providerName.startsWith("eclipse.org") || providerName.equals("unknown")
					|| providerName.startsWith("Engineering") || providerName.contains("org.eclipse.jwt")
					|| providerName.contains("www.example.org") || providerName.contains("www.eclipse.org")
					|| providerName.contains("Provider") || providerName.contains("provider")
					|| providerName.startsWith("Bundle-") || providerName.startsWith("bund")
					|| providerName.startsWith("Eclispe")) {
				incorrectProviderName(checkReport);
			} else if (providerName.startsWith("Eclipse.org - ")) {
				correctProviderName(checkReport);
			} else if (getKnownProviderNames().contains(providerName)) {
				correctProviderName(checkReport);
			} else if (OLD_PROVIDER_NAME.equals(providerName)) {
				oldProviderName(checkReport);
			}
			// order is important, starts with Eclipse, but not one
			// of the above e.g. "Eclipse Orbit" or "Eclipse.org"?
			// TODO: eventually put in with "incorrect?"
			else if (providerName.startsWith("Eclipse")) {
				unknownProviderName(checkReport);
			} else {
				if (iu.getId().startsWith("org.eclipse")) {
					suspectProviderName(checkReport);
				} else {
					unknownProviderName(checkReport);
				}
			}
			checkReport.setCheckResult(providerName);
			consumer.accept(checkReport);
		}

	}

	public Set<String> getKnownProviderNames() {
		if (this.knownProviderNames == null) {
			Set<String> temp = new HashSet<>();
			for (String string : this.EXPECTED_PROVIDER_NAMES) {
				temp.add(string);
			}
			this.knownProviderNames = Collections.unmodifiableSet(temp);
		}
		return this.knownProviderNames;
	}

	private void suspectProviderName(final CheckReport checkReport) {
		checkReport.setType(ReportType.NOT_IN_TRAIN);
	}

	private void incorrectProviderName(final CheckReport checkReport) {
		checkReport.setType(ReportType.NOT_IN_TRAIN);
	}

	private void unknownProviderName(final CheckReport checkReport) {
		checkReport.setType(ReportType.BAD_GUY);
	}

	private void oldProviderName(final CheckReport checkReport) {
		checkReport.setType(ReportType.BAD_GUY);
	}

	private void correctProviderName(final CheckReport checkReport) {
		checkReport.setType(ReportType.INFO);
	}
}