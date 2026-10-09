package ligao.wiki.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.wiki.model.WikiPipelineStepRunEntity;

/**
 * Mapper for {@link WikiPipelineStepRunEntity}.
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface WikiPipelineStepRunMapper extends BaseMapper<WikiPipelineStepRunEntity> {
}
