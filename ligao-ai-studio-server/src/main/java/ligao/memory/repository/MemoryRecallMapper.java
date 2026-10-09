package ligao.memory.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.memory.model.MemoryRecallEntity;

/**
 * 记忆召回追踪 Mapper
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface MemoryRecallMapper extends BaseMapper<MemoryRecallEntity> {
}
