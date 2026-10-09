package ligao.workflow.runtime.mode;

import org.springframework.stereotype.Component;
import ligao.workflow.compiler.ir.WorkflowStep;
import ligao.workflow.runtime.AgentStepExecutor;
import ligao.workflow.runtime.StepAdapter;
import ligao.workflow.runtime.StepResult;
import ligao.workflow.runtime.WorkflowRunContext;

/**
 * {@code sequential} — runs after the previous step finishes and threads its
 * output forward via {@code outputs[outputVar]}. The default mode for any
 * agent-call step that does not need parallel or guarded execution.
 */
@Component
public class SequentialStepAdapter implements StepAdapter {

    private final AgentStepExecutor executor;

    public SequentialStepAdapter(AgentStepExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String typeName() { return "sequential"; }

    @Override
    public StepResult execute(WorkflowStep step, WorkflowRunContext context) {
        return executor.run(step, context);
    }
}
