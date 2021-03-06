/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cbi.p2repo.analyzers.common.checker.impl;

import java.io.File;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.jar.JarEntry;

import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.cbi.p2repo.analyzers.common.CheckReport;
import org.eclipse.cbi.p2repo.analyzers.common.P2RepositoryDescription;
import org.eclipse.cbi.p2repo.analyzers.common.ReportType;
import org.eclipse.cbi.p2repo.analyzers.common.checker.IArtifactChecker;
import org.eclipse.cbi.p2repo.analyzers.common.utils.IUUtil;

/**
 * @author dhuebner - Initial contribution and API
 */
public class SignatureChecker implements IArtifactChecker {

	/**
	 * META-INF/eclipse.inf property key which indicates that signing was
	 * disabled for this jar
	 */
	private static final String JARPROCESSOR_EXCLUDE_SIGN = "jarprocessor.exclude.sign";

	@Override
	public void check(Consumer<? super CheckReport> consumer, P2RepositoryDescription descr, IInstallableUnit iu,
			IArtifactKey artifactKey, File file) {
		CheckReport report = createReport(iu, artifactKey);
		Properties eclipseInf = IUUtil.getEclipseInf(file);
		if (Boolean.valueOf(eclipseInf.getProperty(JARPROCESSOR_EXCLUDE_SIGN, "false"))) {
			report.setCheckResult("Signing was disabled using the eclipse.inf file.");
			report.setType(ReportType.BAD_GUY);
		} else {
			JarEntry newRSA = IUUtil.getJarEntry(file, "META-INF/ECLIPSE_.RSA");
			JarEntry oldRSA = IUUtil.getJarEntry(file, "META-INF/ECLIPSEF.RSA");
			boolean signed = false;
			boolean reSigned = false;
			if (newRSA == null) {
				if (oldRSA != null) {
					signed = true;
				}
			} else {
				if (oldRSA != null) {
					reSigned = true;
				}
				signed = true;
			}
			if (!signed) {
				if (!artifactKey.getClassifier().equals("binary")) {
					report.setCheckResult("Jar is probably not signed.");
					report.setType(ReportType.NOT_IN_TRAIN);
				} else {
					report.setCheckResult("Unsigned binary file.");
					report.setType(ReportType.INFO);
				}
			} else {
				if (reSigned) {
					report.setCheckResult("Probably re-signed. Contains ECLIPSE_.RSA and ECLIPSEF.RSA");
					report.setType(ReportType.WARNING);
				} else {
					report.setCheckResult("Probably signed.");
					report.setType(ReportType.INFO);
				}
			}
		}
		consumer.accept(report);
	}

}
