package ligao.trigger.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.trigger.model.TriggerEntity;

@Mapper
public interface TriggerMapper extends BaseMapper<TriggerEntity> {
}
