package ligao.wiki.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ligao.wiki.model.WikiKnowledgeBaseEntity;

/**
 * Wiki 知识库 Mapper
 *
 * @author Zhouyin.Wei
 */
@Mapper
public interface WikiKnowledgeBaseMapper extends BaseMapper<WikiKnowledgeBaseEntity> {
}
