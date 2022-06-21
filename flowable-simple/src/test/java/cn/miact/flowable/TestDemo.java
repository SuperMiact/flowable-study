package cn.miact.flowable;

import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : mawei
 * @Classname : test
 * @createDate : 2022-06-20 14:49:18
 * @Description :
 */
public class TestDemo {

    /**
     * 获取流程引擎对象
     */
    @Test
    public void testProcessEngine() {
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration();
        cfg.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        cfg.setJdbcDriver("com.mysql.cj.jdbc.Driver");
        cfg.setJdbcUrl("jdbc:mysql://xx.xx.xx.xx:3306/flowable-study?useSSL=false&serverTimezone=UTC");
        cfg.setJdbcUsername("root");
        cfg.setJdbcPassword("123456");
        ProcessEngine processEngine = cfg.buildProcessEngine();
        System.out.println("processEngine = " + processEngine);
    }

    ProcessEngineConfiguration configuration = null;

    @Before
    public void before() {
        // 获取 ProcessEngineConfiguration 对象
        configuration = new StandaloneProcessEngineConfiguration();
        // 配置 相关的数据库的连接信息
        configuration.setJdbcDriver("com.mysql.cj.jdbc.Driver");
        configuration.setJdbcUrl("jdbc:mysql://xx.xx.xx.xx:3306/flowable-study?useSSL=false&serverTimezone=UTC");
        configuration.setJdbcUsername("root");
        configuration.setJdbcPassword("123456");
        // 如果数据库中的表结构不存在就新建
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    }

    /**
     * 部署流程
     */
    @Test
    public void testDeploy() {
        // 1、获得 ProcessEngine 对象
        ProcessEngine processEngine = configuration.buildProcessEngine();
        // 2、获得 RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();
        // 3、完成流程的部署操作
        Deployment deployment = repositoryService.createDeployment()
                // 通过XML文件的路径创建一个新的部署
                .addClasspathResource("holiday-request.bpmn20.xml")
                // 部署的名称
                .name("某某流程")
                // 执行
                .deploy();
        System.out.println("deployment.getId() = " + deployment.getId());
        System.out.println("deployment.getName() = " + deployment.getName());
    }

    /**
     * 查看流程定义的信息
     */
    @Test
    public void testDepolyQuery() {
        ProcessEngine processEngine = configuration.buildProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId("2501")
                .singleResult();
        System.out.println("processDefinition.getDeploymentId() = " + processDefinition.getDeploymentId());
        System.out.println("processDefinition.getName() = " + processDefinition.getName());
        System.out.println("processDefinition.getDescription() = " + processDefinition.getDescription());
        System.out.println("processDefinition.getId() = " + processDefinition.getId());
    }

    /**
     * 删除流程定义
     */
    @Test
    public void testDeleteDeploy() {
        ProcessEngine processEngine = configuration.buildProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        // 删除部署的流程，第一个参数为 id，如果部署的流程启动了，就不允许删除了
        // 第二个参数，代表是否级联删除，默认为 false，如果为 true 那么流程启动了 相关的任务会一并删除
        repositoryService.deleteDeployment("2501",true);
    }

    /**
     * 启动流程实例
     */
    @Test
    public void testRunProcess(){

        String employee = "miact";
        Integer nrOfHolidays = 5;
        String description = "hello,world";

        ProcessEngine processEngine = configuration.buildProcessEngine();
        // 通过 RuntimeService 来启动流程实例
        RuntimeService runtimeService = processEngine.getRuntimeService();
        // 构建流程变量
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("employee",employee);
        hashMap.put("nrOfHolidays",nrOfHolidays);
        hashMap.put("description",description);
        // 启动流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holidayRequest", hashMap);

        System.out.println("processInstance.getProcessDefinitionId() = " + processInstance.getProcessDefinitionId());
        System.out.println("processInstance.getActivityId() = " + processInstance.getActivityId());
        System.out.println("processInstance.getId() = " + processInstance.getId());
    }

    /**
     * 查询任务
     */
    @Test
    public void testQueryTask(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey("holidayRequest")
                .taskAssignee("zhangsan")
                .list();
        for (Task task : list) {
            System.out.println("task.getProcessDefinitionId() = " + task.getProcessDefinitionId());
            System.out.println("task.getName() = " + task.getName());
            System.out.println("task.getAssignee() = " + task.getAssignee());
            System.out.println("task.getDescription() = " + task.getDescription());
            System.out.println("task.getId() = " + task.getId());
        }
    }

    /**
     * 完成当前任务
     */
    @Test
    public void testCompleteTask(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("holidayRequest")
                .taskAssignee("zhangsan")
                .singleResult();
        // 创建流程变量
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("approved",false);
        // 完成任务
        taskService.complete(task.getId(),hashMap);
    }

    /**
     * 获取流程任务的历史数据
     */
    @Test
    public void testHistory(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                .processDefinitionId("holidayRequest:1:7503")
                .finished() // 查询的历史记录的状态是已经完成的
                .orderByHistoricActivityInstanceEndTime().asc() // 指定排序的字段和顺序
                .list();
        for (HistoricActivityInstance history : historicActivityInstanceList) {
            System.out.println(history.getActivityName() + ":" + history.getAssignee() + ":" + history.getActivityId() + ":" + history.getDurationInMillis()+"毫秒");
        }
    }

}