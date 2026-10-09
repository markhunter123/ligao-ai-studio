package ligao.workflow.runtime.mode;

import org.springframework.stereotype.Component;
import ligao.workflow.compiler.ir.WorkflowStep;
import ligao.workflow.runtime.AgentStepExecutor;
import ligao.workflow.runtime.StepAdapter;
import ligao.workflow.runtime.StepResult;
import ligao.workflow.runtime.WorkflowRunContext;

/**
 * {@code fan_out} — body of a parallel group. Each fan_out step runs against
 * the run context snapshot that existed when the group started; the runner
 * dispatches the whole group in parallel and merges {@code outputs} only when
 * the terminating {@code collect} runs. From the adapter's perspective the
 * step body is identical to a sequential agent call — the parallelism is
 * orchestrated upstream.
 */
@Component
public class FanOutStepAdapter implements StepAdapter {

    private final AgentStepExecutor executor;

    public FanOutStepAdapter(AgentStepExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String typeName() { return "fan_out"; }

    @Override
    public StepResult execute(WorkflowStep step, WorkflowRunContext context) {
        return executor.run(step, context);
    }
}
