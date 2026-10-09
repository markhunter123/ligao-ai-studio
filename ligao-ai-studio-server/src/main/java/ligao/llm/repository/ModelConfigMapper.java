package ligao.llm.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.llm.model.ModelConfigEntity;

@Mapper
public interface ModelConfigMapper extends BaseMapper<ModelConfigEntity> {
}
