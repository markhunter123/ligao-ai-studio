package ligao.wiki.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.wiki.model.WikiPipelineRunEntity;

/**
 * Mapper for {@link WikiPipelineRunEntity}.
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface WikiPipelineRunMapper extends BaseMapper<WikiPipelineRunEntity> {
}
