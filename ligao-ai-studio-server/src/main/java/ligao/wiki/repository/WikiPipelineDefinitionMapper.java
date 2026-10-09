package ligao.wiki.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.wiki.model.WikiPipelineDefinitionEntity;

/**
 * Mapper for {@link WikiPipelineDefinitionEntity}.
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface WikiPipelineDefinitionMapper extends BaseMapper<WikiPipelineDefinitionEntity> {
}
