/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.upgrade.helper;

import java.util.logging.Logger;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.upgrade.DbDropUtil;

public class UpgradeUtil {

    private static Boolean DATABASE_DROPPED = false;

    private static final Logger LOG = Logger.getLogger(UpgradeUtil.class.getName());

    public static ProcessEngine getProcessEngine() {
        return getProcessEngine("flowable.cfg.xml");
    }

    public static ProcessEngine getProcessEngine(String configResource) {
        ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
                .createProcessEngineConfigurationFromResource(configResource);

        // When the 'old version' tests are run, we drop the schema always once for the first test
        if (!DATABASE_DROPPED && isTestRunningAgainstOldVersion()) {
            synchronized (DATABASE_DROPPED) {
                if (!DATABASE_DROPPED) {
                    DATABASE_DROPPED = DbDropUtil.dropDatabaseTable(processEngineConfiguration.getJdbcDriver(), 
                            processEngineConfiguration.getJdbcUrl(), 
                            processEngineConfiguration.getJdbcUsername(), 
                            processEngineConfiguration.getJdbcPassword());
                    LOG.info("Dropping upgrade database completed");
                }
            }
        }

        // Building the process engine will also recreate the schema (for that particular version)
        return processEngineConfiguration.buildProcessEngine();
    }

    protected static boolean isTestRunningAgainstOldVersion() {
        // We're piggybacking on the maven test skip property to know if we're generating data in the old version
        String runningTest = System.getProperty("maven.test.skip");
        return runningTest != null && runningTest.equals("true");
    }

}
