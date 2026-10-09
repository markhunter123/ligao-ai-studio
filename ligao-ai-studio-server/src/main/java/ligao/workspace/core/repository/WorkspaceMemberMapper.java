package ligao.workspace.core.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.workspace.core.model.WorkspaceMemberEntity;

/**
 * 工作区成员 Mapper
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface WorkspaceMemberMapper extends BaseMapper<WorkspaceMemberEntity> {
}
