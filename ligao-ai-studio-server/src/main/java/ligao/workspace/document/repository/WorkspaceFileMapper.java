package ligao.workspace.document.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.workspace.document.model.WorkspaceFileEntity;

/**
 * 工作区文件 Mapper
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface WorkspaceFileMapper extends BaseMapper<WorkspaceFileEntity> {
}
