package org.flowable;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class SendRejectionMail implements JavaDelegate {

    /**
     * 这是一个 Flowable 中的触发器
     * @param execution
     */
    @Override
    public void execute(DelegateExecution execution) {
        // 触发器执行的逻辑，按照我们在流程中的定义应该给被拒绝的员工发送通知的邮件
        System.out.println("Sorry，你的请假被拒绝了...安心工作！");
    }
}
