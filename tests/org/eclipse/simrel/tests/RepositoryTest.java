package org.eclipse.simrel.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.simrel.tests.jars.BREETest;
import org.eclipse.simrel.tests.jars.ESTest;
import org.eclipse.simrel.tests.jars.Pack200Test;
import org.eclipse.simrel.tests.jars.TestLayoutTest;
import org.eclipse.simrel.tests.jars.VersionTest;
import org.eclipse.simrel.tests.repos.FeatureDisplayableDataChecker;
import org.eclipse.simrel.tests.repos.FeatureNameLengths;
import org.eclipse.simrel.tests.repos.IUNameChecker;
import org.eclipse.simrel.tests.repos.IUVersionCheckToReference;
import org.eclipse.simrel.tests.repos.ProviderNameChecker;
import org.eclipse.simrel.tests.repos.TestRepo;
import org.eclipse.simrel.tests.repos.VersionChecking;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit TestCase which runs all the available checks as Junit Plugin Tests
 * 
 * @author Dennis Huebner
 *
 */
public class RepositoryTest {

    private static final String      SKIP_CHECKER_PROP_NAME = "skipChecker";
    private static final Set<String> SKIPPED_CHECKER        = new HashSet<String>();
    private static String            dirToTest;
    private static String            repoToTest;
    private static String            refRepoDir;

    @BeforeClass
    public static final void beforeClass() {
        BuildRepoTests tests = new BuildRepoTests();
        String directoryToCheck = tests.getDirectoryToCheck();
        if (directoryToCheck == null) {
            fail("Repository directory was not specified. Use -D" + BuildRepoTests.REPORT_REPO_DIR_PARAM
                    + "=/dir/location to pass the repository location");
        }
        dirToTest = directoryToCheck;
        repoToTest = "file://" + directoryToCheck;
        refRepoDir = tests.getDirectoryToCheckForReference();

        String skipCheckerProp = System.getProperty(SKIP_CHECKER_PROP_NAME);
        if (skipCheckerProp != null) {
            String[] split = skipCheckerProp.split(";");
            for (String checkerName : split) {
                SKIPPED_CHECKER.add(checkerName);
                System.out.println("Checker " + checkerName + " will be skipped.");
            }
        }
    }

    @AfterClass
    public static final void afterClass() {
        String output = new BuildRepoTests().getReportOutputDirectory();
        System.out.println("See reports in:" + output);
    }

    @Test
    public void testVersionUniqness() throws ProvisionException, OperationCanceledException, URISyntaxException, IOException {
        VersionChecking checker = new VersionChecking();
        if (configureChecker(checker)) {
            assertTrue("Unique Versions used in repository", !checker.testVersionUniqness());
        }
    }

    @Test
    public void testIUNames() throws ProvisionException, OperationCanceledException, URISyntaxException, IOException {
        IUNameChecker checker = new IUNameChecker();
        if (configureChecker(checker)) {
            assertTrue("Correct feature names", !checker.testFeatureNames());
            assertTrue("Correct bundle names", !checker.testBundleNames());
        }
    }

    @Test
    public void testProviderName() throws ProvisionException, OperationCanceledException, URISyntaxException, IOException {
        ProviderNameChecker checker = new ProviderNameChecker();
        if (configureChecker(checker)) {
            assertTrue("Correct provider names", !checker.testProviderNames());
        }
    }

    @Test
    public void testFeatureDisplayableData() throws ProvisionException, OperationCanceledException, URISyntaxException, IOException {
        FeatureDisplayableDataChecker checker = new FeatureDisplayableDataChecker();
        if (configureChecker(checker)) {
            assertTrue("Correct displayable data", !checker.testDisplayableData());
        }
    }

    @Test
    public void testFeatureNameLengths() throws ProvisionException, OperationCanceledException, URISyntaxException, IOException {
        FeatureNameLengths checker = new FeatureNameLengths();
        if (configureChecker(checker)) {
            assertFalse("FeatureDirectoryLengths is  <=" + FeatureNameLengths.MAX_CRITERIA,
                    checker.testFeatureDirectoryLength() > FeatureNameLengths.MAX_CRITERIA);
        }
    }

    @Test
    public void testIUVersionCheckToReference() throws ProvisionException, OperationCanceledException, URISyntaxException,
            IOException {
        IUVersionCheckToReference checker = new IUVersionCheckToReference();
        if (configureChecker(checker)) {
            if (refRepoDir != null) {
                assertTrue("Correct version changes", !checker.checkIUVersionsToReference());
                assertTrue("Correct version changes for features", !checker.checkIUVersionsToReferenceForFeatures());
            }
        }
    }

    @Test
    public void testEclipseSourceReferences() throws OperationCanceledException, IOException {
        ESTest checker = new ESTest();
        if (configureChecker(checker)) {
            assertTrue("Correct eclipse source reference", !checker.testESSettingRule());
        }
    }

    @Test
    public void testBREE() throws OperationCanceledException, IOException {
        BREETest checker = new BREETest();
        if (configureChecker(checker)) {
            assertTrue("Correct BREE", !checker.testBREESettingRule());
        }
    }

    @Test
    public void testPack200() throws OperationCanceledException, IOException {
        Pack200Test checker = new Pack200Test();
        if (configureChecker(checker)) {
            assertTrue("Correct Pack200", !checker.testBundlePack());
        }
    }

    @Test
    public void testVersionTest() throws OperationCanceledException, IOException {
        VersionTest checker = new VersionTest();
        if (configureChecker(checker)) {
            assertTrue("Correct version pattern", !checker.testVersionsPatterns());
        }
    }

    @Test
    public void testLayout() throws OperationCanceledException, IOException {
        TestLayoutTest checker = new TestLayoutTest();
        if (configureChecker(checker)) {
            assertTrue("Correct files and layout in bundles and features.", !checker.testLayout());
        }
    }

    private boolean configureChecker(BuildRepoTests checker) {
        checker.setDirectoryToCheck(dirToTest);
        if (checker instanceof TestRepo) {
            ((TestRepo) checker).setRepoURLToTest(repoToTest);
        }
        return !skipChecker(checker);
    }

    private boolean skipChecker(BuildRepoTests checker) {
        if (SKIPPED_CHECKER.size() > 0) {
            return SKIPPED_CHECKER.contains(checker.getClass().getSimpleName());
        }
        return false;
    }

}