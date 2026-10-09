package ligao.tool.guard.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.tool.guard.model.ToolGuardRuleEntity;

@Mapper
public interface ToolGuardRuleMapper extends BaseMapper<ToolGuardRuleEntity> {
}
