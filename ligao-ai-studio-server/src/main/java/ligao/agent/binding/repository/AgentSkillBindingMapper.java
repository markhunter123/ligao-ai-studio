package ligao.agent.binding.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.agent.binding.model.AgentSkillBinding;

@Mapper
public interface AgentSkillBindingMapper extends BaseMapper<AgentSkillBinding> {
}
