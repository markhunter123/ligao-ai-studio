package ligao.workflow.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.workflow.model.WorkflowRevisionEntity;

@Mapper
public interface WorkflowRevisionMapper extends BaseMapper<WorkflowRevisionEntity> {
}
