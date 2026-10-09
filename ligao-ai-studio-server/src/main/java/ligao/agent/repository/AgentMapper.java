package ligao.agent.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.agent.model.AgentEntity;

/**
 * Agent 数据访问层
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface AgentMapper extends BaseMapper<AgentEntity> {
}
