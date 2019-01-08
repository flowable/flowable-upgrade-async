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
package org.activiti.upgrade;

import java.util.HashMap;
import java.util.Map;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.test.HistoryTestHelper;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.upgrade.helper.UpgradeUtil;

/**
 * @author Joram Barrez
 */
public class DataGenerator {

    public static void main(String[] args) {
        ProcessEngine processEngine = UpgradeUtil.getProcessEngine();
        createCommonData(processEngine);
    }

    private static void createCommonData(ProcessEngine processEngine) {
        generateSimplestTaskData(processEngine);
        generateTaskWithExecutionVariableskData(processEngine);
        generateCallActivityData(processEngine);
    }

    private static void generateCallActivityData(ProcessEngine processEngine) {
        RuntimeService runtimeService = processEngine.getRuntimeService();

        processEngine.getRepositoryService().createDeployment()
            .name("callActivityProcess")
            .addClasspathResource("org/flowable/upgrade/test/CallSimpleSubProcess.bpmn20.xml")
            .addClasspathResource("org/flowable/upgrade/test/CalledProcess.bpmn20.xml")
            .tenantId("callSimpleSubProcessTenant")
            .deploy();

        runtimeService.startProcessInstanceByKeyAndTenantId("callSimpleSubProcess", "callSimpleSubProcess", "callSimpleSubProcessTenant");
    }

    private static void generateTaskWithExecutionVariableskData(ProcessEngine processEngine) {
        RuntimeService runtimeService = processEngine.getRuntimeService();

        processEngine.getRepositoryService().createDeployment()
            .name("simpleTaskProcess")
            .addClasspathResource("org/flowable/upgrade/test/UserTaskBeforeTest.testTaskWithExecutionVariables.bpmn20.xml")
            .deploy();

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("instrument", "trumpet");
        variables.put("player", "gonzo");
        runtimeService.startProcessInstanceByKey("taskWithExecutionVariablesProcess", variables);
    }

    private static void generateSimplestTaskData(ProcessEngine processEngine) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        TaskService taskService = processEngine.getTaskService();

        processEngine.getRepositoryService().createDeployment()
            .name("simpleTaskProcess")
            .addClasspathResource("org/flowable/upgrade/test/UserTaskBeforeTest.testSimplestTask.bpmn20.xml")
            .tenantId("changeAssigneeTenant")
            .deploy();
        processEngine.getRepositoryService().createDeployment()
            .name("simpleTaskProcess")
            .addClasspathResource("org/flowable/upgrade/test/UserTaskBeforeTest.testSimplestTask.bpmn20.xml")
            .tenantId("changeAssigneeBeforeHistoricActivityCreatedTenant")
            .deploy();
        processEngine.getRepositoryService().createDeployment()
            .name("simpleTaskProcess")
            .addClasspathResource("org/flowable/upgrade/test/UserTaskBeforeTest.testSimplestTask.bpmn20.xml")
            .tenantId("changeOwnerTenant")
            .deploy();
        processEngine.getRepositoryService().createDeployment()
            .name("simpleTaskProcess")
            .addClasspathResource("org/flowable/upgrade/test/UserTaskBeforeTest.testSimplestTask.bpmn20.xml")
            .tenantId("simpleTaskProcessTenant")
            .deploy();

        runtimeService.startProcessInstanceByKeyAndTenantId("simpleTaskProcess", "changeAssignee", "changeAssigneeTenant");
        runtimeService.startProcessInstanceByKeyAndTenantId("simpleTaskProcess", "changeOwner", "changeOwnerTenant");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId("simpleTaskProcess", "completeTask", "simpleTaskProcessTenant");
        String taskId = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId();
        taskService.complete(taskId);

        HistoryTestHelper.waitForJobExecutorToProcessAllHistoryJobs(processEngine.getProcessEngineConfiguration(), processEngine.getManagementService(), 15000, 200);

        runtimeService.startProcessInstanceByKeyAndTenantId("simpleTaskProcess", "changeAssigneeBeforeHistoricActivityCreated", "changeAssigneeBeforeHistoricActivityCreatedTenant");
    }

}
