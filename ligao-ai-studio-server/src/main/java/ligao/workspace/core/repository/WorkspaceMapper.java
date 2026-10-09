package ligao.workspace.core.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.workspace.core.model.WorkspaceEntity;

/**
 * 工作区 Mapper
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface WorkspaceMapper extends BaseMapper<WorkspaceEntity> {
}
